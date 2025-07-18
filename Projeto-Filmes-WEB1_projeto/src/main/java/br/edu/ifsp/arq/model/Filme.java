package br.edu.ifsp.arq.model;
import java.util.ArrayList;
import java.util.List;

public class Filme {
    private int id;
    private String titulo;
    private String diretor;
    private int anoLancamento;
    private String sinopse;
    private String idioma;
    private String formato;
    private String categoria;
    private int duracao;
    private String imagem;
    private double notaMedia;
    private List<Comentario> comentarios;

    public Filme(String titulo, String diretor, int anoLancamento, String sinopse, String idioma, String formato, String categoria, int duracao, String imagem, double notaMedia) {
        this.titulo = titulo;
        this.diretor = diretor;
        this.anoLancamento = anoLancamento;
        this.sinopse = sinopse;
        this.idioma = idioma;
        this.formato = formato;
        this.categoria = categoria;
        this.duracao = duracao;
        this.imagem = imagem;
        this.notaMedia = notaMedia;
        this.comentarios = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDiretor() { return diretor; }
    public void setDiretor(String diretor) { this.diretor = diretor; }
    public int getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(int anoLancamento) { this.anoLancamento = anoLancamento; }
    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getDuracao() { return duracao; }
    public void setDuracao(int duracao) { this.duracao = duracao; }
    public String getImagem() { return imagem; }
    public void setImagem(String imagem) { this.imagem = imagem; }
    public double getNotaMedia() { return notaMedia; }
    public void setNotaMedia(double notaMedia) { this.notaMedia = notaMedia; }

    public List<Comentario> getComentarios() {
        if (this.comentarios == null) { 
            this.comentarios = new ArrayList<>();
        }
        return comentarios;
    }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public void recalcularNotaMedia() {
        if (comentarios == null || comentarios.isEmpty()) {
            this.notaMedia = 0.0;
            return;
        }
        double totalNotas = 0;
        for (Comentario c : comentarios) {
            totalNotas += c.getNota();
        }
        this.notaMedia = totalNotas / comentarios.size();
    }
}