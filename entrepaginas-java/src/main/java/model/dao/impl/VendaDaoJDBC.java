package model.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.dao.ExemplarDao;
import model.dao.FuncionarioDao;
import model.dao.UsuarioDao;
import model.dao.VendaDao;
import model.entities.Exemplar;
import model.entities.FormaPagamento;
import model.entities.Funcionario;
import model.entities.ItemVenda;
import model.entities.PagamentoBoleto;
import model.entities.PagamentoCartao;
import model.entities.PagamentoDinheiro;
import model.entities.PagamentoPix;
import model.entities.Usuario;
import model.entities.Venda;

public class VendaDaoJDBC implements VendaDao {

    private Connection conn;

    public VendaDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void inserir(Venda obj) {
        boolean autoCommitOriginal = true;
        try {
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO venda_grupo1 (data_venda, valor_total, id_usuario, id_forma_pagamento, id_funcionario) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                st.setDate(1, Date.valueOf(obj.getDataVenda()));
                st.setBigDecimal(2, obj.getValorTotal());
                st.setInt(3, obj.getUsuario().getIdUsuario());
                st.setInt(4, obj.getFormaPagamento().getIdFormaPagamento());
                st.setInt(5, obj.getFuncionario().getIdFuncionario());

                int linhasAfetadas = st.executeUpdate();
                if (linhasAfetadas > 0) {
                    try (ResultSet rs = st.getGeneratedKeys()) {
                        if (rs.next()) {
                            obj.setIdVenda(rs.getInt(1));
                        }
                    }

                    if (obj.getItens() != null && !obj.getItens().isEmpty()) {
                        try (PreparedStatement stItem = conn.prepareStatement(
                                "INSERT INTO itemvenda_grupo1 (preco_unitario, id_venda, id_exemplar) VALUES (?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS
                        )) {
                            for (ItemVenda item : obj.getItens()) {
                                stItem.setBigDecimal(1, item.getPrecoUnitario());
                                stItem.setInt(2, obj.getIdVenda());
                                stItem.setInt(3, item.getExemplar().getIdExemplar());
                                stItem.executeUpdate();
                                try (ResultSet rsItem = stItem.getGeneratedKeys()) {
                                    if (rsItem.next()) {
                                        item.setIdItemVenda(rsItem.getInt(1));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    throw new RuntimeException("Erro inesperado! Nenhuma linha foi inserida.");
                }
            }

            conn.commit();
        } catch (SQLException | RuntimeException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new RuntimeException("Erro ao inserir venda: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(autoCommitOriginal);
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public void atualizar(Venda obj) {
        try (PreparedStatement st = conn.prepareStatement(
                    "UPDATE venda_grupo1 SET data_venda = ?, valor_total = ?, id_usuario = ?, id_forma_pagamento = ?, id_funcionario = ? WHERE id_venda = ?"
            )) {
            st.setDate(1, Date.valueOf(obj.getDataVenda()));
            st.setBigDecimal(2, obj.getValorTotal());
            st.setInt(3, obj.getUsuario().getIdUsuario());
            st.setInt(4, obj.getFormaPagamento().getIdFormaPagamento());
            st.setInt(5, obj.getFuncionario().getIdFuncionario());
            st.setInt(6, obj.getIdVenda());

            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar venda: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement("DELETE FROM venda_grupo1 WHERE id_venda = ?")) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao eliminar venda: " + e.getMessage(), e);
        }
    }

    @Override
    public Venda procurarPorId(Integer id) {
        try (PreparedStatement st = conn.prepareStatement(
                    "SELECT v.*, fp.tipo AS tipo_pagamento " +
                            "FROM venda_grupo1 v " +
                            "INNER JOIN formapagamento_grupo1 fp ON v.id_forma_pagamento = fp.id_forma_pagamento " +
                            "WHERE v.id_venda = ?"
            )) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return instanciarVenda(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao procurar venda por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Venda> procurarTodos() {
        try (PreparedStatement st = conn.prepareStatement(
                    "SELECT v.*, fp.tipo AS tipo_pagamento " +
                            "FROM venda_grupo1 v " +
                            "INNER JOIN formapagamento_grupo1 fp ON v.id_forma_pagamento = fp.id_forma_pagamento " +
                            "ORDER BY v.data_venda DESC"
            )) {
            try (ResultSet rs = st.executeQuery()) {
                List<Venda> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(instanciarVenda(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar vendas: " + e.getMessage(), e);
        }
    }

    private Venda instanciarVenda(ResultSet rs) throws SQLException {
        UsuarioDao usuarioDao = new UsuarioDaoJDBC(conn);
        FuncionarioDao funcionarioDao = new FuncionarioDaoJDBC(conn);
        ExemplarDao exemplarDao = new ExemplarDaoJDBC(conn);

        Usuario usu = usuarioDao.procurarPorId(rs.getInt("id_usuario"));
        Funcionario func = funcionarioDao.procurarPorId(rs.getInt("id_funcionario"));
        FormaPagamento fp = instanciarFormaPagamento(rs.getInt("id_forma_pagamento"), rs.getString("tipo_pagamento"));

        Venda venda = new Venda(
                rs.getInt("id_venda"),
                rs.getDate("data_venda").toLocalDate(),
                rs.getBigDecimal("valor_total"),
                usu,
                fp,
                func
        );

        try (PreparedStatement stItens = conn.prepareStatement("SELECT * FROM itemvenda_grupo1 WHERE id_venda = ?")) {
            stItens.setInt(1, venda.getIdVenda());
            try (ResultSet rsItens = stItens.executeQuery()) {
                while (rsItens.next()) {
                    Exemplar ex = exemplarDao.procurarPorId(rsItens.getInt("id_exemplar"));
                    ItemVenda item = new ItemVenda(
                            rsItens.getInt("id_item_venda"),
                            rsItens.getBigDecimal("preco_unitario"),
                            ex
                    );
                    venda.adicionarItem(item);
                }
            }
        }

        return venda;
    }

    private FormaPagamento instanciarFormaPagamento(int id, String tipo) {
        if ("Pix".equalsIgnoreCase(tipo)) {
            return new PagamentoPix(id);
        } else if ("Boleto".equalsIgnoreCase(tipo)) {
            return new PagamentoBoleto(id);
        } else if ("Dinheiro".equalsIgnoreCase(tipo)) {
            return new PagamentoDinheiro(id, tipo);
        } else {
            return new PagamentoCartao(id, tipo);
        }
    }
}