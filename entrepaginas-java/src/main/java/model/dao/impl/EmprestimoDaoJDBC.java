package model.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import model.dao.EmprestimoDao;
import model.entities.Admin;
import model.entities.Atendente;
import model.entities.Emprestimo;
import model.entities.Exemplar;
import model.entities.Funcionario;
import model.entities.Usuario;

public class EmprestimoDaoJDBC implements EmprestimoDao {

    private Connection conn;

    public EmprestimoDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void inserir(Emprestimo obj) {
        try (PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO emprestimo_grupo1 (data_emprestimo, data_prevista_devolucao, data_devolucao, status, id_usuario, id_exemplar, id_funcionario) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
            st.setDate(1, Date.valueOf(obj.getDataEmprestimo()));
            st.setDate(2, Date.valueOf(obj.getDataPrevistaDevolucao()));
            if (obj.getDataDevolucao() != null) {
                st.setDate(3, Date.valueOf(obj.getDataDevolucao()));
            } else {
                st.setNull(3, Types.DATE);
            }
            st.setString(4, obj.getStatus());
            st.setInt(5, obj.getUsuario().getIdUsuario());
            st.setInt(6, obj.getExemplar().getIdExemplar());
            st.setInt(7, obj.getFuncionario().getIdFuncionario());

            int linhasAfetadas = st.executeUpdate();
            if (linhasAfetadas > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        obj.setIdEmprestimo(rs.getInt(1));
                    }
                }
            } else {
                throw new RuntimeException("Erro inesperado! Nenhuma linha foi inserida.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir empréstimo: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizar(Emprestimo obj) {
        try (PreparedStatement st = conn.prepareStatement(
                    "UPDATE emprestimo_grupo1 SET data_emprestimo = ?, data_prevista_devolucao = ?, data_devolucao = ?, status = ?, id_usuario = ?, id_exemplar = ?, id_funcionario = ? " +
                            "WHERE id_emprestimo = ?"
            )) {
            st.setDate(1, Date.valueOf(obj.getDataEmprestimo()));
            st.setDate(2, Date.valueOf(obj.getDataPrevistaDevolucao()));
            if (obj.getDataDevolucao() != null) {
                st.setDate(3, Date.valueOf(obj.getDataDevolucao()));
            } else {
                st.setNull(3, Types.DATE);
            }
            st.setString(4, obj.getStatus());
            st.setInt(5, obj.getUsuario().getIdUsuario());
            st.setInt(6, obj.getExemplar().getIdExemplar());
            st.setInt(7, obj.getFuncionario().getIdFuncionario());
            st.setInt(8, obj.getIdEmprestimo());

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar empréstimo: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM emprestimo_grupo1 WHERE id_emprestimo = ?")) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao eliminar empréstimo: " + e.getMessage(), e);
        }
    }

    @Override
    public Emprestimo procurarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement(
                    "SELECT emp.*, " +
                            "u.nome AS nome_usu, u.cpf AS cpf_usu, u.email_usuario, u.senha_hash AS senha_usu, u.telefone, " +
                            "ex.codigo_patrimonial, ex.status AS status_ex, ex.finalidade, ex.id_livro, " +
                            "l.titulo, l.isbn, l.ano_publicacao, l.preco_venda, l.id_categoria, c.nome AS nome_categoria, " +
                            "f.nome AS nome_func, f.cpf AS cpf_func, f.email AS email_func, f.senha_hash AS senha_func, f.tipo_funcionario, a.nivel_acesso " +
                            "FROM emprestimo_grupo1 emp " +
                            "INNER JOIN usuario_grupo1 u ON emp.id_usuario = u.id_usuario " +
                            "INNER JOIN exemplar_grupo1 ex ON emp.id_exemplar = ex.id_exemplar " +
                            "INNER JOIN livro_grupo1 l ON ex.id_livro = l.id_livro " +
                            "INNER JOIN categoria_grupo1 c ON l.id_categoria = c.id_categoria " +
                            "INNER JOIN funcionario_grupo1 f ON emp.id_funcionario = f.id_funcionario " +
                            "LEFT JOIN admin_grupo1 a ON f.id_funcionario = a.id_funcionario " +
                            "WHERE emp.id_emprestimo = ?"
            )) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return instanciarEmprestimo(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar empréstimo por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Emprestimo> procurarTodos() {
        try (PreparedStatement st = conn.prepareStatement(
                    "SELECT emp.*, " +
                            "u.nome AS nome_usu, u.cpf AS cpf_usu, u.email_usuario, u.senha_hash AS senha_usu, u.telefone, " +
                            "ex.codigo_patrimonial, ex.status AS status_ex, ex.finalidade, ex.id_livro, " +
                            "l.titulo, l.isbn, l.ano_publicacao, l.preco_venda, l.id_categoria, c.nome AS nome_categoria, " +
                            "f.nome AS nome_func, f.cpf AS cpf_func, f.email AS email_func, f.senha_hash AS senha_func, f.tipo_funcionario, a.nivel_acesso " +
                            "FROM emprestimo_grupo1 emp " +
                            "INNER JOIN usuario_grupo1 u ON emp.id_usuario = u.id_usuario " +
                            "INNER JOIN exemplar_grupo1 ex ON emp.id_exemplar = ex.id_exemplar " +
                            "INNER JOIN livro_grupo1 l ON ex.id_livro = l.id_livro " +
                            "INNER JOIN categoria_grupo1 c ON l.id_categoria = c.id_categoria " +
                            "INNER JOIN funcionario_grupo1 f ON emp.id_funcionario = f.id_funcionario " +
                            "LEFT JOIN admin_grupo1 a ON f.id_funcionario = a.id_funcionario " +
                            "ORDER BY emp.data_emprestimo DESC"
            )) {
            try (ResultSet rs = st.executeQuery()) {
                List<Emprestimo> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(instanciarEmprestimo(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar empréstimos: " + e.getMessage(), e);
        }
    }

    private Emprestimo instanciarEmprestimo(ResultSet rs) throws SQLException {
        Usuario usu = new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nome_usu"),
                rs.getString("cpf_usu"),
                rs.getString("email_usuario"),
                rs.getString("senha_usu"),
                rs.getString("telefone")
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

        Exemplar ex = new ExemplarDaoJDBC(conn).procurarPorId(rs.getInt("id_exemplar"));

        Date dataDev = rs.getDate("data_devolucao");

        return new Emprestimo(
                rs.getInt("id_emprestimo"),
                rs.getDate("data_emprestimo").toLocalDate(),
                rs.getDate("data_prevista_devolucao").toLocalDate(),
                dataDev != null ? dataDev.toLocalDate() : null,
                rs.getString("status"),
                usu,
                ex,
                func
        );
    }
}