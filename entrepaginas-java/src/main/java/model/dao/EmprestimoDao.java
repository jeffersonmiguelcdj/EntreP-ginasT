package model.dao;

import java.util.List;
import model.entities.Emprestimo;

public interface EmprestimoDao {
    void inserir(Emprestimo obj);
    void atualizar(Emprestimo obj);
    void eliminarPorId(Integer id);
    Emprestimo procurarPorId(Integer id);
    List<Emprestimo> procurarTodos();
}