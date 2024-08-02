package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class Registration
 */
@WebServlet("/Registration")
@MultipartConfig
public class Registration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Registration() {
        super();
    }
    
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//GET PARAMETERS FROM THE request, SEND ERROR IF THEY ARE NULL
		String username = null;	
		String email = null;
		String password = null;
		String repeatedPassword = null;
		username = StringEscapeUtils.escapeJava(request.getParameter("username"));
		email = StringEscapeUtils.escapeJava(request.getParameter("email"));
		password = StringEscapeUtils.escapeJava(request.getParameter("password"));
		repeatedPassword = StringEscapeUtils.escapeJava(request.getParameter("repeatedPassword"));
		
		if (username == null || password == null || username.isEmpty() || password.isEmpty() ||
				email == null || email.isEmpty() || repeatedPassword == null || repeatedPassword.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Credentials must be not null");
			return;
		}
		
		//CHECK THAT THE PASSWORD FIELD AND THE REPEATED PASSWORD FIELD ARE THE SAME
		if (!password.equals(repeatedPassword)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Password and Repeated password fields must be the same");
			return;
		}
		
		//CHECK IF THE USERNAME IS UNIQUE OR HAS ALREADY BEEN PICKED
		UserDAO userDao = new UserDAO(connection);
		List<String> usernames = new ArrayList<>();
		try {
			usernames = userDao.getUsernames();
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
			return;
		}
		for (String s: usernames) {
			if (s.equals(username)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Username has already been chosen");
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
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Email format is wrong");
			return;
		}
		
		
		//EXECUTE SQL COMMAND TO CREATE THE USER FOR THE CLIENT
		try {
			userDao.createUser(username, email, password);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
			return;
		}
		
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(password);

		//ADD INFO TO THE SESSION IF ALL WAS GOOD AND REDIRECT TO HOMEPAGE, 
		//OTHERWISE RETURN AN ERROR STATUS CODE AND MESSAGE
		request.getSession().setAttribute("user", user);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(username);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
