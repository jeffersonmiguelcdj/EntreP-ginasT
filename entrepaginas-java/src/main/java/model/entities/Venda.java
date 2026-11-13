package model.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Venda {
    private Integer idVenda;
    private LocalDate dataVenda;
    private BigDecimal valorTotal;
    private Usuario usuario;
    private FormaPagamento formaPagamento;
    private Funcionario funcionario;
    private List<ItemVenda> itens = new ArrayList<>();

    public Venda() {}

    public Venda(Integer idVenda, LocalDate dataVenda, BigDecimal valorTotal, Usuario usuario, FormaPagamento formaPagamento, Funcionario funcionario) {
        this.idVenda = idVenda;
        this.dataVenda = dataVenda;
        this.valorTotal = valorTotal;
        this.usuario = usuario;
        this.formaPagamento = formaPagamento;
        this.funcionario = funcionario;
    }

    public void adicionarItem(ItemVenda item) {
        itens.add(item);
    }

    public Integer getIdVenda() { return idVenda; }
    public void setIdVenda(Integer idVenda) { this.idVenda = idVenda; }

    public LocalDate getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDate dataVenda) { this.dataVenda = dataVenda; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }

    public List<ItemVenda> getItens() { return itens; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Venda venda = (Venda) o;
        return Objects.equals(idVenda, venda.idVenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idVenda);
    }
}