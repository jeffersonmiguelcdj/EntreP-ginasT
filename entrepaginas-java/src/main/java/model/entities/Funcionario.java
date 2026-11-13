package model.entities;

import java.util.Objects;

public abstract class Funcionario {
    private Integer idFuncionario;
    private String nome;
    private String cpf;
    private String email;
    private String senhaHash;
    private String tipoFuncionario;

    public Funcionario() {}

    public Funcionario(Integer idFuncionario, String nome, String cpf, String email, String senhaHash, String tipoFuncionario) {
        this.idFuncionario = idFuncionario;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senhaHash = senhaHash;
        this.tipoFuncionario = tipoFuncionario;
    }


    public abstract boolean podeGerenciarAcervo();

    public Integer getIdFuncionario() { return idFuncionario; }
    public void setIdFuncionario(Integer idFuncionario) { this.idFuncionario = idFuncionario; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public String getTipoFuncionario() { return tipoFuncionario; }
    public void setTipoFuncionario(String tipoFuncionario) { this.tipoFuncionario = tipoFuncionario; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Funcionario that = (Funcionario) o;
        return Objects.equals(idFuncionario, that.idFuncionario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFuncionario);
    }

    @Override
    public String toString() {
        return "Funcionário: " + nome + " (" + tipoFuncionario + ")";
    }
}