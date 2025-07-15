package br.edu.ifsp.arq.controller;

import br.edu.ifsp.arq.dao.FilmeDAO;
import br.edu.ifsp.arq.model.Comentario;
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

@WebServlet("/listar-comentarios")
public class ListarComentarios extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ListarComentarios() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();
        Map<String, String> erroResposta = new HashMap<>();

        try {
            String filmeId = request.getParameter("filmeId");
            if (filmeId == null || filmeId.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                erroResposta.put("status", "erro");
                erroResposta.put("mensagem", "ID do filme não fornecido.");
                response.getWriter().write(gson.toJson(erroResposta));
                return;
            }

            FilmeDAO filmeDAO = FilmeDAO.getInstance();
            List<Comentario> comentarios = filmeDAO.getComentariosDoFilme(filmeId);

            response.getWriter().write(gson.toJson(comentarios));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            erroResposta.put("status", "erro");
            erroResposta.put("mensagem", "ID do filme inválido.");
            response.getWriter().write(gson.toJson(erroResposta));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            erroResposta.put("status", "erro");
            erroResposta.put("mensagem", "Erro interno ao listar comentários: " + e.getMessage());
            response.getWriter().write(gson.toJson(erroResposta));
        }
    }
}
