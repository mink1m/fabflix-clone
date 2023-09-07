
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private String processSearchParameter(String param) {
        return processSearchParameter(param, "%", "%");
    }
    private String processSearchParameter(String param, String base, String outer) {
        return processSearchParameter(param, base, outer, outer);
    }
    private String processSearchParameter(String param, String base, String left, String right) {
        if(param == null || param.trim().length() == 0) {
            return base;
        }
        else {
            return left + param + right;
        }
    }
    private String processSortParam(String sort_by) {
        if(sort_by == null) {
            sort_by = "0";
        }
        switch (sort_by) {
            case "0":
                return "m.rating DESC, m.title DESC";
            case "1":
                return "m.rating DESC, m.title ASC";
            case "2":
                return "m.rating ASC, m.title DESC";
            case "3":
                return "m.rating ASC, m.title ASC";
            case "4":
                return "m.title DESC, m.rating DESC";
            case "5":
                return "m.title DESC, m.rating ASC";
            case "6":
                return "m.title ASC, m.rating DESC";
            default:
                return "m.title ASC, m.rating ASC";
        }
    }
    private int processPageParam(String page) {
        if(page == null) return 0;
        int num;
        try {
            num = Integer.parseInt(page);
        } catch (NumberFormatException e) {
            num = 0;
        }
        return num;
    }
    private int processNumResultsParam(String num_results) {
        if(num_results == null) return 25;
        int num;
        try {
            num = Integer.parseInt(num_results);
        } catch (NumberFormatException e) {
            num = 25;
        }
        return num;
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        long startTimeTS = System.nanoTime();
        long elapsedTimeTJ = 0;

        // Retrieve parameter for search
        User user = (User) request.getSession().getAttribute("user");
        if(user != null) {
            synchronized (user) {
                user.setLastSearch(request.getQueryString());
            }
        }

        // search conditions
        String title = FullTextSearchServlet.process_query_string(request.getParameter("title"));
        String year = processSearchParameter(request.getParameter("year"), "%", "");
        String director = processSearchParameter(request.getParameter("director"));
        String star_name = processSearchParameter(request.getParameter("star_name"));

        // browse conditions
        String genre = processSearchParameter(request.getParameter("genre"), "%", "");
        String start_char = processSearchParameter(request.getParameter("start_char"), "", "", "%");

        // retrieval options

        int page = processPageParam(request.getParameter("page"));
        int page_size = processNumResultsParam(request.getParameter("page_size"));
        String sort_by = processSortParam(request.getParameter("sort"));

        request.getServletContext().log("title: " + title);
        request.getServletContext().log("page: " + page);
        request.getServletContext().log("page_size: " + page_size);


        // The log message can be found in localhost log
        request.getServletContext().log("Searching");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // start time tj
            long startTimeTJ = System.nanoTime();

            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query_raw = "WITH " +
                    "    star_match as ( " +
                    "    SELECT sim.movieId " +
                    "    FROM stars as s, stars_in_movies as sim " +
                    "    WHERE s.name LIKE ? AND s.id = sim.starId " +
                    "), " +
                    "top as( " +
                    "    SELECT m.id, m.title, m.year, m.director, r.rating, COUNT(m.title) OVER () AS total " +
                    "    FROM ( " +
                    "        SELECT DISTINCT m.id, m.title, m.year, m.director " +
                    "        FROM movies as m, genres_in_movies as gim " +
                    "        WHERE %s AND year LIKE ? AND director LIKE ? " +
                    "          AND id IN (SELECT movieId FROM star_match) " +
                    "          AND m.id = gim.movieId AND gim.genreId LIKE ? " +
                    "         ) as m " +
                    "         LEFT JOIN ratings AS r ON m.id = r.movieId " +
                    "    ORDER BY %s " +
                    "    LIMIT ? " +
                    "    OFFSET ? " +
                    "),stars_count as ( " +
                    "    SELECT sim2.starId, COUNT(DISTINCT sim2.movieId) AS num_movies " +
                    "    FROM stars_in_movies as sim, stars_in_movies as sim2 " +
                    "    WHERE sim.movieId IN ( " +
                    "        SELECT id " +
                    "        FROM top " +
                    "    ) AND sim2.starId = sim.starId " +
                    "    GROUP BY sim2.starId " +
                    ") " +
                    "SELECT m.id, m.title, m.year, m.director, " +
                    "       GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC SEPARATOR ', ') AS genres, " +
                    "       GROUP_CONCAT(DISTINCT g.id ORDER BY g.name ASC SEPARATOR ', ') AS genre_ids, " +
                    "       GROUP_CONCAT(DISTINCT s.name ORDER BY sc.num_movies DESC, s.name ASC SEPARATOR ',') AS star_names, " +
                    "       GROUP_CONCAT(DISTINCT s.id ORDER BY sc.num_movies DESC, s.name ASC SEPARATOR ',') AS star_ids, " +
                    "       m.rating, m.total " +
                    "FROM top AS m, genres_in_movies AS gm, genres AS g, " +
                    "     stars_in_movies AS sim, stars AS s, stars_count AS sc " +
                    "WHERE m.id = gm.movieId AND gm.genreId = g.id AND m.id = sim.movieId AND sim.starId = s.id AND " +
                    "        sc.starId = s.id " +
                    "GROUP BY m.id, m.rating, m.title " +
                    "ORDER BY %s";

            String title_filter;
            int replace_offset = 0;
            if(start_char.equals("") && !title.equals("")) {
                title_filter = "MATCH(title) AGAINST (? IN BOOLEAN MODE)";
            }
            else {
                if (start_char.startsWith("*")) {
                    // browse *
                    title_filter = "title RLIKE '^[^0-9A-Za-z]'";
                    replace_offset = -1;
                } else {
                    // search or browse alphanumeric
                    title_filter = "title LIKE ?";
                }
            }

            String query = String.format(query_raw, title_filter, sort_by.replace("m.rating", "r.rating"), sort_by);

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, star_name);


            // handle browsing by alphanumeric
            if(start_char.equals("")) {
                // search
                if(title.equals("")) {
                    statement.setString(2, "%");
                }
                else {
                    statement.setString(2, title);
                }
            }
            else{
                // browse
                if(!start_char.startsWith("*")) {
                    statement.setString(2, start_char);
                }
            }

            // query replacement with offset based on browse condition
            statement.setString(3+replace_offset, year);
            statement.setString(4+replace_offset, director);
            statement.setString(5+replace_offset, genre);
            statement.setInt(6+replace_offset, page_size);
            statement.setInt(7+replace_offset, Math.max(0, page-1) * page_size);


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
                Number total_rows = rs.getInt("total");

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
                jsonObject.addProperty("total_rows", total_rows);
                jsonObject.addProperty("total_pages", Math.ceil((double) total_rows.intValue() / page_size));

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            long endTimeTJ = System.nanoTime();
            elapsedTimeTJ = endTimeTJ - startTimeTJ;

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

        long endTimeTS = System.nanoTime();
        long elapsedTimeTS = endTimeTS - startTimeTS;

        String file_name = request.getServletContext().getRealPath("/") + "timelog.log";

        request.getServletContext().log("Log file: " + file_name);
        File logFile = new File(file_name);
        if(logFile.createNewFile()) {
            FileWriter fw = new FileWriter(file_name);
            fw.write(elapsedTimeTS + "," + elapsedTimeTJ + "\n");
            fw.close();
        }
        else {
            FileWriter fw = new FileWriter(file_name, true);
            fw.write(elapsedTimeTS + "," + elapsedTimeTJ + "\n");
            fw.close();
        }


    }

}