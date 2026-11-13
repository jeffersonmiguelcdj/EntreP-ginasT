package model.entities;

public class Atendente extends Funcionario {

    public Atendente() {
        super();
    }

    public Atendente(Integer idFuncionario, String nome, String cpf, String email, String senhaHash) {
        super(idFuncionario, nome, cpf, email, senhaHash, "Atendente");
    }

    @Override
    public boolean podeGerenciarAcervo() {
        return false;
    }
}