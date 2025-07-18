package br.edu.ifsp.arq.model;

import br.edu.ifsp.arq.model.Comentario;

public class Comentario {
    private int id; 
    private String texto;
    private int nota;
    private Usuario autor; 
    
    public Comentario(String texto, int nota, Usuario autor) {
        this.texto = texto;
        this.nota = nota;
        this.autor = autor;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public int getNota() { return nota; }
    public void setNota(int nota) { this.nota = nota; }
    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }
}