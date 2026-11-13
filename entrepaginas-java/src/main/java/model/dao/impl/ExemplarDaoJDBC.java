package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.dao.ExemplarDao;
import model.entities.Admin;
import model.entities.Atendente;
import model.entities.Categoria;
import model.entities.Exemplar;
import model.entities.Funcionario;
import model.entities.Livro;

public class ExemplarDaoJDBC implements ExemplarDao {

    private Connection conn;

    public ExemplarDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void inserir(Exemplar obj) {
        try (PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO exemplar_grupo1 (codigo_patrimonial, status, finalidade, id_livro, id_funcionario) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
            st.setString(1, obj.getCodigoPatrimonial());
            st.setString(2, obj.getStatus());
            st.setString(3, obj.getFinalidade());
            st.setInt(4, obj.getLivro().getIdLivro());
            st.setInt(5, obj.getFuncionarioCadastrou().getIdFuncionario());

            int linhasAfetadas = st.executeUpdate();
            if (linhasAfetadas > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        obj.setIdExemplar(rs.getInt(1));
                    }
                }
            } else {
                throw new RuntimeException("Erro inesperado! Nenhuma linha foi inserida.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir exemplar: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizar(Exemplar obj) {
        try (PreparedStatement st = conn.prepareStatement(
                    "UPDATE exemplar_grupo1 SET codigo_patrimonial = ?, status = ?, finalidade = ?, id_livro = ?, id_funcionario = ? WHERE id_exemplar = ?"
            )) {
            st.setString(1, obj.getCodigoPatrimonial());
            st.setString(2, obj.getStatus());
            st.setString(3, obj.getFinalidade());
            st.setInt(4, obj.getLivro().getIdLivro());
            st.setInt(5, obj.getFuncionarioCadastrou().getIdFuncionario());
            st.setInt(6, obj.getIdExemplar());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar exemplar: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM exemplar_grupo1 WHERE id_exemplar = ?")) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao eliminar exemplar: " + e.getMessage(), e);
        }
    }

    @Override
    public Exemplar procurarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement(
                    "SELECT e.*, " +
                            "l.titulo, l.isbn, l.ano_publicacao, l.preco_venda, l.id_categoria, c.nome AS nome_categoria, " +
                            "f.nome AS nome_func, f.cpf AS cpf_func, f.email AS email_func, f.senha_hash AS senha_func, f.tipo_funcionario, a.nivel_acesso " +
                            "FROM exemplar_grupo1 e " +
                            "INNER JOIN livro_grupo1 l ON e.id_livro = l.id_livro " +
                            "INNER JOIN categoria_grupo1 c ON l.id_categoria = c.id_categoria " +
                            "INNER JOIN funcionario_grupo1 f ON e.id_funcionario = f.id_funcionario " +
                            "LEFT JOIN admin_grupo1 a ON f.id_funcionario = a.id_funcionario " +
                            "WHERE e.id_exemplar = ?"
            )) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return instanciarExemplar(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar exemplar por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Exemplar> procurarTodos() {
        try (PreparedStatement st = conn.prepareStatement(
                    "SELECT e.*, " +
                            "l.titulo, l.isbn, l.ano_publicacao, l.preco_venda, l.id_categoria, c.nome AS nome_categoria, " +
                            "f.nome AS nome_func, f.cpf AS cpf_func, f.email AS email_func, f.senha_hash AS senha_func, f.tipo_funcionario, a.nivel_acesso " +
                            "FROM exemplar_grupo1 e " +
                            "INNER JOIN livro_grupo1 l ON e.id_livro = l.id_livro " +
                            "INNER JOIN categoria_grupo1 c ON l.id_categoria = c.id_categoria " +
                            "INNER JOIN funcionario_grupo1 f ON e.id_funcionario = f.id_funcionario " +
                            "LEFT JOIN admin_grupo1 a ON f.id_funcionario = a.id_funcionario " +
                            "ORDER BY e.codigo_patrimonial"
            )) {
            try (ResultSet rs = st.executeQuery()) {
                List<Exemplar> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(instanciarExemplar(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar exemplares: " + e.getMessage(), e);
        }
    }

    private Exemplar instanciarExemplar(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria(rs.getInt("id_categoria"), rs.getString("nome_categoria"));
        Livro livro = new Livro(
                rs.getInt("id_livro"),
                rs.getString("titulo"),
                rs.getString("isbn"),
                rs.getInt("ano_publicacao"),
                rs.getBigDecimal("preco_venda"),
                cat
        );

        Funcionario func;
        if ("Admin".equalsIgnoreCase(rs.getString("tipo_funcionario"))) {
            func = new Admin(
                    rs.getInt("id_funcionario"),
                    rs.getString("nome_func"),
                    rs.getString("cpf_func"),
                    rs.getString("email_func"),
                    rs.getString("senha_func"),
                    rs.getString("nivel_acesso")
            );
        } else {
            func = new Atendente(
                    rs.getInt("id_funcionario"),
                    rs.getString("nome_func"),
                    rs.getString("cpf_func"),
                    rs.getString("email_func"),
                    rs.getString("senha_func")
            );
        }

        return new Exemplar(
                rs.getInt("id_exemplar"),
                rs.getString("codigo_patrimonial"),
                rs.getString("status"),
                rs.getString("finalidade"),
                livro,
                func
        );
    }
}