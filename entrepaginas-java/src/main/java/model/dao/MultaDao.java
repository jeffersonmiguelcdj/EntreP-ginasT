package model.dao;

import java.util.List;
import model.entities.Multa;

public interface MultaDao {
    void inserir(Multa obj);
    void atualizar(Multa obj);
    void eliminarPorId(Integer id);
    Multa procurarPorId(Integer id);
    List<Multa> procurarTodos();
}