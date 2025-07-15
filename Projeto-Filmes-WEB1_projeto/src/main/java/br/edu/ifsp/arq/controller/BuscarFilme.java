package br.edu.ifsp.arq.controller;

import br.edu.ifsp.arq.dao.FilmeDAO;
import br.edu.ifsp.arq.model.Filme;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Importar Collectors

@WebServlet("/buscar-filme")
public class BuscarFilme extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // Não é necessário ter um filmeDAO como atributo se ele é um Singleton
    // private final FilmeDAO filmeDAO; 

    public BuscarFilme() {
        // this.filmeDAO = FilmeDAO.getInstance(); // Não é mais necessário
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();
        
        try {
            String palavraChave = request.getParameter("palavraChave");
            
            if (palavraChave == null || palavraChave.trim().isEmpty()) {
                // Se a palavra-chave estiver vazia, retorna todos os filmes
                List<Filme> todosOsFilmes = FilmeDAO.getInstance().getListaFilmes();
                String jsonResponse = gson.toJson(todosOsFilmes);
                response.getWriter().write(jsonResponse);
                return;
            }

            // Transforma a palavra-chave para minúsculas para busca case-insensitive
            String p = palavraChave.toLowerCase();
            
            // Pega a lista completa de filmes e filtra
            List<Filme> filmesEncontrados = FilmeDAO.getInstance().getListaFilmes().stream()
                .filter(filme -> filme.getTitulo().toLowerCase().contains(p) ||
                                 filme.getDiretor().toLowerCase().contains(p) ||
                                 filme.getCategoria().toLowerCase().contains(p))
                .collect(Collectors.toList());

            String jsonResponse = gson.toJson(filmesEncontrados);
            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> erro = new HashMap<>();
            erro.put("status", "erro");
            erro.put("mensagem", "Ocorreu um erro ao processar a busca: " + e.getMessage());
            response.getWriter().write(gson.toJson(erro));
        }
    }
}