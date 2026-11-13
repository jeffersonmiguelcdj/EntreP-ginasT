package model.dao;

import db.DB;
import model.dao.impl.EmprestimoDaoJDBC;
import model.dao.impl.ExemplarDaoJDBC;
import model.dao.impl.FuncionarioDaoJDBC;
import model.dao.impl.LivroDaoJDBC;
import model.dao.impl.MultaDaoJDBC;
import model.dao.impl.UsuarioDaoJDBC;
import model.dao.impl.VendaDaoJDBC;

public class DaoFactory {

    public static LivroDao criarLivroDao() {
        return new LivroDaoJDBC(DB.getConnection());
    }

    public static UsuarioDao criarUsuarioDao() {
        return new UsuarioDaoJDBC(DB.getConnection());
    }

    public static FuncionarioDao criarFuncionarioDao() {
        return new FuncionarioDaoJDBC(DB.getConnection());
    }

    public static ExemplarDao criarExemplarDao() {
        return new ExemplarDaoJDBC(DB.getConnection());
    }

    public static EmprestimoDao criarEmprestimoDao() {
        return new EmprestimoDaoJDBC(DB.getConnection());
    }

    public static MultaDao criarMultaDao() {
        return new MultaDaoJDBC(DB.getConnection());
    }

    public static VendaDao criarVendaDao() {
        return new VendaDaoJDBC(DB.getConnection());
    }
}