package model.dao;

import java.util.List;
import model.entities.Usuario;

public interface UsuarioDao {
    void inserir(Usuario obj);
    void atualizar(Usuario obj);
    void eliminarPorId(Integer id);
    Usuario procurarPorId(Integer id);
    List<Usuario> procurarTodos();
}