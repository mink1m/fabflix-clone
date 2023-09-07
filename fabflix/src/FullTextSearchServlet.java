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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "FullTextSearchServlet", urlPatterns = "/api/full_text_search")
public class FullTextSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

//    private static final String[] stopwords = {"a","about","an","are","as","at","be","by","com","de","en","for","from","how","i","in","is","it","la","of","on","or","that","the","this","to","was","what","when","where","who","will","with","und","the","www"};
    private static Set<String> stopwords = new HashSet<>();

    static {
        stopwords.addAll(Arrays.asList(
                "a","about","an","are","as","at","be","by","com","de","en","for","from","how","i","in","is","it","la","of","on","or","that","the","this","to","was","what","when","where","who","will","with","und","the","www"));
    }


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

        // query
        String query_param = process_query_string(request.getParameter("query"));

        // The log message can be found in localhost log
        request.getServletContext().log("Searching");
        request.getServletContext().log("Query: " + query_param);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT * FROM movies " +
                    "WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE) " +
                    "LIMIT 10";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, query_param);


            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");

                jsonArray.add(generateJsonObject(movie_id, movie_title));

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


    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "heroID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieId, String movieTitle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movie_id", movieId);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }

    public static String process_query_string(String query_string) {
        if(query_string == null || query_string.trim().length() == 0) {
            return "";
        }

        String[] s = query_string.split(" ");

        StringBuilder new_string = new StringBuilder();

        for (String value : s) {
            if(!stopwords.contains(value.toLowerCase())) {
                new_string.append("+").append(value.trim()).append("* ");
            }
        }

        return new_string.toString();

    }

}