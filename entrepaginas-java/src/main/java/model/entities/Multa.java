package model.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Multa {
    private Integer idMulta;
    private BigDecimal valor;
    private LocalDate dataGeracao;
    private LocalDate dataPagamento;
    private String status;
    private Emprestimo emprestimo;

    public Multa() {}

    public Multa(Integer idMulta, BigDecimal valor, LocalDate dataGeracao, LocalDate dataPagamento, String status, Emprestimo emprestimo) {
        this.idMulta = idMulta;
        this.valor = valor;
        this.dataGeracao = dataGeracao;
        this.dataPagamento = dataPagamento;
        this.status = status;
        this.emprestimo = emprestimo;
    }

    public Integer getIdMulta() { return idMulta; }
    public void setIdMulta(Integer idMulta) { this.idMulta = idMulta; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getDataGeracao() { return dataGeracao; }
    public void setDataGeracao(LocalDate dataGeracao) { this.dataGeracao = dataGeracao; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Emprestimo getEmprestimo() { return emprestimo; }
    public void setEmprestimo(Emprestimo emprestimo) { this.emprestimo = emprestimo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Multa multa = (Multa) o;
        return Objects.equals(idMulta, multa.idMulta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMulta);
    }
}