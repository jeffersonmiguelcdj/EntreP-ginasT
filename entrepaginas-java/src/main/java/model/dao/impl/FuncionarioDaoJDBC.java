package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.dao.FuncionarioDao;
import model.entities.Admin;
import model.entities.Atendente;
import model.entities.Funcionario;

public class FuncionarioDaoJDBC implements FuncionarioDao {

    private Connection conn;

    public FuncionarioDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void inserir(Funcionario obj) {
        try (PreparedStatement st = conn.prepareStatement(
                "INSERT INTO funcionario_grupo1 (nome, cpf, email, senha_hash, tipo_funcionario) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            st.setString(1, obj.getNome());
            st.setString(2, obj.getCpf());
            st.setString(3, obj.getEmail());
            st.setString(4, obj.getSenhaHash());
            st.setString(5, obj.getTipoFuncionario());

            int linhasAfetadas = st.executeUpdate();
            if (linhasAfetadas > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        obj.setIdFuncionario(rs.getInt(1));
                    }
                }

                if (obj instanceof Admin) {
                    Admin admin = (Admin) obj;
                    try (PreparedStatement stSub = conn.prepareStatement("INSERT INTO admin_grupo1 (id_funcionario, nivel_acesso) VALUES (?, ?)")) {
                        stSub.setInt(1, admin.getIdFuncionario());
                        stSub.setString(2, admin.getNivelAcesso() != null ? admin.getNivelAcesso() : "Total");
                        stSub.executeUpdate();
                    }
                } else if (obj instanceof Atendente) {
                    try (PreparedStatement stSub = conn.prepareStatement("INSERT INTO atendente_grupo1 (id_funcionario) VALUES (?)")) {
                        stSub.setInt(1, obj.getIdFuncionario());
                        stSub.executeUpdate();
                    }
                }
            } else {
                throw new RuntimeException("Erro inesperado! Nenhuma linha foi inserida.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir funcionário: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizar(Funcionario obj) {
        try (PreparedStatement st = conn.prepareStatement(
                "UPDATE funcionario_grupo1 SET nome = ?, cpf = ?, email = ?, senha_hash = ?, tipo_funcionario = ? WHERE id_funcionario = ?"
        )) {
            st.setString(1, obj.getNome());
            st.setString(2, obj.getCpf());
            st.setString(3, obj.getEmail());
            st.setString(4, obj.getSenhaHash());
            st.setString(5, obj.getTipoFuncionario());
            st.setInt(6, obj.getIdFuncionario());
            st.executeUpdate();

            if (obj instanceof Admin) {
                Admin admin = (Admin) obj;
                try (PreparedStatement stSub = conn.prepareStatement("UPDATE admin_grupo1 SET nivel_acesso = ? WHERE id_funcionario = ?")) {
                    stSub.setString(1, admin.getNivelAcesso());
                    stSub.setInt(2, admin.getIdFuncionario());
                    stSub.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar funcionário: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM funcionario_grupo1 WHERE id_funcionario = ?")) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao eliminar funcionário: " + e.getMessage(), e);
        }
    }

    @Override
    public Funcionario procurarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement(
                    "SELECT f.*, a.nivel_acesso " +
                            "FROM funcionario_grupo1 f " +
                            "LEFT JOIN admin_grupo1 a ON f.id_funcionario = a.id_funcionario " +
                            "WHERE f.id_funcionario = ?"
            )) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return instanciarFuncionario(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar funcionário por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Funcionario> procurarTodos() {
        try (PreparedStatement st = conn.prepareStatement(
                    "SELECT f.*, a.nivel_acesso " +
                            "FROM funcionario_grupo1 f " +
                            "LEFT JOIN admin_grupo1 a ON f.id_funcionario = a.id_funcionario " +
                            "ORDER BY f.nome"
            )) {
            try (ResultSet rs = st.executeQuery()) {
                List<Funcionario> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(instanciarFuncionario(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar funcionários: " + e.getMessage(), e);
        }
    }

    private Funcionario instanciarFuncionario(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo_funcionario");
        if ("Admin".equalsIgnoreCase(tipo)) {
            return new Admin(
                    rs.getInt("id_funcionario"),
                    rs.getString("nome"),
                    rs.getString("cpf"),
                    rs.getString("email"),
                    rs.getString("senha_hash"),
                    rs.getString("nivel_acesso")
            );
        } else {
            return new Atendente(
                    rs.getInt("id_funcionario"),
                    rs.getString("nome"),
                    rs.getString("cpf"),
                    rs.getString("email"),
                    rs.getString("senha_hash")
            );
        }
    }
}