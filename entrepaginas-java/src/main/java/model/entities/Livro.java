package model.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Livro {
    private Integer idLivro;
    private String titulo;
    private String isbn;
    private Integer anoPublicacao;
    private BigDecimal precoVenda;
    private Categoria categoria;
    private List<Autor> autores = new ArrayList<>();

    public Livro() {}

    public Livro(Integer idLivro, String titulo, String isbn, Integer anoPublicacao, BigDecimal precoVenda, Categoria categoria) {
        this.idLivro = idLivro;
        this.titulo = titulo;
        this.isbn = isbn;
        this.anoPublicacao = anoPublicacao;
        this.precoVenda = precoVenda;
        this.categoria = categoria;
    }

    public void adicionarAutor(Autor autor) {
        if (!autores.contains(autor)) {
            autores.add(autor);
        }
    }

    public void limparAutores() {
        autores.clear();
    }

    public Integer getIdLivro() { return idLivro; }
    public void setIdLivro(Integer idLivro) { this.idLivro = idLivro; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getAnoPublicacao() { return anoPublicacao; }
    public void setAnoPublicacao(Integer anoPublicacao) { this.anoPublicacao = anoPublicacao; }
    public BigDecimal getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(BigDecimal precoVenda) { this.precoVenda = precoVenda; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public List<Autor> getAutores() { return autores; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Livro livro = (Livro) o;
        return Objects.equals(idLivro, livro.idLivro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLivro);
    }

    @Override
    public String toString() {
        return "Livro #" + idLivro + ": \"" + titulo + "\" | ISBN: " + isbn +
                " | Ano: " + anoPublicacao + " | Preço: R$ " + precoVenda +
                " | Categoria: " + (categoria != null ? categoria.getNome() : "Sem categoria") +
                " | Autor(es): " + nomesDosAutores();
    }

    public String nomesDosAutores() {
        if (autores.isEmpty()) return "Sem autor";
        List<String> nomes = new ArrayList<>();
        for (Autor autor : autores) nomes.add(autor.getNome());
        return String.join(", ", nomes);
    }
}