package model.entities;

import java.util.Objects;

public class Usuario {
    private Integer idUsuario;
    private String nome;
    private String cpf;
    private String emailUsuario;
    private String senhaHash;
    private String telefone;

    public Usuario() {}

    public Usuario(Integer idUsuario, String nome, String cpf, String emailUsuario, String senhaHash, String telefone) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.cpf = cpf;
        this.emailUsuario = emailUsuario;
        this.senhaHash = senhaHash;
        this.telefone = telefone;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(idUsuario, usuario.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario);
    }

    @Override
    public String toString() {
        return "Usuario [id=" + idUsuario + ", nome=" + nome + ", cpf=" + cpf + ", email=" + emailUsuario + "]";
    }
}