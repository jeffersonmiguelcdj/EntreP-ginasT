package model.dao;

import java.util.List;
import model.entities.Funcionario;

public interface FuncionarioDao {
    void inserir(Funcionario obj);
    void atualizar(Funcionario obj);
    void eliminarPorId(Integer id);
    Funcionario procurarPorId(Integer id);
    List<Funcionario> procurarTodos();
}