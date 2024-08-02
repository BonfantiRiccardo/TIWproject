package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class Registration
 */
@WebServlet("/Registration")
public class Registration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Connection connection;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Registration() {
        super();
    }
    
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		// GET PARAMETERS FROM request, SEND ERROR IF THEY ARE MISSING
		String username = null;
		String email = null;
		String password = null;
		String repeatedPassword = null;

		try {
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			email = StringEscapeUtils.escapeJava(request.getParameter("email"));
			password = StringEscapeUtils.escapeJava(request.getParameter("password"));
			repeatedPassword = StringEscapeUtils.escapeJava(request.getParameter("repeatedPassword"));

			if (username == null || password == null || username.isEmpty() || password.isEmpty() ||
					email == null || repeatedPassword == null || email.isEmpty() || repeatedPassword.isEmpty()) {
				throw new Exception("Missing or empty credential value");
			}
			
			//CHECK THAT THE PASSWORD FIELD AND THE REPEATED PASSWORD FIELD ARE THE SAME
			String path;
			if (!password.equals(repeatedPassword)) {
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("errorMsg2", "Password and repeated password are different, they must be the same");
				path = "/index.html";
				templateEngine.process(path, ctx, response.getWriter());
				return;
			}
			
			//CHECK IF THE USERNAME IS UNIQUE OR HAS ALREADY BEEN PICKED
			UserDAO userDao = new UserDAO(connection);
			List<String> usernames = new ArrayList<>();
			try {
				usernames = userDao.getUsernames();
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check credentials");
				return;
			}
			for (String s: usernames) {
				if (s.equals(username)) {
					ServletContext servletContext = getServletContext();
					final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
					ctx.setVariable("errorMsg2", "Username has already been chosen");
					path = "/index.html";
					templateEngine.process(path, ctx, response.getWriter());
					return;
				}
			}
			
			//CHECK EMAIL FORMAT
			String[] splitEmail = email.split("@");
			boolean isGood = true;
			if (splitEmail[0] == null || splitEmail[1] == null || splitEmail[0].isEmpty() || splitEmail[1].isEmpty() || splitEmail.length > 2){
				isGood = false;
			}
			
			if (isGood) {
				String[] domain = splitEmail[1].split("\\.");
				if (domain == null || domain.length < 2 || splitEmail[1].endsWith(".")) {
					isGood = false;
				}

				for (String s: domain) {
					if (s == null || s.isEmpty()) {
						isGood = false;
					}
				}
			}
			
			if(!isGood) {
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("errorMsg2", "Email format is wrong");
				path = "/index.html";
				templateEngine.process(path, ctx, response.getWriter());
				return;
			}

			
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}

		// EXECUTE SQL COMMAND TO CREATE THE USER FOR THE CLIENT
		UserDAO userDao = new UserDAO(connection);
		try {
			userDao.createUser(username, email, password);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check credentials");
			return;
		}
		
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(password);
		
		// ADD INFO TO THE SESSION AND REDIRECT TO HOMEPAGE
		String path;
		request.getSession().setAttribute("user", user);
		path = getServletContext().getContextPath() + "/HomePage";
		response.sendRedirect(path);

	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
