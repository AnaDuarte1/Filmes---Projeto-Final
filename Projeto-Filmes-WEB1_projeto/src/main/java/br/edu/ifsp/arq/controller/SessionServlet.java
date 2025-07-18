package br.edu.ifsp.arq.controller;

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

@WebServlet("/session-status")
public class SessionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public SessionServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(false); 
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;

        Gson gson = new Gson();
        
        if (usuarioLogado != null) {

            response.getWriter().write(gson.toJson(usuarioLogado));
        } else {

            Map<String, Boolean> status = new HashMap<>();
            status.put("logado", false);
            response.getWriter().write(gson.toJson(status));
        }
    }
}