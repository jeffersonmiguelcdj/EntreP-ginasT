package servidor;

import model.entities.Autor;
import model.entities.Categoria;
import model.entities.Emprestimo;
import model.entities.Exemplar;
import model.entities.FormaPagamento;
import model.entities.Funcionario;
import model.entities.ItemVenda;
import model.entities.Livro;
import model.entities.Multa;
import model.entities.Usuario;
import model.entities.Venda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ConversorJson {
    private ConversorJson() {}

    public static Map<String, Object> livro(Livro l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id_livro", l.getIdLivro());
        m.put("titulo", l.getTitulo());
        m.put("isbn", l.getIsbn());
        m.put("ano_publicacao", l.getAnoPublicacao());
        m.put("preco_venda", numero(l.getPrecoVenda()));
        m.put("id_categoria", l.getCategoria() != null ? l.getCategoria().getIdCategoria() : null);
        List<String> autores = new ArrayList<>();
        for (Autor a : l.getAutores()) autores.add(a.getNome());
        m.put("autores", autores);
        return m;
    }

    public static Map<String, Object> categoria(Categoria c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id_categoria", c.getIdCategoria());
        m.put("nome", c.getNome());
        return m;
    }

    public static Map<String, Object> usuario(Usuario u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id_usuario", u.getIdUsuario());
        m.put("nome", u.getNome());
        m.put("cpf", u.getCpf());
        m.put("email_usuario", u.getEmailUsuario());
        m.put("telefone", u.getTelefone());
        return m;
    }

    public static Map<String, Object> funcionario(Funcionario f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id_funcionario", f.getIdFuncionario());
        m.put("nome", f.getNome());
        m.put("email", f.getEmail());
        m.put("tipo_funcionario", f.getTipoFuncionario());
        m.put("pode_gerenciar_acervo", f.podeGerenciarAcervo());
        return m;
    }

    public static Map<String, Object> exemplar(Exemplar e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id_exemplar", e.getIdExemplar());
        m.put("codigo_patrimonial", e.getCodigoPatrimonial());
        m.put("status", e.getStatus());
        m.put("finalidade", e.getFinalidade());
        m.put("id_livro", e.getLivro().getIdLivro());
        m.put("id_funcionario", e.getFuncionarioCadastrou().getIdFuncionario());
        return m;
    }

    public static Map<String, Object> emprestimo(Emprestimo e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id_emprestimo", e.getIdEmprestimo());
        m.put("data_emprestimo", data(e.getDataEmprestimo()));
        m.put("data_prevista_devolucao", data(e.getDataPrevistaDevolucao()));
        m.put("data_devolucao", data(e.getDataDevolucao()));
        m.put("status", e.getStatus());
        m.put("id_usuario", e.getUsuario().getIdUsuario());
        m.put("id_exemplar", e.getExemplar().getIdExemplar());
        m.put("id_funcionario", e.getFuncionario().getIdFuncionario());
        return m;
    }

    public static Map<String, Object> multa(Multa mu) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id_multa", mu.getIdMulta());
        m.put("valor", numero(mu.getValor()));
        m.put("data_geracao", data(mu.getDataGeracao()));
        m.put("data_pagamento", data(mu.getDataPagamento()));
        m.put("status", mu.getStatus());
        m.put("id_emprestimo", mu.getEmprestimo().getIdEmprestimo());
        return m;
    }

    public static Map<String, Object> venda(Venda v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id_venda", v.getIdVenda());
        m.put("data_venda", data(v.getDataVenda()));
        m.put("valor_total", numero(v.getValorTotal()));
        m.put("id_usuario", v.getUsuario().getIdUsuario());
        m.put("id_forma_pagamento", v.getFormaPagamento().getIdFormaPagamento());
        m.put("id_funcionario", v.getFuncionario().getIdFuncionario());
        return m;
    }

    public static Map<String, Object> itemVenda(Venda v, ItemVenda i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id_item_venda", i.getIdItemVenda());
        m.put("preco_unitario", numero(i.getPrecoUnitario()));
        m.put("id_venda", v.getIdVenda());
        m.put("id_exemplar", i.getExemplar().getIdExemplar());
        return m;
    }

    public static Map<String, Object> formaPagamento(FormaPagamento f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id_forma_pagamento", f.getIdFormaPagamento());
        m.put("tipo", f.getTipo());
        return m;
    }

    private static String data(LocalDate d) {
        return d == null ? null : d.toString();
    }

    private static Double numero(BigDecimal b) {
        return b == null ? null : b.doubleValue();
    }
}
