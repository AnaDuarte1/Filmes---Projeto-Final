package br.edu.ifsp.arq.dao;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;


import com.google.gson.Gson; // Importar Gson

import br.edu.ifsp.arq.model.Usuario;

public class UsuarioDAO {
    private static UsuarioDAO instance; 
    private ArrayList<Usuario> listaDeUsuarios; 

    private UsuarioDAO() {
        listaDeUsuarios = new ArrayList<>();
        carregarUsuarios(); 
    }

    public static UsuarioDAO getInstance() {
        if (instance == null) {
            instance = new UsuarioDAO();
        }
        return instance;
    }


    private String getCaminhoArquivo() {
        String userHome = System.getProperty("user.home");
        return userHome + File.separator + "Downloads" + File.separator + "Usuarios.txt";
    }

    private void carregarUsuarios() {
        File f = new File(getCaminhoArquivo());
        Gson gson = new Gson();
        listaDeUsuarios.clear(); 
        int maxId = 0;

        if (!f.exists()) {
            try {
                f.createNewFile(); 
                Usuario admin = new Usuario(1, "Admin", "admin@cineweb.com", "admin123", "admin");
                Usuario avaliador = new Usuario(2, "Avaliador Padrao", "avaliador@cineweb.com", "avaliador123", "avaliador");

                listaDeUsuarios.add(admin);
                listaDeUsuarios.add(avaliador);

                salvarListaUsuarios(listaDeUsuarios);

                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try (FileReader fr = new FileReader(f);
             Scanner sc = new Scanner(fr)) {

            while (sc.hasNextLine()) {
                String linha = sc.nextLine().trim();
                if (linha.isEmpty()) continue;

                try {
                    Usuario u = gson.fromJson(linha, Usuario.class);
                    if (u != null) {
                        listaDeUsuarios.add(u);
                        if (u.getId() > maxId) {
                            maxId = u.getId();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao ler linha do arquivo de usuários: " + linha + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean salvarListaUsuarios(ArrayList<Usuario> lista) {
        Gson gson = new Gson();
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(getCaminhoArquivo()), StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(writer)) {
            for (Usuario u : lista) {
                String json = gson.toJson(u);
                pw.println(json);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean adicionarUsuario(Usuario usuario) {

        if (getListaUsuarios().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(usuario.getEmail()))) {
            return false; 
        }
        
  
        if (usuario.getId() == 0) { 
            usuario.setId(lastId()); 
        }
        
        ArrayList<Usuario> currentList = getListaUsuarios(); 
        currentList.add(usuario); 
        return salvarListaUsuarios(currentList); 
    }

    public Usuario getUsuario(String id) {
        try {
            int ID = Integer.parseInt(id);
            return getListaUsuarios().stream()
                                     .filter(u -> u.getId() == ID)
                                     .findFirst()
                                     .orElse(null);
        } catch (NumberFormatException e) {
            return null; 
        }
    }
    
    public Usuario autenticar(String email, String senha) {
        return getListaUsuarios().stream()
                                 .filter(u -> u.getEmail().equals(email) && u.getSenha().equals(senha))
                                 .findFirst()
                                 .orElse(null);
    }
    
    public Usuario buscarPorEmail(String email) {
        return getListaUsuarios().stream()
                                 .filter(u -> u.getEmail().equalsIgnoreCase(email))
                                 .findFirst()
                                 .orElse(null);
    }
    
    public ArrayList<Usuario> getListaUsuarios() {
        carregarUsuarios(); 
        return new ArrayList<>(listaDeUsuarios); 
    }

    public boolean atualizarUsuario(Usuario usuarioAtualizado) {
        ArrayList<Usuario> lista = getListaUsuarios();
        boolean atualizado = false;

        for (int i = 0; i < lista.size(); i++) {
            Usuario usuario = lista.get(i);
            if (usuario.getId() == usuarioAtualizado.getId()) {
                lista.set(i, usuarioAtualizado);
                atualizado = true;
                break;
            }
        }

        if (atualizado) {
            return salvarListaUsuarios(lista);
        }
        return false;
    }

    public void removerUsuarioPorId(String id) {
        try {
            int idInt = Integer.parseInt(id);
            ArrayList<Usuario> lista = getListaUsuarios();

            Usuario usuarioParaRemover = null;
            for (Usuario usuario : lista) {
                if (usuario != null && usuario.getId() == idInt) {
                    usuarioParaRemover = usuario;
                    break;
                }
            }

            if (usuarioParaRemover != null) {
                lista.remove(usuarioParaRemover);
                salvarListaUsuarios(lista);
            }
        } catch (NumberFormatException e) {
            System.err.println("ID de usuário inválido para remoção: " + id);
            e.printStackTrace();
        }
    }

    public int lastId() {
        return listaDeUsuarios.stream().mapToInt(Usuario::getId).max().orElse(0) + 1;
    }

}
