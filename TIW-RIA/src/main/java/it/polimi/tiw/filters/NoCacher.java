package it.polimi.tiw.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet Filter implementation class LoginChecker
 */
public class NoCacher implements Filter {

    /**
     * Default constructor. 
     */
    public NoCacher() {
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
		System.out.print("No cacher filter executing ...\n");
		//GET response OBJECT
		HttpServletResponse res = (HttpServletResponse) response;
		
		//SET HEADERS IN THE RESPONSE SO THAT THE BROWSER DOES NOT STORE ELEMENTS IN CACHE
		res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
		res.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		res.setHeader("Expires", "0"); // Proxies.
		
		//CONTINUE FILTER CHAIN
		chain.doFilter(request, response);
	}
}
