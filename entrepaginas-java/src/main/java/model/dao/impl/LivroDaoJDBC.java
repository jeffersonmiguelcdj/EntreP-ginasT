package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.dao.LivroDao;
import model.entities.Autor;
import model.entities.Categoria;
import model.entities.Livro;

public class LivroDaoJDBC implements LivroDao {

    private Connection conn;

    public LivroDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void inserir(Livro obj) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn.setAutoCommit(true);
            st = conn.prepareStatement(
                    "INSERT INTO livro_grupo1 (titulo, isbn, ano_publicacao, preco_venda, id_categoria) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            st.setString(1, obj.getTitulo());
            st.setString(2, obj.getIsbn());
            st.setInt(3, obj.getAnoPublicacao());
            st.setBigDecimal(4, obj.getPrecoVenda());
            st.setInt(5, obj.getCategoria().getIdCategoria());

            int linhasAfetadas = st.executeUpdate();

            if (linhasAfetadas > 0) {
                rs = st.getGeneratedKeys();
                if (rs.next()) {
                    obj.setIdLivro(rs.getInt(1));
                }
                gravarAutores(obj);
            } else {
                throw new RuntimeException("Erro inesperado! Nenhuma linha foi inserida.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir livro: " + e.getMessage(), e);
        } finally {
            fecharRecursos(st, rs);
        }
    }

    @Override
    public void atualizar(Livro obj) {
        PreparedStatement st = null;
        try {
            conn.setAutoCommit(true);
            st = conn.prepareStatement(
                    "UPDATE livro_grupo1 SET titulo = ?, isbn = ?, ano_publicacao = ?, preco_venda = ?, id_categoria = ? WHERE id_livro = ?"
            );
            st.setString(1, obj.getTitulo());
            st.setString(2, obj.getIsbn());
            st.setInt(3, obj.getAnoPublicacao());
            st.setBigDecimal(4, obj.getPrecoVenda());
            st.setInt(5, obj.getCategoria().getIdCategoria());
            st.setInt(6, obj.getIdLivro());

            st.executeUpdate();
            gravarAutores(obj);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar livro: " + e.getMessage(), e);
        } finally {
            fecharRecursos(st, null);
        }
    }

    @Override
    public void eliminarPorId(Integer id) {
        PreparedStatement st = null;
        try {
            conn.setAutoCommit(true);

            executarDeleteAuxiliar("DELETE FROM multa_grupo1 WHERE id_emprestimo IN (SELECT id_emprestimo FROM emprestimo_grupo1 WHERE id_exemplar IN (SELECT id_exemplar FROM exemplar_grupo1 WHERE id_livro = ?))", id);
            executarDeleteAuxiliar("DELETE FROM emprestimo_grupo1 WHERE id_exemplar IN (SELECT id_exemplar FROM exemplar_grupo1 WHERE id_livro = ?)", id);
            executarDeleteAuxiliar("DELETE FROM itemvenda_grupo1 WHERE id_exemplar IN (SELECT id_exemplar FROM exemplar_grupo1 WHERE id_livro = ?)", id);
            executarDeleteAuxiliar("DELETE FROM exemplar_grupo1 WHERE id_livro = ?", id);
            executarDeleteAuxiliar("DELETE FROM livro_autor_grupo1 WHERE id_livro = ?", id);

            st = conn.prepareStatement("DELETE FROM livro_grupo1 WHERE id_livro = ?");
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao eliminar livro: " + e.getMessage(), e);
        } finally {
            fecharRecursos(st, null);
        }
    }

    private void executarDeleteAuxiliar(String sql, Integer idLivro) throws SQLException {
        try (PreparedStatement stAux = conn.prepareStatement(sql)) {
            stAux.setInt(1, idLivro);
            stAux.executeUpdate();
        }
    }

    @Override
    public Livro procurarPorId(Integer id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT l.*, c.nome AS nome_categoria, a.id_autor, a.nome AS nome_autor, a.nacionalidade " +
                            "FROM livro_grupo1 l " +
                            "INNER JOIN categoria_grupo1 c ON l.id_categoria = c.id_categoria " +
                            "LEFT JOIN livro_autor_grupo1 la ON l.id_livro = la.id_livro " +
                            "LEFT JOIN autor_grupo1 a ON la.id_autor = a.id_autor " +
                            "WHERE l.id_livro = ?"
            );
            st.setInt(1, id);
            rs = st.executeQuery();

