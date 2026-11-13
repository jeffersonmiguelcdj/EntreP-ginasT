package model.dao;

import java.util.List;
import model.entities.Venda;

public interface VendaDao {
    void inserir(Venda obj);
    void atualizar(Venda obj);
    void eliminarPorId(Integer id);
    Venda procurarPorId(Integer id);
    List<Venda> procurarTodos();
}