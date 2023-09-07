import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "PreviousSearchServlet", urlPatterns = "/api/previous_search")
public class PreviousSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {


        // The log message can be found in localhost log
        request.getServletContext().log("Redirecting to last search");

        User user = (User) request.getSession().getAttribute("user");

        if(user != null) {
            synchronized (user) {
                request.getServletContext().log(user.getId());
                response.sendRedirect( request.getContextPath() + "/list.html?" + user.getLastSearch());
            }
        }
        else {
            response.sendRedirect( request.getContextPath() + "/list.html");
        }

    }

}