package application;

import db.DB;
import model.dao.DaoFactory;
import model.dao.EmprestimoDao;
import model.dao.ExemplarDao;
import model.dao.FuncionarioDao;
import model.dao.LivroDao;
import model.dao.MultaDao;
import model.dao.UsuarioDao;
import model.dao.VendaDao;
import model.entities.Autor;
import model.entities.Categoria;
import model.entities.Emprestimo;
import model.entities.Exemplar;
import model.entities.Funcionario;
import model.entities.Livro;
import model.entities.Multa;
import model.entities.Usuario;
import model.entities.Venda;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        LivroDao livroDao = DaoFactory.criarLivroDao();
        UsuarioDao usuarioDao = DaoFactory.criarUsuarioDao();
        FuncionarioDao funcionarioDao = DaoFactory.criarFuncionarioDao();
        ExemplarDao exemplarDao = DaoFactory.criarExemplarDao();
        EmprestimoDao emprestimoDao = DaoFactory.criarEmprestimoDao();
        MultaDao multaDao = DaoFactory.criarMultaDao();
        VendaDao vendaDao = DaoFactory.criarVendaDao();

        int opcao = -1;

        while (opcao != 0) {
            System.out.println("\n==============================================================");
            System.out.println("          SISTEMA ENTREPÁGINAS - TERMINAL CRUD AO VIVO        ");
            System.out.println("==============================================================");
            System.out.println(" [LIVROS]");
            System.out.println("  1 - Listar todos os Livros (READ)");
            System.out.println("  2 - Cadastrar novo Livro   (CREATE)");
            System.out.println("  3 - Editar Livro existente (UPDATE)");
            System.out.println("  4 - Excluir Livro por ID   (DELETE)");
            System.out.println("--------------------------------------------------------------");
            System.out.println(" [USUÁRIOS]");
            System.out.println("  5 - Listar todos os Usuários (READ)");
            System.out.println("  6 - Cadastrar novo Usuário   (CREATE)");
            System.out.println("  7 - Editar Usuário existente (UPDATE)");
            System.out.println("  8 - Excluir Usuário por ID   (DELETE)");
            System.out.println("--------------------------------------------------------------");
            System.out.println(" [ESPECIAL APRESENTAÇÃO]");
            System.out.println("  9  - ROTEIRO GUIADO: Executar CRUD completo passo a passo");
            System.out.println("  10 - Consultar demais DAOs (Funcionários, Exemplares, Vendas)");
            System.out.println("  0  - Sair e Fechar Conexão com o Banco");
            System.out.println("==============================================================");
            System.out.print("Escolha uma opção: ");

            String entradaOpcao = sc.nextLine().trim();
            if (entradaOpcao.isEmpty()) {
                continue;
            }

            try {
                opcao = Integer.parseInt(entradaOpcao);
            } catch (NumberFormatException e) {
                System.out.println(">> Opção inválida! Digite apenas o número da opção.");
                continue;
            }

            try {
                switch (opcao) {
                    case 1:
                        listarLivros(livroDao);
                        break;
                    case 2:
                        cadastrarLivro(sc, livroDao);
                        break;
                    case 3:
                        editarLivro(sc, livroDao);
                        break;
                    case 4:
                        excluirLivro(sc, livroDao);
                        break;
                    case 5:
                        listarUsuarios(usuarioDao);
                        break;
                    case 6:
                        cadastrarUsuario(sc, usuarioDao);
                        break;
                    case 7:
                        editarUsuario(sc, usuarioDao);
                        break;
                    case 8:
                        excluirUsuario(sc, usuarioDao);
                        break;
                    case 9:
                        executarRoteiroGuiado(sc, livroDao);
                        break;
                    case 10:
                        mostrarDemaisDaos(funcionarioDao, exemplarDao, emprestimoDao, multaDao, vendaDao);
                        break;
                    case 0:
                        System.out.println("\nEncerrando aplicação e fechando conexão JDBC...");
                        break;
                    default:
                        System.out.println(">> Opção não encontrada! Escolha entre 0 e 10.");
                }
            } catch (Exception e) {
                System.out.println("\n>> AVISO DO BANCO DE DADOS: " + e.getMessage());
                System.out.println("   (Dica: Registros antigos que já possuem exemplares, vendas ou empréstimos");
                System.out.println("    vinculados são protegidos contra exclusão pela Chave Estrangeira do MySQL).");
            }
        }

        sc.close();
        DB.closeConnection();
        System.out.println("Conexão fechada com sucesso!");
    }

    private static void listarLivros(LivroDao livroDao) {
        System.out.println("\n--- [READ] LISTA DE LIVROS NO BANCO DE DADOS ---");
        List<Livro> livros = livroDao.procurarTodos();
        for (Livro l : livros) {
            System.out.println(l);
        }
        System.out.println("Total de livros cadastrados: " + livros.size());
    }

    private static Livro cadastrarLivro(Scanner sc, LivroDao livroDao) {
        System.out.println("\n--- [CREATE] CADASTRAR NOVO LIVRO ---");

        System.out.print("Título do livro: ");
        String titulo = sc.nextLine().trim();
        if (titulo.isEmpty()) {
            titulo = "Dom Quixote (Edição Especial)";
        }

        String isbnGerado = "978" + String.format("%010d", System.currentTimeMillis() % 10000000000L);
        System.out.print("ISBN de 13 dígitos [ENTER para gerar " + isbnGerado + "]: ");
        String isbn = sc.nextLine().replaceAll("\\D", "");
        if (isbn.length() != 13) {
            isbn = isbnGerado;
        }

        System.out.print("Ano de publicação [ENTER para 2026]: ");
        String anoStr = sc.nextLine().trim();
        int ano = anoStr.isEmpty() ? 2026 : Integer.parseInt(anoStr);

        System.out.print("Preço de venda (R$) [ENTER para 49.90]: ");
        String precoStr = sc.nextLine().trim().replace(",", ".");
        BigDecimal preco = precoStr.isEmpty() ? new BigDecimal("49.90") : new BigDecimal(precoStr);

        System.out.println("Categorias: 1-Romance | 2-Ficção Científica | 3-Fantasia | 4-História | 5-Tecnologia | 6-Ciência | 7-Infantil | 8-Literatura Brasileira");
        System.out.print("ID da Categoria [ENTER para 3-Fantasia]: ");
        String catStr = sc.nextLine().trim();
        int idCat = catStr.isEmpty() ? 3 : Integer.parseInt(catStr);

        Categoria cat = new Categoria(idCat, null);
        Livro novo = new Livro(null, titulo, isbn, ano, preco, cat);

        System.out.print("Autor(es), separados por vírgula [ENTER para Miguel de Cervantes]: ");
        String autoresDigitados = sc.nextLine().trim();
        if (autoresDigitados.isEmpty()) {
            autoresDigitados = "Miguel de Cervantes";
        }
        adicionarAutores(sc, livroDao, novo, autoresDigitados);

        livroDao.inserir(novo);

        Livro salvo = livroDao.procurarPorId(novo.getIdLivro());
        System.out.println("\n>> SUCESSO! Livro gravado no MySQL:");
        System.out.println("   " + salvo);
        return salvo;
    }

    private static void editarLivro(Scanner sc, LivroDao livroDao) {
        System.out.println("\n--- [UPDATE] EDITAR LIVRO EXISTENTE ---");
        System.out.print("Digite o ID do Livro que deseja editar: ");
        int id = Integer.parseInt(sc.nextLine().trim());

        Livro livro = livroDao.procurarPorId(id);
        if (livro == null) {
            System.out.println(">> Livro com ID " + id + " não encontrado no banco!");
            return;
        }

        System.out.println("Registro atual: " + livro);

        System.out.print("Novo Título [ENTER para manter \"" + livro.getTitulo() + "\"]: ");
        String novoTitulo = sc.nextLine().trim();
        if (!novoTitulo.isEmpty()) {
            livro.setTitulo(novoTitulo);
        }

        System.out.print("Novo Preço (R$) [ENTER para manter R$ " + livro.getPrecoVenda() + "]: ");
        String novoPreco = sc.nextLine().trim().replace(",", ".");
        if (!novoPreco.isEmpty()) {
            livro.setPrecoVenda(new BigDecimal(novoPreco));
        }

        System.out.print("Novo Ano [ENTER para manter " + livro.getAnoPublicacao() + "]: ");
        String novoAno = sc.nextLine().trim();
        if (!novoAno.isEmpty()) {
            livro.setAnoPublicacao(Integer.parseInt(novoAno));
        }

        System.out.print("Novo(s) Autor(es), separados por vírgula [ENTER para manter " + livro.nomesDosAutores() + "]: ");
        String novosAutores = sc.nextLine().trim();
        if (!novosAutores.isEmpty()) {
            livro.limparAutores();
            adicionarAutores(sc, livroDao, livro, novosAutores);
        }

        livroDao.atualizar(livro);
        Livro atualizado = livroDao.procurarPorId(id);
        System.out.println("\n>> SUCESSO! Livro atualizado no MySQL:");
        System.out.println("   " + atualizado);
    }

    // Coloca no livro os autores digitados ("Autor Um, Autor Dois").
    // Autor que já existe no banco é reaproveitado; autor novo pede a nacionalidade.
    private static void adicionarAutores(Scanner sc, LivroDao livroDao, Livro livro, String autoresDigitados) {
        List<Livro> livrosDoBanco = livroDao.procurarTodos();
        for (String nomeDigitado : autoresDigitados.split(",")) {
            String nome = nomeDigitado.trim();
            if (nome.isEmpty()) continue;

            Autor autorExistente = null;
            for (Livro outroLivro : livrosDoBanco) {
                for (Autor autor : outroLivro.getAutores()) {
                    if (autor.getNome().equalsIgnoreCase(nome)) autorExistente = autor;
                }
            }

            if (autorExistente != null) {
                livro.adicionarAutor(autorExistente);
            } else {
                System.out.print("Nacionalidade de " + nome + " (autor novo) [ENTER para Não informada]: ");
                String nacionalidade = sc.nextLine().trim();
                livro.adicionarAutor(new Autor(null, nome, nacionalidade.isEmpty() ? "Não informada" : nacionalidade));
            }
        }
    }

    private static void excluirLivro(Scanner sc, LivroDao livroDao) {
        System.out.println("\n--- [DELETE] EXCLUIR LIVRO POR ID ---");
        System.out.print("Digite o ID do Livro que deseja excluir: ");
        int id = Integer.parseInt(sc.nextLine().trim());

        Livro livro = livroDao.procurarPorId(id);
        if (livro == null) {
            System.out.println(">> Livro com ID " + id + " não existe no banco!");
            return;
        }

        livroDao.eliminarPorId(id);
        Livro confere = livroDao.procurarPorId(id);
        if (confere == null) {
            System.out.println(">> SUCESSO! O livro \"" + livro.getTitulo() + "\" (ID " + id + ") foi removido do MySQL.");
        }
    }

    private static void listarUsuarios(UsuarioDao usuarioDao) {
        System.out.println("\n--- [READ] LISTA DE USUÁRIOS NO BANCO DE DADOS ---");
        List<Usuario> usuarios = usuarioDao.procurarTodos();
        for (Usuario u : usuarios) {
            System.out.println(u + " | Tel: " + u.getTelefone());
        }
        System.out.println("Total de usuários cadastrados: " + usuarios.size());
    }

    private static void cadastrarUsuario(Scanner sc, UsuarioDao usuarioDao) {
        System.out.println("\n--- [CREATE] CADASTRAR NOVO USUÁRIO ---");

        System.out.print("Nome do usuário: ");
        String nome = sc.nextLine().trim();
        if (nome.isEmpty()) {
            nome = "Visitante Apresentação";
        }

        String cpfGerado = String.format("%011d", System.currentTimeMillis() % 100000000000L);
        System.out.print("CPF (11 números) [ENTER para gerar " + cpfGerado + "]: ");
        String cpf = sc.nextLine().replaceAll("\\D", "");
        if (cpf.length() != 11) {
            cpf = cpfGerado;
        }

        System.out.print("E-mail [ENTER para gerar automático]: ");
        String email = sc.nextLine().trim();
        if (email.isEmpty()) {
            email = "usuario" + (System.currentTimeMillis() % 10000) + "@email.com";
        }

        System.out.print("Telefone [ENTER para (11) 99999-0000]: ");
        String telefone = sc.nextLine().trim();
        if (telefone.isEmpty()) {
            telefone = "(11) 99999-0000";
        }

        Usuario novo = new Usuario(null, nome, cpf, email, "hash123", telefone);
        usuarioDao.inserir(novo);

        System.out.println("\n>> SUCESSO! Usuário inserido no MySQL com ID: " + novo.getIdUsuario());
        System.out.println("   " + usuarioDao.procurarPorId(novo.getIdUsuario()));
    }

    private static void editarUsuario(Scanner sc, UsuarioDao usuarioDao) {
        System.out.println("\n--- [UPDATE] EDITAR USUÁRIO EXISTENTE ---");
        System.out.print("Digite o ID do Usuário que deseja editar: ");
        int id = Integer.parseInt(sc.nextLine().trim());

        Usuario u = usuarioDao.procurarPorId(id);
        if (u == null) {
            System.out.println(">> Usuário com ID " + id + " não encontrado!");
            return;
        }

        System.out.println("Registro atual: " + u + " | Tel: " + u.getTelefone());

        System.out.print("Novo Nome [ENTER para manter \"" + u.getNome() + "\"]: ");
        String novoNome = sc.nextLine().trim();
        if (!novoNome.isEmpty()) {
            u.setNome(novoNome);
        }

        System.out.print("Novo E-mail [ENTER para manter \"" + u.getEmailUsuario() + "\"]: ");
        String novoEmail = sc.nextLine().trim();
        if (!novoEmail.isEmpty()) {
            u.setEmailUsuario(novoEmail);
        }

        System.out.print("Novo Telefone [ENTER para manter \"" + u.getTelefone() + "\"]: ");
        String novoTel = sc.nextLine().trim();
        if (!novoTel.isEmpty()) {
            u.setTelefone(novoTel);
        }

        usuarioDao.atualizar(u);
        System.out.println("\n>> SUCESSO! Usuário atualizado no MySQL:");
        System.out.println("   " + usuarioDao.procurarPorId(id));
    }

    private static void excluirUsuario(Scanner sc, UsuarioDao usuarioDao) {
        System.out.println("\n--- [DELETE] EXCLUIR USUÁRIO POR ID ---");
        System.out.print("Digite o ID do Usuário que deseja excluir: ");
        int id = Integer.parseInt(sc.nextLine().trim());

        Usuario u = usuarioDao.procurarPorId(id);
        if (u == null) {
            System.out.println(">> Usuário com ID " + id + " não encontrado!");
            return;
        }

        usuarioDao.eliminarPorId(id);
        if (usuarioDao.procurarPorId(id) == null) {
            System.out.println(">> SUCESSO! Usuário \"" + u.getNome() + "\" (ID " + id + ") removido do MySQL.");
        }
    }

    private static void executarRoteiroGuiado(Scanner sc, LivroDao livroDao) {
        System.out.println("\n==============================================================");
        System.out.println("       ROTEIRO GUIADO DE APRESENTAÇÃO (PASSO A PASSO)         ");
        System.out.println("==============================================================");

        System.out.println("\n>>> PASSO 1 de 4: INSERIR UM NOVO LIVRO (CREATE)");
        Livro criado = cadastrarLivro(sc, livroDao);
        int idGerado = criado.getIdLivro();

        System.out.print("\n[Pressione ENTER para avançar para o PASSO 2 - Leitura no Banco...]");
        sc.nextLine();

        System.out.println("\n>>> PASSO 2 de 4: BUSCAR O LIVRO RECÉM-CRIADO NO MYSQL (READ)");
        Livro buscado = livroDao.procurarPorId(idGerado);
        System.out.println("Buscando livro de ID " + idGerado + " diretamente no banco:");
        System.out.println("Resultado -> " + buscado);

        System.out.print("\n[Pressione ENTER para avançar para o PASSO 3 - Edição (UPDATE)...]");
        sc.nextLine();

        System.out.println("\n>>> PASSO 3 de 4: ATUALIZAR DADOS DO LIVRO ID " + idGerado + " (UPDATE)");
        System.out.print("Digite um novo título [ou ENTER para \"" + buscado.getTitulo() + " - 2ª Edição\"]: ");
        String nTitulo = sc.nextLine().trim();
        buscado.setTitulo(nTitulo.isEmpty() ? buscado.getTitulo() + " - 2ª Edição" : nTitulo);

        System.out.print("Digite um novo preço [ou ENTER para R$ 79.90]: ");
        String nPreco = sc.nextLine().trim().replace(",", ".");
        buscado.setPrecoVenda(nPreco.isEmpty() ? new BigDecimal("79.90") : new BigDecimal(nPreco));

        livroDao.atualizar(buscado);
        System.out.println(">> SUCESSO! Estado atualizado no MySQL:");
        System.out.println("   " + livroDao.procurarPorId(idGerado));

        System.out.print("\n[Pressione ENTER para avançar para o PASSO 4 - Exclusão (DELETE)...]");
        sc.nextLine();

        System.out.println("\n>>> PASSO 4 de 4: EXCLUIR O LIVRO ID " + idGerado + " DO BANCO (DELETE)");
        livroDao.eliminarPorId(idGerado);
        Livro aposExclusao = livroDao.procurarPorId(idGerado);
        System.out.println("Comando DELETE executado para o ID " + idGerado + "!");
        System.out.println("Verificando no MySQL (procurarPorId): " + (aposExclusao == null ? "NULL (Registro não existe mais!)" : aposExclusao));
        System.out.println("\n=== CICLO COMPLETO DO CRUD FINALIZADO COM SUCESSO! ===");
    }

    private static void mostrarDemaisDaos(
            FuncionarioDao funcionarioDao,
            ExemplarDao exemplarDao,
            EmprestimoDao emprestimoDao,
            MultaDao multaDao,
            VendaDao vendaDao) {

        System.out.println("\n--- [READ] CONSULTA INTEGRADA DOS DEMAIS DAOs ---");

        List<Funcionario> funcionarios = funcionarioDao.procurarTodos();
        System.out.println("\n> Funcionários (" + funcionarios.size() + "):");
        for (Funcionario f : funcionarios) {
            System.out.println("  - ID " + f.getIdFuncionario() + ": " + f.getNome() + " [" + f.getTipoFuncionario() + "]");
        }

        List<Exemplar> exemplares = exemplarDao.procurarTodos();
        System.out.println("\n> Exemplares Físicos no Acervo (" + exemplares.size() + " no total - exibindo os 5 primeiros):");
        for (int i = 0; i < Math.min(5, exemplares.size()); i++) {
            Exemplar ex = exemplares.get(i);
            System.out.println("  - Patrimônio " + ex.getCodigoPatrimonial() + " | Livro: " + ex.getLivro().getTitulo() + " | Status: " + ex.getStatus());
        }

        List<Emprestimo> emprestimos = emprestimoDao.procurarTodos();
        System.out.println("\n> Empréstimos Registrados (" + emprestimos.size() + "):");
        for (Emprestimo emp : emprestimos) {
            System.out.println("  - Empréstimo #" + emp.getIdEmprestimo() + " | Usuário: " + emp.getUsuario().getNome() +
                    " | Livro: " + emp.getExemplar().getLivro().getTitulo() + " | Status: " + emp.getStatus());
        }

        List<Multa> multas = multaDao.procurarTodos();
        System.out.println("\n> Multas Registradas (" + multas.size() + "):");
        for (Multa m : multas) {
            System.out.println("  - Multa #" + m.getIdMulta() + " | Valor: R$ " + m.getValor() + " | Status: " + m.getStatus());
        }

        List<Venda> vendas = vendaDao.procurarTodos();
        System.out.println("\n> Vendas Realizadas (" + vendas.size() + "):");
        for (Venda v : vendas) {
            System.out.println("  - Venda #" + v.getIdVenda() + " | Data: " + v.getDataVenda() +
                    " | Total: R$ " + v.getValorTotal() + " | Pagamento: " + v.getFormaPagamento().getTipo() +
                    " | Itens: " + v.getItens().size());
        }
    }
}