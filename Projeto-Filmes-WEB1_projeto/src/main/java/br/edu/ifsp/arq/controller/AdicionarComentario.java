package br.edu.ifsp.arq.controller;

import br.edu.ifsp.arq.dao.FilmeDAO;
import br.edu.ifsp.arq.dao.UsuarioDAO; // Importar UsuarioDAO
import br.edu.ifsp.arq.model.Comentario;
import br.edu.ifsp.arq.model.Usuario;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/adicionar-comentario")
public class AdicionarComentario extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public AdicionarComentario() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8"); 
        
        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        Map<String, String> resposta = new HashMap<>();
        Gson gson = new Gson();

        if (usuarioLogado == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resposta.put("status", "erro");
            resposta.put("mensagem", "Você precisa estar logado para comentar.");
            response.getWriter().write(gson.toJson(resposta));
            return;
        }

        if (!"avaliador".equals(usuarioLogado.getTipo())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); 
            resposta.put("status", "erro");
            resposta.put("mensagem", "Apenas usuários avaliadores podem adicionar comentários.");
            response.getWriter().write(gson.toJson(resposta));
            return;
        }

        try {
            String filmeIdString = request.getParameter("filmeId");
            int filmeId = Integer.parseInt(filmeIdString);
            String texto = request.getParameter("texto");
            int nota = Integer.parseInt(request.getParameter("nota"));

            if (texto == null || texto.trim().isEmpty() || nota < 1 || nota > 5) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resposta.put("status", "erro");
                resposta.put("mensagem", "Dados de comentário inválidos. Certifique-se de preencher o texto e a nota (1-5).");
                response.getWriter().write(gson.toJson(resposta));
                return;
            }

            Comentario comentario = new Comentario(texto, nota, usuarioLogado);
            
            FilmeDAO filmeDAO = FilmeDAO.getInstance();

            boolean comentarioAdicionado = filmeDAO.adicionarComentario(filmeId, comentario);

            if (comentarioAdicionado) {
                resposta.put("status", "sucesso");
                resposta.put("mensagem", "Comentário adicionado com sucesso!");
                response.getWriter().write(gson.toJson(resposta));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resposta.put("status", "erro");
                resposta.put("mensagem", "Filme não encontrado para adicionar o comentário.");
                response.getWriter().write(gson.toJson(resposta));
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resposta.put("status", "erro");
            resposta.put("mensagem", "ID do filme ou nota inválidos.");
            response.getWriter().write(gson.toJson(resposta));
        } catch (Exception e) {
            e.printStackTrace(); 
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resposta.put("status", "erro");
            resposta.put("mensagem", "Erro interno ao processar seu comentário: " + e.getMessage());
            response.getWriter().write(gson.toJson(resposta));
        }
    }
}