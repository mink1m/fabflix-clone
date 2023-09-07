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

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();


        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String source = request.getParameter("source");
        String mobile = request.getParameter("mobile");

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        boolean dashboard = source != null && source.equals("_dashboard");

        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        // verify reCaptcha
        if(mobile == null) {
            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            } catch (Exception e) {
                JsonObject responseJsonObject = new JsonObject();
                // invalid reCaptcha
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Failed reCAPTCHA");

                responseJsonObject.addProperty("message", "Failed reCAPTCHA");
                // Write JSON string to output
                out.write(responseJsonObject.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);

                out.close();
                return;
            }
        }

        try (Connection conn = dataSource.getConnection()) {

            String query = "SELECT * " +
                    "FROM customers " +
                    "WHERE email = ?";

            if(dashboard) {
                query = "SELECT * " +
                        "FROM employees " +
                        "WHERE email = ?";
            }

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the email
            statement.setString(1, email);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();
            if(rs.next() && VerifyPassword.verifyCredentials(password, rs.getString("password"))) {
                // successful login
                if(dashboard) {
                    request.getSession().setAttribute("employee", new Employee(email));
                }
                else {
                    String user_id = rs.getString("id");
                    request.getSession().setAttribute("user", new User(user_id));
                }

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            }
            else {
                // user not found
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");

                // best practice to not tell user which one is incorrect
                responseJsonObject.addProperty("message", "Incorrect email or password.");
            }

            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(responseJsonObject.toString());
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

    }


    public boolean reCAP(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // recaptcha verification
        // returns true if verified, false if failed.
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse =" + gRecaptchaResponse);
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);    // verify recaptcha
            return true;
        } catch (Exception e) {
            // recaptcha failed
            return false;
        }
    }
}
