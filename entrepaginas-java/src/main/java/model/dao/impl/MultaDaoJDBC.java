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
import model.dao.MultaDao;
import model.entities.Emprestimo;
import model.entities.Multa;

public class MultaDaoJDBC implements MultaDao {

    private Connection conn;

    public MultaDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void inserir(Multa obj) {
        try (PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO multa_grupo1 (valor, data_geracao, data_pagamento, status, id_emprestimo) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
            st.setBigDecimal(1, obj.getValor());
            st.setDate(2, Date.valueOf(obj.getDataGeracao()));
            if (obj.getDataPagamento() != null) {
                st.setDate(3, Date.valueOf(obj.getDataPagamento()));
            } else {
                st.setNull(3, Types.DATE);
            }
            st.setString(4, obj.getStatus());
            st.setInt(5, obj.getEmprestimo().getIdEmprestimo());

            int linhasAfetadas = st.executeUpdate();
            if (linhasAfetadas > 0) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    if (rs.next()) {
                        obj.setIdMulta(rs.getInt(1));
                    }
                }
            } else {
                throw new RuntimeException("Erro inesperado! Nenhuma linha foi inserida.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir multa: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizar(Multa obj) {
        try (PreparedStatement st = conn.prepareStatement(
                    "UPDATE multa_grupo1 SET valor = ?, data_geracao = ?, data_pagamento = ?, status = ?, id_emprestimo = ? WHERE id_multa = ?"
            )) {
            st.setBigDecimal(1, obj.getValor());
            st.setDate(2, Date.valueOf(obj.getDataGeracao()));
            if (obj.getDataPagamento() != null) {
                st.setDate(3, Date.valueOf(obj.getDataPagamento()));
            } else {
                st.setNull(3, Types.DATE);
            }
            st.setString(4, obj.getStatus());
            st.setInt(5, obj.getEmprestimo().getIdEmprestimo());
            st.setInt(6, obj.getIdMulta());

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar multa: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM multa_grupo1 WHERE id_multa = ?")) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao eliminar multa: " + e.getMessage(), e);
        }
    }

    @Override
    public Multa procurarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM multa_grupo1 WHERE id_multa = ?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return instanciarMulta(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar multa por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Multa> procurarTodos() {
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM multa_grupo1 ORDER BY data_geracao DESC")) {
            try (ResultSet rs = st.executeQuery()) {
                List<Multa> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(instanciarMulta(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar multas: " + e.getMessage(), e);
        }
    }

    private Multa instanciarMulta(ResultSet rs) throws SQLException {
        EmprestimoDao emprestimoDao = new EmprestimoDaoJDBC(conn);
        Emprestimo emp = emprestimoDao.procurarPorId(rs.getInt("id_emprestimo"));
        Date dataPag = rs.getDate("data_pagamento");

        return new Multa(
                rs.getInt("id_multa"),
                rs.getBigDecimal("valor"),
                rs.getDate("data_geracao").toLocalDate(),
                dataPag != null ? dataPag.toLocalDate() : null,
                rs.getString("status"),
                emp
        );
    }
}