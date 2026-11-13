package model.entities;

import java.util.Objects;

public class Categoria {
    private Integer idCategoria;
    private String nome;

    public Categoria() {}

    public Categoria(Integer idCategoria, String nome) {
        this.idCategoria = idCategoria;
        this.nome = nome;
    }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria that = (Categoria) o;
        return Objects.equals(idCategoria, that.idCategoria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCategoria);
    }
}