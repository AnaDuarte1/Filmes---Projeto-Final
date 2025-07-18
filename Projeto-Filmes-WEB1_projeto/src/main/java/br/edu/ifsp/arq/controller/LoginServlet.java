package br.edu.ifsp.arq.controller;

import br.edu.ifsp.arq.dao.UsuarioDAO;
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
import java.util.logging.Logger;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    public LoginServlet() {
        super();
        LOGGER.info("LoginServlet inicializado.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String senha = request.getParameter("senha");
        
        LOGGER.info("Tentativa de login para o email: " + email);
        
        Map<String, String> result = new HashMap<>();
        Gson gson = new Gson();

        UsuarioDAO usuarioDAO = UsuarioDAO.getInstance(); 

        try {
            Usuario usuario = usuarioDAO.autenticar(email, senha);

            if (usuario != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("usuarioLogado", usuario);
                LOGGER.info("Login bem-sucedido para o usuário: " + usuario.getNome() + " (" + usuario.getEmail() + ") - Tipo: " + usuario.getTipo());
                result.put("status", "sucesso");
                result.put("message", "Login bem-sucedido!");
                result.put("userType", usuario.getTipo()); 
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
                LOGGER.warning("Falha no login para o email: " + email + " - Credenciais inválidas.");
                result.put("status", "erro");
                result.put("message", "Email ou senha inválidos.");
            }
        } catch (Exception e) {
            LOGGER.severe("Erro inesperado no LoginServlet: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("status", "erro");
            result.put("message", "Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.");
        } finally {
            response.getWriter().write(gson.toJson(result));
        }
    }
}