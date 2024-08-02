package it.polimi.tiw.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter implementation class LoginChecker
 */
public class LoginChecker implements Filter {

	/**
	 * Default constructor.
	 */
	public LoginChecker() {
	}
	

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		System.out.print("Login checker filter executing ...\n");

		//GETS request AND response OBJECTS AND PREPARES THE PATH
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginpath = req.getServletContext().getContextPath() + "/index.html";

		//IF THE SESSION IS NEW OR THERE IS NO USER, REDIRECT TO LOGIN
		HttpSession s = req.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			res.setStatus(403);						//CODE 403 FORBIDDEN
			res.setHeader("Location", loginpath);
			System.out.print("Login checker FAILED...\n");
			return;
		}

		//IF ALL GOOD CONTINUE FILTER CHAIN
		chain.doFilter(request, response);
	}
}
