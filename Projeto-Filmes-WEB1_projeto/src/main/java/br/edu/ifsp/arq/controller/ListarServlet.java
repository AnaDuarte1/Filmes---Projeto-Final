package br.edu.ifsp.arq.controller;

import br.edu.ifsp.arq.dao.FilmeDAO;
import br.edu.ifsp.arq.model.Filme;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/listar-filmes")
public class ListarServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String categoria = request.getParameter("categoria");
            List<Filme> filmes = FilmeDAO.getInstance().getListaFilmes();
            
            if (categoria != null && !categoria.trim().isEmpty()) {
                filmes = filmes.stream()
                               .filter(f -> f.getCategoria().equalsIgnoreCase(categoria))
                               .collect(Collectors.toList());
            }
            
            String jsonResponse = new Gson().toJson(filmes);
            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"erro\", \"mensagem\":\"Erro ao carregar filmes\"}");
        }
    }
}