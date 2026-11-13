package model.dao;

import java.util.List;
import model.entities.Exemplar;

public interface ExemplarDao {
    void inserir(Exemplar obj);
    void atualizar(Exemplar obj);
    void eliminarPorId(Integer id);
    Exemplar procurarPorId(Integer id);
    List<Exemplar> procurarTodos();
}