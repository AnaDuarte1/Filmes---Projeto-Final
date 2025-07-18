package br.edu.ifsp.arq.dao;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import br.edu.ifsp.arq.model.Filme;
import br.edu.ifsp.arq.model.Comentario;

public class FilmeDAO {
    private static FilmeDAO instance;
    private static ArrayList<Filme> listaDeFilmes;

    private FilmeDAO() {
        listaDeFilmes = new ArrayList<>();
    }

    public static FilmeDAO getInstance() {
        if (instance == null) {
            instance = new FilmeDAO();
        }
        return instance;
    }

    private String getCaminhoArquivo() {
        String userHome = System.getProperty("user.home");
        return userHome + File.separator + "Downloads" + File.separator + "filmes.txt";
    }

    public ArrayList<Filme> getListaFilmes() {
        File f = new File(getCaminhoArquivo());
        Gson gson = new Gson();
        ArrayList<Filme> lista = new ArrayList<>();

        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            listaDeFilmes = lista;
            return lista;
        }

        try (FileReader fr = new FileReader(f, StandardCharsets.UTF_8)) {
            lista = gson.fromJson(fr, new TypeToken<ArrayList<Filme>>(){}.getType());
            if (lista == null) {
                lista = new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        listaDeFilmes = lista;
        return new ArrayList<>(listaDeFilmes);
    }


    public boolean salvarListaFilmes(ArrayList<Filme> lista) {
        Gson gson = new Gson();
        try (FileWriter fw = new FileWriter(getCaminhoArquivo(), StandardCharsets.UTF_8)) {
            gson.toJson(lista, fw);  
            listaDeFilmes = lista;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean adicionarFilme(Filme filme) {
        ArrayList<Filme> lista = getListaFilmes(); 

        int novoId = 1;
        if (!lista.isEmpty()) {
            novoId = lista.stream().mapToInt(Filme::getId).max().getAsInt() + 1;
        }
        filme.setId(novoId);

        if (filme.getComentarios() == null) {
            filme.setComentarios(new ArrayList<>());
        }

        lista.add(filme);
        return salvarListaFilmes(lista);
    }

    public Filme getFilme(String id) {
        int ID;
        try {
            ID = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return null;
        }

        for (Filme f : getListaFilmes()) {
            if (f.getId() == ID) {
                return f;
            }
        }
        return null;
    }

    public boolean atualizarFilme(Filme filmeAtualizado) {
        ArrayList<Filme> lista = getListaFilmes();
        boolean atualizado = false;

        for (int i = 0; i < lista.size(); i++) {
            Filme f = lista.get(i);
            if (f.getId() == filmeAtualizado.getId()) {
                lista.set(i, filmeAtualizado);
                atualizado = true;
                break;
            }
        }

        if (atualizado) {
            return salvarListaFilmes(lista);
        }
        return false;
    }

    public void removerFilmePorId(String id) {
        int idInt;
        try {
            idInt = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return;
        }

        ArrayList<Filme> lista = getListaFilmes();

        Filme filmeParaRemover = null;
        for (Filme filme : lista) {
            if (filme != null && filme.getId() == idInt) {
                filmeParaRemover = filme;
                break;
            }
        }

        if (filmeParaRemover != null) {
            lista.remove(filmeParaRemover);
            salvarListaFilmes(lista);
        }
    }

    public boolean adicionarComentario(int filmeId, Comentario comentario) {
        ArrayList<Filme> filmes = getListaFilmes();
        for (Filme filme : filmes) {
            if (filme.getId() == filmeId) {
                comentario.setId(lastComentarioId(filme.getComentarios()));
                filme.getComentarios().add(comentario);
                filme.recalcularNotaMedia();
                return salvarListaFilmes(filmes);
            }
        }
        return false;
    }

    public List<Comentario> getComentariosDoFilme(String filmeId) {
        try {
            int id = Integer.parseInt(filmeId);
            for (Filme filme : getListaFilmes()) {
                if (filme.getId() == id) {
                    return filme.getComentarios() != null ? filme.getComentarios() : new ArrayList<>();
                }
            }
        } catch (NumberFormatException e) {
        }
        return new ArrayList<>();
    }

    private int lastComentarioId(List<Comentario> list) {
        if (list == null || list.isEmpty()) {
            return 1;
        }
        return list.stream().mapToInt(Comentario::getId).max().orElse(0) + 1;
    }
}
