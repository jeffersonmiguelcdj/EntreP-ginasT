package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.dao.UsuarioDao;
import model.entities.Usuario;

public class UsuarioDaoJDBC implements UsuarioDao {

    private Connection conn;

    public UsuarioDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void inserir(Usuario obj) {
        try (PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO usuario_grupo1 (nome, cpf, email_usuario, senha_hash, telefone) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
            st.setString(1, obj.getNome());
            st.setString(2, obj.getCpf());
            st.setString(3, obj.getEmailUsuario());
            st.setString(4, obj.getSenhaHash());
            st.setString(5, obj.getTelefone());

            int linhasAfetadas = st.executeUpdate();
            if (linhasAfetadas > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        obj.setIdUsuario(rs.getInt(1));
                    }
                }
            } else {
                throw new RuntimeException("Erro inesperado! Nenhuma linha foi inserida.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir usuário: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizar(Usuario obj) {
        try (PreparedStatement st = conn.prepareStatement(
                    "UPDATE usuario_grupo1 SET nome = ?, cpf = ?, email_usuario = ?, senha_hash = ?, telefone = ? WHERE id_usuario = ?"
            )) {
            st.setString(1, obj.getNome());
            st.setString(2, obj.getCpf());
            st.setString(3, obj.getEmailUsuario());
            st.setString(4, obj.getSenhaHash());
            st.setString(5, obj.getTelefone());
            st.setInt(6, obj.getIdUsuario());

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM usuario_grupo1 WHERE id_usuario = ?")) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao eliminar usuário: " + e.getMessage(), e);
        }
    }

    @Override
    public Usuario procurarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM usuario_grupo1 WHERE id_usuario = ?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return instanciarUsuario(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar usuário por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> procurarTodos() {
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM usuario_grupo1 ORDER BY nome")) {
            try (ResultSet rs = st.executeQuery()) {
                List<Usuario> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(instanciarUsuario(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar usuários: " + e.getMessage(), e);
        }
    }

    private Usuario instanciarUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("email_usuario"),
                rs.getString("senha_hash"),
                rs.getString("telefone")
        );
    }
}