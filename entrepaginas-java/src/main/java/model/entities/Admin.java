package model.entities;

public class Admin extends Funcionario {
    private String nivelAcesso;

    public Admin() {
        super();
    }

    public Admin(Integer idFuncionario, String nome, String cpf, String email, String senhaHash, String nivelAcesso) {
        super(idFuncionario, nome, cpf, email, senhaHash, "Admin");
        this.nivelAcesso = nivelAcesso;
    }

    @Override
    public boolean podeGerenciarAcervo() {
        return true;
    }

    public String getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }
}