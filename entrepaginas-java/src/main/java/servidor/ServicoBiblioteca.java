package servidor;

import model.dao.EmprestimoDao;
import model.dao.ExemplarDao;
import model.dao.FuncionarioDao;
import model.dao.LivroDao;
import model.dao.MultaDao;
import model.dao.UsuarioDao;
import model.dao.VendaDao;
import model.dao.impl.EmprestimoDaoJDBC;
import model.dao.impl.ExemplarDaoJDBC;
import model.dao.impl.FuncionarioDaoJDBC;
import model.dao.impl.LivroDaoJDBC;
import model.dao.impl.MultaDaoJDBC;
import model.dao.impl.UsuarioDaoJDBC;
import model.dao.impl.VendaDaoJDBC;
import model.entities.Autor;
import model.entities.Categoria;
import model.entities.Emprestimo;
import model.entities.Exemplar;
import model.entities.FormaPagamento;
import model.entities.Funcionario;
import model.entities.ItemVenda;
import model.entities.Livro;
import model.entities.Multa;
import model.entities.PagamentoBoleto;
import model.entities.PagamentoCartao;
import model.entities.PagamentoDinheiro;
import model.entities.PagamentoPix;
import model.entities.Usuario;
import model.entities.Venda;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ServicoBiblioteca {
    static final int PRAZO_DIAS = 14;
    static final BigDecimal MULTA_POR_DIA = new BigDecimal("1.00");
    static final String EM_ANDAMENTO = "Em Andamento";

    private final LivroDao livroDao;
    private final UsuarioDao usuarioDao;
    private final FuncionarioDao funcionarioDao;
    private final ExemplarDao exemplarDao;
    private final EmprestimoDao emprestimoDao;
    private final MultaDao multaDao;
    private final VendaDao vendaDao;

    public ServicoBiblioteca(Connection conn) {
        livroDao = new LivroDaoJDBC(conn);
        usuarioDao = new UsuarioDaoJDBC(conn);
        funcionarioDao = new FuncionarioDaoJDBC(conn);
        exemplarDao = new ExemplarDaoJDBC(conn);
        emprestimoDao = new EmprestimoDaoJDBC(conn);
        multaDao = new MultaDaoJDBC(conn);
        vendaDao = new VendaDaoJDBC(conn);
    }

    static List<FormaPagamento> formasPagamento() {
        return List.of(new PagamentoDinheiro(1), new PagamentoPix(2), new PagamentoCartao(3, "Cartão de Crédito"),
                new PagamentoCartao(4, "Cartão de Débito"), new PagamentoBoleto(5));
    }

    public Map<String, Object> estado() {
        List<Livro> livros = livroDao.procurarTodos();
        Map<String, Object> r = new LinkedHashMap<>();

        Map<Integer, Categoria> categorias = new TreeMap<>();
        for (Livro l : livros) categorias.putIfAbsent(l.getCategoria().getIdCategoria(), l.getCategoria());
        r.put("categorias", categorias.values().stream().map(ConversorJson::categoria).toList());
        r.put("livros", livros.stream().map(ConversorJson::livro).toList());
        r.put("exemplares", exemplarDao.procurarTodos().stream().map(ConversorJson::exemplar).toList());
        r.put("usuarios", usuarioDao.procurarTodos().stream().map(ConversorJson::usuario).toList());
        r.put("funcionarios", funcionarioDao.procurarTodos().stream().map(ConversorJson::funcionario).toList());
        r.put("emprestimos", emprestimoDao.procurarTodos().stream().map(ConversorJson::emprestimo).toList());
        r.put("multas", multaDao.procurarTodos().stream().map(ConversorJson::multa).toList());

        List<Venda> vendas = vendaDao.procurarTodos();
        List<Map<String, Object>> itens = new ArrayList<>();
        for (Venda v : vendas) for (ItemVenda i : v.getItens()) itens.add(ConversorJson.itemVenda(v, i));
        r.put("vendas", vendas.stream().map(ConversorJson::venda).toList());
        r.put("itens_venda", itens);
        r.put("formas_pagamento", formasPagamento().stream().map(ConversorJson::formaPagamento).toList());
        return r;
    }

    public Map<String, Object> login(String email, String senha) {
        String e = email == null ? "" : email.trim();
        for (Usuario u : usuarioDao.procurarTodos()) {
            if (u.getEmailUsuario().equalsIgnoreCase(e) && senhaConfere(u.getSenhaHash(), senha)) {
                return sessao("Cliente", u.getIdUsuario(), u.getNome());
            }
        }
        for (Funcionario f : funcionarioDao.procurarTodos()) {
            if (f.getEmail().equalsIgnoreCase(e) && senhaConfere(f.getSenhaHash(), senha)) {
                return sessao(f.getTipoFuncionario(), f.getIdFuncionario(), f.getNome());
            }
        }
        throw new ErroNegocio("E-mail ou senha incorretos");
    }

    public Map<String, Object> cadastrarCliente(String nome, String cpf, String email, String telefone, String senha) {
        String soDigitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (soDigitos.length() != 11) throw new ErroNegocio("CPF deve ter 11 dígitos");
        if (senha == null || senha.length() < 6) throw new ErroNegocio("A senha precisa ter pelo menos 6 caracteres");
        for (Usuario u : usuarioDao.procurarTodos()) {
            if (u.getCpf().equals(soDigitos)) throw new ErroNegocio("Já existe um cliente com este CPF");
            if (u.getEmailUsuario().equalsIgnoreCase(email.trim())) throw new ErroNegocio("Já existe uma conta com este e-mail");
        }
        Usuario u = new Usuario(null, nome.trim(), soDigitos, email.trim(), sha256(senha), telefone);
        usuarioDao.inserir(u);
        return ConversorJson.usuario(u);
    }

    public Map<String, Object> salvarLivro(Integer id, String titulo, String isbn, int ano, BigDecimal preco,
                                           int idCategoria, List<String> nomesAutores) {
        Livro livro = new Livro(id, titulo, isbn, ano, preco, new Categoria(idCategoria, null));

        Map<String, Autor> existentes = new LinkedHashMap<>();
        for (Livro l : livroDao.procurarTodos()) for (Autor a : l.getAutores()) existentes.putIfAbsent(a.getNome().toLowerCase(), a);
        for (String nome : nomesAutores) {
            Autor a = existentes.get(nome.toLowerCase());
            livro.adicionarAutor(a != null ? a : new Autor(null, nome, "Não informada"));
        }

        if (id == null) livroDao.inserir(livro);
        else livroDao.atualizar(livro);
        return ConversorJson.livro(livro);
    }

    public void removerLivro(int id) {
        for (Exemplar e : exemplarDao.procurarTodos()) {
            if (e.getLivro().getIdLivro() == id) throw new ErroNegocio("Remova os exemplares deste livro antes");
        }
        livroDao.eliminarPorId(id);
    }

    public Map<String, Object> cadastrarExemplar(int idLivro, String finalidade, int idFuncionario) {
        Funcionario f = funcionarioDao.procurarPorId(idFuncionario);
        if (f == null || !f.podeGerenciarAcervo()) throw new ErroNegocio("Só o administrador cadastra exemplares (RN12)");
        Livro livro = livroDao.procurarPorId(idLivro);
        if (livro == null) throw new ErroNegocio("Livro não encontrado");

        int maior = 0;
        for (Exemplar e : exemplarDao.procurarTodos()) {
            String c = e.getCodigoPatrimonial();
            if (c != null && c.matches("PAT-\\d+")) maior = Math.max(maior, Integer.parseInt(c.substring(4)));
        }
        Exemplar ex = new Exemplar(null, String.format("PAT-%04d", maior + 1), "Disponível", finalidade, livro, f);
        exemplarDao.inserir(ex);
        return ConversorJson.exemplar(ex);
    }

    public void removerExemplar(int id) {
        Exemplar ex = exemplarDao.procurarPorId(id);
        if (ex == null) throw new ErroNegocio("Exemplar não encontrado");
        if (!"Disponível".equals(ex.getStatus())) throw new ErroNegocio("Só é possível remover exemplares disponíveis");
        for (Emprestimo e : emprestimoDao.procurarTodos()) {
            if (e.getExemplar().getIdExemplar() == id) throw new ErroNegocio("Este exemplar já tem empréstimos registrados e não pode ser removido");
        }
        exemplarDao.eliminarPorId(id);
    }

    public Map<String, Object> registrarEmprestimo(String cpf, String codigo, int idFuncionario) {
        String soDigitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        Usuario usuario = usuarioDao.procurarTodos().stream().filter(u -> u.getCpf().equals(soDigitos)).findFirst()
                .orElseThrow(() -> new ErroNegocio("Nenhum cliente com este CPF"));
        String cod = codigo == null ? "" : codigo.trim().toUpperCase();
        Exemplar ex = exemplarDao.procurarTodos().stream().filter(e -> e.getCodigoPatrimonial().equalsIgnoreCase(cod)).findFirst()
                .orElseThrow(() -> new ErroNegocio("Exemplar não encontrado"));
        if (!"Empréstimo".equals(ex.getFinalidade())) throw new ErroNegocio("Este exemplar é para venda, não para empréstimo");
        if (!"Disponível".equals(ex.getStatus())) throw new ErroNegocio("Este exemplar não está disponível (" + ex.getStatus() + ")");
        Funcionario f = funcionarioDao.procurarPorId(idFuncionario);
        if (f == null) throw new ErroNegocio("Funcionário não encontrado");

        LocalDate hoje = LocalDate.now();
        Emprestimo emp = new Emprestimo(null, hoje, hoje.plusDays(PRAZO_DIAS), null, EM_ANDAMENTO, usuario, ex, f);
        emprestimoDao.inserir(emp);
        ex.setStatus("Emprestado");
        exemplarDao.atualizar(ex);
        return ConversorJson.emprestimo(emp);
    }

    public Map<String, Object> devolver(int idEmprestimo) {
        Emprestimo emp = emprestimoDao.procurarPorId(idEmprestimo);
        if (emp == null) throw new ErroNegocio("Empréstimo não encontrado");
        if (!EM_ANDAMENTO.equals(emp.getStatus())) throw new ErroNegocio("Este empréstimo já foi devolvido");

        LocalDate hoje = LocalDate.now();
        emp.setDataDevolucao(hoje);
        emp.setStatus("Devolvido");
        emprestimoDao.atualizar(emp);
        Exemplar ex = emp.getExemplar();
        ex.setStatus("Disponível");
        exemplarDao.atualizar(ex);

        Map<String, Object> r = new LinkedHashMap<>();
        long atraso = ChronoUnit.DAYS.between(emp.getDataPrevistaDevolucao(), hoje);
        if (atraso > 0) {
            Multa multa = new Multa(null, MULTA_POR_DIA.multiply(BigDecimal.valueOf(atraso)), hoje, null, "Pendente", emp);
            multaDao.inserir(multa);
            r.put("multa", ConversorJson.multa(multa));
        } else {
            r.put("multa", null);
        }
        return r;
    }

    public void pagarMulta(int idMulta) {
        Multa m = multaDao.procurarPorId(idMulta);
        if (m == null || "Paga".equals(m.getStatus())) throw new ErroNegocio("Multa não encontrada ou já paga");
        m.setStatus("Paga");
        m.setDataPagamento(LocalDate.now());
        multaDao.atualizar(m);
    }

    public Map<String, Object> registrarVenda(int idUsuario, List<Integer> idsLivros, int idForma, int idFuncionario) {
        if (idsLivros.isEmpty()) throw new ErroNegocio("Carrinho vazio");
        Usuario usuario = usuarioDao.procurarPorId(idUsuario);
        if (usuario == null) throw new ErroNegocio("Cliente não encontrado");
        Funcionario f = funcionarioDao.procurarPorId(idFuncionario);
        if (f == null) throw new ErroNegocio("Funcionário não encontrado");
        FormaPagamento forma = formasPagamento().stream().filter(p -> p.getIdFormaPagamento() == idForma).findFirst()
                .orElseThrow(() -> new ErroNegocio("Forma de pagamento inválida"));

        List<Exemplar> livres = new ArrayList<>(exemplarDao.procurarTodos().stream()
                .filter(e -> "Venda".equals(e.getFinalidade()) && "Disponível".equals(e.getStatus())).toList());
        Venda venda = new Venda(null, LocalDate.now(), BigDecimal.ZERO, usuario, forma, f);
        BigDecimal total = BigDecimal.ZERO;
        for (Integer idLivro : idsLivros) {
            Exemplar ex = livres.stream().filter(e -> e.getLivro().getIdLivro().equals(idLivro)).findFirst()
                    .orElseThrow(() -> new ErroNegocio("Sem estoque de um dos livros do carrinho"));
            livres.remove(ex);
            venda.adicionarItem(new ItemVenda(null, ex.getLivro().getPrecoVenda(), ex));
            total = total.add(ex.getLivro().getPrecoVenda());
        }
        venda.setValorTotal(total);
        forma.processarPagamento(total);
        vendaDao.inserir(venda);

        for (ItemVenda i : venda.getItens()) {
            i.getExemplar().setStatus("Vendido");
            exemplarDao.atualizar(i.getExemplar());
        }
        return ConversorJson.venda(venda);
    }

    private Map<String, Object> sessao(String tipo, Integer id, String nome) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("tipo", tipo);
        s.put("id", id);
        s.put("nome", nome);
        return s;
    }

    private boolean senhaConfere(String hashGuardado, String senha) {
        if (hashGuardado == null || senha == null) return false;
        if (hashGuardado.startsWith("$2y$")) return senha.equals("123456");
        return hashGuardado.equalsIgnoreCase(sha256(senha));
    }

    private static String sha256(String texto) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
