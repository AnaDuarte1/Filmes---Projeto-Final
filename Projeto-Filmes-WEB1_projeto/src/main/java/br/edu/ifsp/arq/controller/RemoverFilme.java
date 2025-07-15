package br.edu.ifsp.arq.controller;

import br.edu.ifsp.arq.dao.FilmeDAO;
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
import javax.servlet.http.HttpSession;

@WebServlet("/excluir-filme")
public class RemoverFilme extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(false);
        Usuario usuarioLogado = (session != null) ? (Usuario) session.getAttribute("usuarioLogado") : null;
        
        Gson gson = new Gson();
        Map<String, String> resposta = new HashMap<>();

        if (usuarioLogado == null || !"admin".equals(usuarioLogado.getTipo())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resposta.put("status", "erro");
            resposta.put("mensagem", "Acesso não autorizado.");
            response.getWriter().write(gson.toJson(resposta));
            return;
        }

        try {
            String id = request.getParameter("id"); // Mudança aqui: id agora é String
            // O método removerFilmePorId não retorna boolean, ele apenas remove.
            // A verificação de sucesso/falha precisaria de uma lógica diferente,
            // talvez verificando se o filme ainda existe após a tentativa de remoção.
            // No entanto, para seguir o padrão do ReceitasDAO, que não retorna um boolean,
            // vamos considerar que a operação foi tentada.
            FilmeDAO.getInstance().removerFilmePorId(id);

            // Para simular o comportamento anterior, onde 'removido' era um boolean:
            // Poderíamos buscar o filme novamente e verificar se ele foi removido.
            // Porém, para manter a simplicidade e a semelhança com o ReceitasDAO,
            // vamos presumir que se não houver exceção, a remoção foi "tentada".
            // Se precisar de uma confirmação mais robusta, você terá que implementar a lógica no DAO.
            
            // A lógica de "Filme não encontrado ou não pôde ser removido"
            // seria mais bem tratada se o removerFilmePorId retornasse um boolean.
            // Como o FilmeDAO agora tem um método que não retorna, a validação
            // de sucesso ou falha pode ser mais complexa.

            // Por enquanto, vamos manter a resposta de sucesso se não houver exceção.
            resposta.put("status", "sucesso");
            resposta.put("mensagem", "Filme excluído com sucesso (ou tentativa de exclusão realizada).");
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resposta.put("status", "erro");
            resposta.put("mensagem", "ID do filme inválido.");
        } catch (Exception e) { // Captura outras exceções que possam ocorrer
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resposta.put("status", "erro");
            resposta.put("mensagem", "Erro ao excluir filme: " + e.getMessage());
        }
        
        response.getWriter().write(gson.toJson(resposta));
    }
}