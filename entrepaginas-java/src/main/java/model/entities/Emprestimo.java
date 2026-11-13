package model.entities;

import java.time.LocalDate;
import java.util.Objects;

public class Emprestimo {
    private Integer idEmprestimo;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevistaDevolucao;
    private LocalDate dataDevolucao;
    private String status; // "Em Andamento", "Devolvido", "Atrasado"
    private Usuario usuario;
    private Exemplar exemplar;
    private Funcionario funcionario;

    public Emprestimo() {}

    public Emprestimo(Integer idEmprestimo, LocalDate dataEmprestimo, LocalDate dataPrevistaDevolucao, LocalDate dataDevolucao, String status, Usuario usuario, Exemplar exemplar, Funcionario funcionario) {
        this.idEmprestimo = idEmprestimo;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
        this.dataDevolucao = dataDevolucao;
        this.status = status;
        this.usuario = usuario;
        this.exemplar = exemplar;
        this.funcionario = funcionario;
    }

    public Integer getIdEmprestimo() { return idEmprestimo; }
    public void setIdEmprestimo(Integer idEmprestimo) { this.idEmprestimo = idEmprestimo; }

    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public void setDataEmprestimo(LocalDate dataEmprestimo) { this.dataEmprestimo = dataEmprestimo; }

    public LocalDate getDataPrevistaDevolucao() { return dataPrevistaDevolucao; }
    public void setDataPrevistaDevolucao(LocalDate dataPrevistaDevolucao) { this.dataPrevistaDevolucao = dataPrevistaDevolucao; }

    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public void setDataDevolucao(LocalDate dataDevolucao) { this.dataDevolucao = dataDevolucao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Exemplar getExemplar() { return exemplar; }
    public void setExemplar(Exemplar exemplar) { this.exemplar = exemplar; }

    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Emprestimo that = (Emprestimo) o;
        return Objects.equals(idEmprestimo, that.idEmprestimo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEmprestimo);
    }
}