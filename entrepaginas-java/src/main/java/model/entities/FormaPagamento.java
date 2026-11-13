package model.entities;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class FormaPagamento {
    private Integer idFormaPagamento;
    private String tipo;

    public FormaPagamento() {}

    public FormaPagamento(Integer idFormaPagamento, String tipo) {
        this.idFormaPagamento = idFormaPagamento;
        this.tipo = tipo;
    }


    public abstract void processarPagamento(BigDecimal valor);

    public Integer getIdFormaPagamento() { return idFormaPagamento; }
    public void setIdFormaPagamento(Integer idFormaPagamento) { this.idFormaPagamento = idFormaPagamento; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormaPagamento that = (FormaPagamento) o;
        return Objects.equals(idFormaPagamento, that.idFormaPagamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFormaPagamento);
    }

    @Override
    public String toString() {
        return "Forma de Pagamento: " + tipo;
    }
}