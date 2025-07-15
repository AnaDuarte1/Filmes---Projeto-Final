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

@WebServlet("/criar-filme")
@MultipartConfig
public class AdicionarFilme extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        Gson gson = new Gson();
        Map<String, String> resposta = new HashMap<>();

        if (usuarioLogado == null || !"admin".equals(usuarioLogado.getTipo())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resposta.put("status", "erro");
            resposta.put("mensagem", "Acesso não autorizado para esta operação.");
            response.getWriter().write(gson.toJson(resposta));
            return;
        }

        try {
            Part imagemPart = request.getPart("imagem");
            String imagemPath = null;

            if (imagemPart != null && imagemPart.getSize() > 0) {
                String uploadPath = getServletContext().getRealPath("") + File.separator + "imagens";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                String fileName = Paths.get(imagemPart.getSubmittedFileName()).getFileName().toString();
                imagemPath = "imagens/" + fileName;
                imagemPart.write(uploadPath + File.separator + fileName);
            }

            Filme novoFilme = new Filme(
                request.getParameter("titulo"),
                request.getParameter("diretor"),
                Integer.parseInt(request.getParameter("anoLancamento")),
                request.getParameter("sinopse"),
                request.getParameter("idioma"),
                request.getParameter("formato"),
                request.getParameter("categoria"),
                Integer.parseInt(request.getParameter("duracao")),
                imagemPath, 0);

            FilmeDAO.getInstance().adicionarFilme(novoFilme);
            
            resposta.put("status", "sucesso");
            resposta.put("mensagem", "Filme adicionado com sucesso!");
            resposta.put("filmeId", String.valueOf(novoFilme.getId())); 
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resposta.put("status", "erro");
            resposta.put("mensagem", "Erro ao adicionar filme: " + e.getMessage());
        } finally {
            response.getWriter().write(gson.toJson(resposta));
        }
    }
}