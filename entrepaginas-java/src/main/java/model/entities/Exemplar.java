package model.entities;

import java.util.Objects;

public class Exemplar {
    private Integer idExemplar;
    private String codigoPatrimonial;
    private String status;
    private String finalidade;
    private Livro livro;
    private Funcionario funcionarioCadastrou;

    public Exemplar() {}

    public Exemplar(Integer idExemplar, String codigoPatrimonial, String status, String finalidade, Livro livro, Funcionario funcionarioCadastrou) {
        this.idExemplar = idExemplar;
        this.codigoPatrimonial = codigoPatrimonial;
        this.status = status;
        this.finalidade = finalidade;
        this.livro = livro;
        this.funcionarioCadastrou = funcionarioCadastrou;
    }

    public Integer getIdExemplar() { return idExemplar; }
    public void setIdExemplar(Integer idExemplar) { this.idExemplar = idExemplar; }

    public String getCodigoPatrimonial() { return codigoPatrimonial; }
    public void setCodigoPatrimonial(String codigoPatrimonial) { this.codigoPatrimonial = codigoPatrimonial; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFinalidade() { return finalidade; }
    public void setFinalidade(String finalidade) { this.finalidade = finalidade; }

    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }

    public Funcionario getFuncionarioCadastrou() { return funcionarioCadastrou; }
    public void setFuncionarioCadastrou(Funcionario funcionarioCadastrou) { this.funcionarioCadastrou = funcionarioCadastrou; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exemplar exemplar = (Exemplar) o;
        return Objects.equals(idExemplar, exemplar.idExemplar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idExemplar);
    }

    @Override
    public String toString() {
        return "Exemplar [" + codigoPatrimonial + " - " + status + " (" + finalidade + ")]";
    }
}