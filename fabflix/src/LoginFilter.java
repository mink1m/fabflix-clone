import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;


        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        boolean is_dashboard = httpRequest.getRequestURI().contains("_dashboard");
        boolean is_api = httpRequest.getRequestURI().contains("api");
        boolean is_user = httpRequest.getSession().getAttribute("user") != null;
        boolean is_employee = httpRequest.getSession().getAttribute("employee") != null;

        if(is_api && (is_user || is_employee)) {
            // valid
            chain.doFilter(request, response);
        } else if(!is_dashboard && !is_user) {

            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");

        } else if (is_dashboard && !is_employee) {

            httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard/login.html");

        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("style.css");
        allowedURIs.add("api/login");
    }

    public void destroy() {
        // ignored.
    }

}
