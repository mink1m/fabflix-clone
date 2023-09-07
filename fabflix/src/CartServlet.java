import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * This CartServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/cart.
 */
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {

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
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // OUTPUT stream
        PrintWriter out = response.getWriter();

        // get connection from data source
        try(Connection conn = dataSource.getConnection()) {

            HttpSession session = request.getSession();

            ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");
            if (shoppingCart == null) {
                shoppingCart = new ShoppingCart();
            }
            // Log to localhost log
            request.getServletContext().log("getting " + shoppingCart.size() + " items");

            JsonArray jsonArray = getCartData(shoppingCart, conn);

            // write all the data into the jsonObject
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
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // OUTPUT stream
        PrintWriter out = response.getWriter();

        // get connection from data source
        try(Connection conn = dataSource.getConnection()) {

            String item = request.getParameter("item");
            int amount = 1;
            try {
                amount = Integer.parseInt(request.getParameter("amount"));
            } catch (NumberFormatException ignored) {
            }
            HttpSession session = request.getSession();

            // get the previous items in a ArrayList
            ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");
            if (shoppingCart == null) {
                shoppingCart = new ShoppingCart();
                shoppingCart.add(item, amount);
                session.setAttribute("shoppingCart", shoppingCart);
            } else {
                // prevent corrupted states through sharing under multi-threads
                // will only be executed by one thread at a time
                synchronized (shoppingCart) {
                    shoppingCart.add(item, amount);
                }
            }

            JsonArray jsonArray = getCartData(shoppingCart, conn);

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
     * handles DELETE request to delete cart item
     */
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // OUTPUT stream
        PrintWriter out = response.getWriter();

        // get connection from data source
        try(Connection conn = dataSource.getConnection()) {

            String item = request.getParameter("item");
            System.out.println(item);
            HttpSession session = request.getSession();

            // get the previous items in a ArrayList
            ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");
            if (shoppingCart == null) {
                shoppingCart = new ShoppingCart();
                session.setAttribute("shoppingCart", shoppingCart);
            } else {
                // prevent corrupted states through sharing under multi-threads
                // will only be executed by one thread at a time
                synchronized (shoppingCart) {
                    shoppingCart.remove(item);
                }
            }

            JsonArray jsonArray = getCartData(shoppingCart, conn);
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
     * Helper function to get cart data
     */
    private JsonArray getCartData(ShoppingCart shoppingCart, Connection conn) throws SQLException {
        String query_raw = "SELECT * " +
                "FROM movies " +
                "WHERE id IN (null %s)";

        StringBuilder movie_ids_string = new StringBuilder();

        // create ? for each item in cart
        movie_ids_string.append(",?".repeat(Math.max(0, shoppingCart.size())));

        String query = String.format(query_raw, movie_ids_string);


        // Declare statement
        PreparedStatement statement = conn.prepareStatement(query);

        int i = 0;
        for(String item_id: shoppingCart.getItems()) {
            statement.setString(i+1, item_id);
            i++;
        }

        ResultSet rs = statement.executeQuery();


        JsonArray jsonArray = new JsonArray();

        while(rs.next()) {

            String movie_id = rs.getString("id");
            String movie_name = rs.getString("title");

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("movie_id", movie_id);
            jsonObject.addProperty("count", shoppingCart.get(movie_id));
            jsonObject.addProperty("movie_title", movie_name);
            jsonObject.addProperty("price", 8);
            jsonArray.add(jsonObject);
        }

        return jsonArray;
    }
}