            Livro livro = null;
            Map<Integer, Categoria> mapCat = new HashMap<>();

            while (rs.next()) {
                if (livro == null) {
                    Categoria cat = mapCat.get(rs.getInt("id_categoria"));
                    if (cat == null) {
                        cat = instanciarCategoria(rs);
                        mapCat.put(rs.getInt("id_categoria"), cat);
                    }
                    livro = instanciarLivro(rs, cat);
                }
                int idAutor = rs.getInt("id_autor");
                if (!rs.wasNull()) {
                    Autor autor = instanciarAutor(rs);
                    livro.adicionarAutor(autor);
                }
            }
            return livro;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar livro por ID: " + e.getMessage(), e);
        } finally {
            fecharRecursos(st, rs);
        }
    }

    @Override
    public List<Livro> procurarTodos() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(
                    "SELECT l.*, c.nome AS nome_categoria, a.id_autor, a.nome AS nome_autor, a.nacionalidade " +
                            "FROM livro_grupo1 l " +
                            "INNER JOIN categoria_grupo1 c ON l.id_categoria = c.id_categoria " +
                            "LEFT JOIN livro_autor_grupo1 la ON l.id_livro = la.id_livro " +
                            "LEFT JOIN autor_grupo1 a ON la.id_autor = a.id_autor " +
                            "ORDER BY l.id_livro"
            );
            rs = st.executeQuery();

            List<Livro> lista = new ArrayList<>();
            Map<Integer, Livro> mapLivro = new HashMap<>();
            Map<Integer, Categoria> mapCat = new HashMap<>();

            while (rs.next()) {
                int idLivro = rs.getInt("id_livro");
                Livro livro = mapLivro.get(idLivro);

                if (livro == null) {
                    Categoria cat = mapCat.get(rs.getInt("id_categoria"));
                    if (cat == null) {
                        cat = instanciarCategoria(rs);
                        mapCat.put(rs.getInt("id_categoria"), cat);
                    }
                    livro = instanciarLivro(rs, cat);
                    mapLivro.put(idLivro, livro);
                    lista.add(livro);
                }

                int idAutor = rs.getInt("id_autor");
                if (!rs.wasNull()) {
                    Autor autor = instanciarAutor(rs);
                    livro.adicionarAutor(autor);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar livros: " + e.getMessage(), e);
        } finally {
            fecharRecursos(st, rs);
        }
    }

    private Categoria instanciarCategoria(ResultSet rs) throws SQLException {
        return new Categoria(rs.getInt("id_categoria"), rs.getString("nome_categoria"));
    }

    private Livro instanciarLivro(ResultSet rs, Categoria cat) throws SQLException {
        return new Livro(
                rs.getInt("id_livro"),
                rs.getString("titulo"),
                rs.getString("isbn"),
                rs.getInt("ano_publicacao"),
                rs.getBigDecimal("preco_venda"),
                cat
        );
    }

    private Autor instanciarAutor(ResultSet rs) throws SQLException {
        return new Autor(
                rs.getInt("id_autor"),
                rs.getString("nome_autor"),
                rs.getString("nacionalidade")
        );
    }

    private void fecharRecursos(PreparedStatement st, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (st != null) {
            try { st.close(); } catch (SQLException ignored) {}
        }
    }

    private void gravarAutores(Livro livro) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM livro_autor_grupo1 WHERE id_livro = ?")) {
            st.setInt(1, livro.getIdLivro());
            st.executeUpdate();
        }
        for (Autor autor : livro.getAutores()) {
            if (autor.getIdAutor() == null) {
                try (PreparedStatement st = conn.prepareStatement(
                        "INSERT INTO autor_grupo1 (nome, nacionalidade) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                )) {
                    st.setString(1, autor.getNome());
                    st.setString(2, autor.getNacionalidade());
                    st.executeUpdate();
                    try (ResultSet rs = st.getGeneratedKeys()) {
                        if (rs.next()) {
                            autor.setIdAutor(rs.getInt(1));
                        }
                    }
                }
            }
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO livro_autor_grupo1 (id_livro, id_autor) VALUES (?, ?)")) {
                st.setInt(1, livro.getIdLivro());
                st.setInt(2, autor.getIdAutor());
                st.executeUpdate();
            }
        }
    }
}