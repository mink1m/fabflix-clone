import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // create datasource registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Declare our statement
            Statement statement = conn.createStatement();

            // Construct a query with parameter represented by "?"
            String query = "WITH top AS ( " +
                    "SELECT * " +
                    "FROM movies AS m, ( " +
                    "SELECT * " +
                    "FROM ratings " +
                    "ORDER BY rating DESC " +
                    "LIMIT 20 " +
                    ") AS r " +
                    "WHERE m.id = r.movieID " +
                    ") " +
                    "SELECT m.id, m.title, m.year, m.director, " +
                    "GROUP_CONCAT(DISTINCT g.name ORDER BY g.id ASC SEPARATOR ', ') AS genres, " +
                    "GROUP_CONCAT(DISTINCT s.name, ' : ', s.id ORDER BY s.name ASC SEPARATOR ', ') AS stars, " +
                    "m.rating " +
                    "FROM top AS m " +
                    "LEFT JOIN genres_in_movies AS gm ON m.id = gm.movieId " +
                    "LEFT JOIN genres AS g ON gm.genreId = g.id " +
                    "LEFT JOIN stars_in_movies AS sm ON m.id = sm.movieId " +
                    "LEFT JOIN stars AS s ON sm.starId = s.id " +
                    "GROUP BY m.id, m.rating  " +
                    "ORDER BY m.rating DESC  " +
                    "LIMIT 20";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");

                String movieGenres = rs.getString("genres");
                String movieStars = rs.getString("stars");
                Number movieRating = rs.getFloat("rating");

                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);

                jsonObject.addProperty("genres", movieGenres);
                jsonObject.addProperty("stars", movieStars);
                jsonObject.addProperty("rating", movieRating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }


}
