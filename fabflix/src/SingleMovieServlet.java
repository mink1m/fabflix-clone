
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;
    private DataSource masterDataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            masterDataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master");
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

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting movie id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "WITH movieInfo as ( " +
                    "    SELECT m.id, m.title, m.year, m.director, r.rating " +
                    "    FROM movies as m LEFT JOIN ratings r on m.id = r.movieId " +
                    "    WHERE m.id = ? " +
                    "), stars_count as ( " +
                    "    SELECT sim2.starId, COUNT(DISTINCT sim2.movieId) AS num_movies " +
                    "    FROM stars_in_movies as sim, stars_in_movies as sim2 " +
                    "    WHERE sim.movieId IN ( " +
                    "        SELECT id " +
                    "        FROM movieInfo " +
                    "    ) AND sim2.starId = sim.starId " +
                    "    GROUP BY sim2.starId " +
                    ") " +
                    "SELECT m.id, m.title, m.year, m.director, " +
                    "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC SEPARATOR ',') AS genres, " +
                    "GROUP_CONCAT(DISTINCT g.id ORDER BY g.name ASC SEPARATOR ',') AS genre_ids, " +
                    "GROUP_CONCAT(DISTINCT s.name ORDER BY sc.num_movies DESC, s.name ASC SEPARATOR ',') AS star_names, " +
                    "GROUP_CONCAT(DISTINCT s.id ORDER BY sc.num_movies DESC, s.name ASC SEPARATOR ',') AS star_ids, " +
                    "m.rating " +
                    "FROM movieInfo AS m " +
                    "LEFT JOIN genres_in_movies AS gm ON m.id = gm.movieId " +
                    "LEFT JOIN genres AS g ON gm.genreId = g.id " +
                    "LEFT JOIN stars_in_movies AS sm ON m.id = sm.movieId " +
                    "LEFT JOIN stars AS s ON sm.starId = s.id " +
                    "LEFT JOIN stars_count AS sc ON s.id = sc.starId " +
                    "GROUP BY m.id, m.rating";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");

                String genres = rs.getString("genres");
                String genre_ids = rs.getString("genre_ids");
                String star_names = rs.getString("star_names");
                String star_ids = rs.getString("star_ids");
                Number rating = rs.getFloat("rating");

                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);

                jsonObject.addProperty("genres", genres);
                jsonObject.addProperty("genre_ids", genre_ids);
                jsonObject.addProperty("star_names", star_names);
                jsonObject.addProperty("star_ids", star_ids);
                jsonObject.addProperty("rating", rating);

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

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Retrieve parameters from url request.
        String movie_title = request.getParameter("movie_title");
        String movie_year = request.getParameter("movie_year");
        String movie_director = request.getParameter("movie_director");

        String star_name = request.getParameter("star_name");

        String genre_name = request.getParameter("genre_name");

        // The log message can be found in localhost log
        request.getServletContext().log("creating move: " + movie_title);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = masterDataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "call add_movie(?, ?, ?, ?, ?)";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, movie_title);
            statement.setString(2, movie_year);
            statement.setString(3, movie_director);
            statement.setString(4, star_name);
            statement.setString(5, genre_name);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();

            if(rs.next()) {
                String message = rs.getString("result");

                jsonObject.addProperty("status", "success");
                jsonObject.addProperty("message", message);
            }
            else {
                jsonObject.addProperty("status", "failure");
                jsonObject.addProperty("message", "Server Error No response from database");
            }

            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonObject.toString());
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
