package br.edu.ifsp.arq.controller;

import br.edu.ifsp.arq.dao.UsuarioDAO;
import br.edu.ifsp.arq.model.Usuario;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger; // Importação adicionada para Logger

@WebServlet("/cadastrar")
public class CadastroServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CadastroServlet.class.getName()); 

    public CadastroServlet() {
        super();
        LOGGER.info("CadastroServlet inicializado."); 
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");
        String tipo = "comum"; 

        Gson gson = new Gson();
        Map<String, String> resposta = new HashMap<>();

        UsuarioDAO usuarioDAO = UsuarioDAO.getInstance(); 

        try {
            LOGGER.info("Tentativa de cadastro para o email: " + email); 
            if (usuarioDAO.buscarPorEmail(email) != null) {
                response.setStatus(HttpServletResponse.SC_CONFLICT); 
                resposta.put("status", "erro");
                resposta.put("mensagem", "Erro: este e-mail já está em uso!");
                LOGGER.warning("Falha no cadastro: email já em uso para " + email); 
            } else {
                
                Usuario novoUsuario = new Usuario(nome, email, senha, tipo);
                boolean adicionado = usuarioDAO.adicionarUsuario(novoUsuario);

                if (adicionado) {
                    resposta.put("status", "sucesso");
                    resposta.put("mensagem", "Usuário cadastrado com sucesso! Faça o login.");
                    LOGGER.info("Usuário cadastrado com sucesso: " + email); 
                } else {
                    
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resposta.put("status", "erro");
                    resposta.put("mensagem", "Erro interno ao cadastrar usuário.");
                    LOGGER.severe("Erro interno ao cadastrar usuário (adicionarUsuario retornou false) para " + email); 
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Erro inesperado no CadastroServlet para " + email + ": " + e.getMessage()); 
            e.printStackTrace(); 
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resposta.put("status", "erro");
            resposta.put("mensagem", "Ocorreu um erro inesperado no servidor: " + e.getMessage());
        } finally {
            response.getWriter().write(gson.toJson(resposta));
        }
    }
}
