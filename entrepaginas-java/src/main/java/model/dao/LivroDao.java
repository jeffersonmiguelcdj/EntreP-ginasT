package model.dao;

import model.entities.Livro;
import java.util.List;

public interface LivroDao {
    void inserir(Livro obj);


    void atualizar(Livro obj);
    void eliminarPorId(Integer id);
    Livro procurarPorId(Integer id);
    List<Livro> procurarTodos();
}