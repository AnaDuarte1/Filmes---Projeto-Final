package br.edu.ifsp.arq.controller;

import br.edu.ifsp.arq.dao.FilmeDAO;
import br.edu.ifsp.arq.model.Filme;
import br.edu.ifsp.arq.model.Usuario;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet({"/editar-filme", "/atualizar-filme"})
@MultipartConfig
public class AtualizarFilme extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String id = request.getParameter("id"); // Mudança aqui: id agora é String
            Filme filme = FilmeDAO.getInstance().getFilme(id); // Usa getFilme(String id)

            if (filme == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"status\":\"erro\", \"mensagem\":\"Filme não encontrado\"}");
                return;
            }
            // Returns the movie data as JSON
            response.getWriter().write(new Gson().toJson(filme));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"erro\", \"mensagem\":\"ID de filme inválido\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        Gson gson = new Gson();
        Map<String, String> resposta = new HashMap<>();

        // 1. Security Check: Only admins can update movies.
        if (usuarioLogado == null || !"admin".equals(usuarioLogado.getTipo())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resposta.put("status", "erro");
            resposta.put("mensagem", "Acesso não autorizado para esta operação.");
            response.getWriter().write(gson.toJson(resposta));
            return;
        }

        try {
            String idString = request.getParameter("id"); // Mudança aqui: id agora é String
            int id = Integer.parseInt(idString); // Converte para int para setar no objeto Filme

            FilmeDAO filmeDAO = FilmeDAO.getInstance();
            Filme filmeAtual = filmeDAO.getFilme(idString); // Usa getFilme(String id)

            if (filmeAtual == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resposta.put("status", "erro");
                resposta.put("mensagem", "Filme não encontrado para atualização.");
                response.getWriter().write(gson.toJson(resposta));
                return;
            }

            Part imagemPart = request.getPart("imagem");
            String imagemPath = filmeAtual.getImagem(); // Keep the old image by default
            
            if (imagemPart != null && imagemPart.getSize() > 0) {
                String uploadPath = getServletContext().getRealPath("") + File.separator + "imagens";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdir();

                String fileName = Paths.get(imagemPart.getSubmittedFileName()).getFileName().toString();
                imagemPath = "imagens/" + fileName; 
                imagemPart.write(uploadPath + File.separator + fileName);
            }

            filmeAtual.setId(id); // Garante que o ID do filme seja mantido
            filmeAtual.setTitulo(request.getParameter("titulo"));
            filmeAtual.setDiretor(request.getParameter("diretor"));
            filmeAtual.setAnoLancamento(Integer.parseInt(request.getParameter("anoLancamento")));
            filmeAtual.setSinopse(request.getParameter("sinopse"));
            filmeAtual.setIdioma(request.getParameter("idioma"));
            filmeAtual.setFormato(request.getParameter("formato"));
            filmeAtual.setCategoria(request.getParameter("categoria"));
            filmeAtual.setDuracao(Integer.parseInt(request.getParameter("duracao")));
            filmeAtual.setImagem(imagemPath);

            boolean atualizado = filmeDAO.atualizarFilme(filmeAtual);
            
            if (atualizado) {
                resposta.put("status", "sucesso");
                resposta.put("mensagem", "Filme '" + filmeAtual.getTitulo() + "' atualizado com sucesso!");
                resposta.put("filmeId", String.valueOf(filmeAtual.getId()));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resposta.put("status", "erro");
                resposta.put("mensagem", "Ocorreu um erro inesperado ao salvar as alterações.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resposta.put("status", "erro");
            resposta.put("mensagem", "Erro ao processar a requisição: " + e.getMessage());
        } finally {
            // 5. Send the JSON response
            response.getWriter().write(gson.toJson(resposta));
        }
    }
}