package model.entities;

import java.math.BigDecimal;
import java.util.Objects;

public class ItemVenda {
    private Integer idItemVenda;
    private BigDecimal precoUnitario;
    private Exemplar exemplar;

    public ItemVenda() {}

    public ItemVenda(Integer idItemVenda, BigDecimal precoUnitario, Exemplar exemplar) {
        this.idItemVenda = idItemVenda;
        this.precoUnitario = precoUnitario;
        this.exemplar = exemplar;
    }

    public Integer getIdItemVenda() { return idItemVenda; }
    public void setIdItemVenda(Integer idItemVenda) { this.idItemVenda = idItemVenda; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public Exemplar getExemplar() { return exemplar; }
    public void setExemplar(Exemplar exemplar) { this.exemplar = exemplar; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemVenda itemVenda = (ItemVenda) o;
        return Objects.equals(idItemVenda, itemVenda.idItemVenda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idItemVenda);
    }
}