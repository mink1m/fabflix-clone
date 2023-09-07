import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.imageio.IIOException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "SalesServlet", urlPatterns = "/api/sales")
public class SalesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // create datasource registered in web.xml
    private DataSource dataSource;
    private DataSource masterDataSource;


    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            masterDataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/jdbc/master");

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try(Connection conn = dataSource.getConnection()) {
            HttpSession session = request.getSession();

            User user = (User) session.getAttribute("user");

            if(user == null) {
                user = new User("");
                session.setAttribute("user", user);
            }

            String query = "SELECT s.id, s.movieId, s.saleDate, s.movieQuantity, m.title " +
                    "FROM sales AS s LEFT JOIN movies m on s.movieId = m.id " +
                    "WHERE s.customerId = ? AND s.saleDate = ? ";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, user.getId());
            java.util.Date current = new java.util.Date();
            java.sql.Date now_date = new java.sql.Date(current.getTime());
            statement.setDate(2, now_date);


            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {

                int sale_id = rs.getInt("id");
                int movie_quantity = rs.getInt("movieQuantity");
                String movie_id = rs.getString("movieId");
                String movie_title = rs.getString("title");

                JsonObject obj = new JsonObject();
                obj.addProperty("sale_id", sale_id);
                obj.addProperty("count", movie_quantity);
                obj.addProperty("movie_id", movie_id);
                obj.addProperty("movie_title", movie_title);
                obj.addProperty("price", 8);
                jsonArray.add(obj);
            }

            out.write(jsonArray.toString());
            // set status (200) ok
            response.setStatus(200);

        }
        catch (Exception e) {
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

    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try(Connection conn = masterDataSource.getConnection()) {

            String firstname = request.getParameter("firstname");
            String lastname = request.getParameter("lastname");
            String card = request.getParameter("card");
            String expiration = request.getParameter("expiration");

            HttpSession session = request.getSession();

            ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");
            User user = (User) session.getAttribute("user");

            if(shoppingCart == null || user == null) {
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                if(shoppingCart == null)
                    responseJsonObject.addProperty("message", "empty shopping cart");
                else
                    responseJsonObject.addProperty("message", "invalid user");
                out.write(responseJsonObject.toString());
                response.setStatus(200);
                return;
            }

            String query = "SELECT * " +
                    "FROM creditcards " +
                    "WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, card);
            statement.setString(2, firstname);
            statement.setString(3, lastname);
            statement.setString(4, expiration);

            ResultSet rs = statement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();
            if(!rs.next()) {
                // user not found
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");

                // best practice to not tell user which one is incorrect
                responseJsonObject.addProperty("message", "invalid card details");
            }
            else {
                // successful login
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

                postSaleData(conn, shoppingCart, user.getId());
            }

            rs.close();
            statement.close();

            out.write(responseJsonObject.toString());
            // set status (200) ok
            response.setStatus(200);

        }
        catch (Exception e) {
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

    }

    private void postSaleData(Connection conn, ShoppingCart shoppingCart, String userID) throws SQLException {
        String query_raw = "INSERT INTO sales(customerId, movieId, saleDate, movieQuantity) " +
                "VALUES %s";

        StringBuilder movie_ids_string = new StringBuilder();

        // create ? for each item in cart
        movie_ids_string.append("(?,?,?,?),".repeat(Math.max(0, shoppingCart.size())));
        movie_ids_string.deleteCharAt(movie_ids_string.length() - 1);

        String query = String.format(query_raw, movie_ids_string);

        PreparedStatement statement = conn.prepareStatement(query);

        int i = 0;
        for(String item_id: shoppingCart.getItems()) {
            statement.setString(i+1, userID);
            statement.setString(i+2, item_id);
            java.util.Date current = new java.util.Date();
            java.sql.Date now_date = new java.sql.Date(current.getTime());
            statement.setDate(i+3, now_date);
            statement.setInt(i+4, shoppingCart.get(item_id));
            i+=4;
        }

        int update = statement.executeUpdate();
        if(update == 0) {
            throw new SQLException("Invalid shopping cart or customer data");
        }
    }

}
