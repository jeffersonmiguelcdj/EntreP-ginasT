package model.entities;

import java.util.Objects;

public class Autor {
    private Integer idAutor;
    private String nome;
    private String nacionalidade;

    public Autor() {}

    public Autor(Integer idAutor, String nome, String nacionalidade) {
        this.idAutor = idAutor;
        this.nome = nome;
        this.nacionalidade = nacionalidade;
    }

    public Integer getIdAutor() { return idAutor; }
    public void setIdAutor(Integer idAutor) { this.idAutor = idAutor; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Autor autor = (Autor) o;
        if (idAutor != null && autor.idAutor != null) {
            return Objects.equals(idAutor, autor.idAutor);
        }
        return Objects.equals(nome, autor.nome) && Objects.equals(nacionalidade, autor.nacionalidade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAutor != null ? idAutor : nome);
    }
}