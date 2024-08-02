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
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Directory;
import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.DirectoryDAO;
import it.polimi.tiw.dao.DocumentDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class GetContent
 */
@WebServlet("/GetContent")
public class GetContent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Connection connection;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetContent() {
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

		//CHECK IF THE USER HAS LOGGED IN, OTHERWISE REDIRECT TO INDEX/LOGIN
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		
		//GET PARAMETERS FROM request, SEND ERROR IF THEY ARE MISSING
		boolean isBadRequest = false;
				
		int directoryId = -1;

		try {
			directoryId = Integer.parseInt(request.getParameter("directoryId"));

			isBadRequest = directoryId == -1;

		} catch (NumberFormatException | NullPointerException e) {
			isBadRequest = true;
			e.printStackTrace();
		}

		if (isBadRequest) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}
		
		//GET USER FROM SESSION, INITIALIZE DAO TO QUERY THE DATABASE, 
		//INITIALIZE OBJECTS THAT WILL CONTAIN THE DATA TO PASS TO THE NEXT VIEW
		User user = (User) session.getAttribute("user");
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		List<Directory> directories = new ArrayList<Directory>();
		DocumentDAO documentDAO = new DocumentDAO(connection);
		List<Document> documents = new ArrayList<>();
		int previousId = -1;
		String directoryName = null;
		
		//CHECK THAT THE USER IS ACTUALLY THE CREATOR OF THE FATHER DIRECTORY
		try {
			directories = directoryDAO.findDirectoriesByUser(user.getUsername());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to get directory information");
			return;
		}
		
		boolean directoryOk = false;
		for (Directory d: directories) {
			if (d.getId() == directoryId) {
				directoryOk = true;
				previousId = d.getFatherDirectory();
				directoryName = d.getName();
				break;
			}
		}
		if (!directoryOk) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You don't have a directory with this id");
			return;
		}

		//QUERY THE DATABASE TO FIND ALL THE SUBDIRECTORIES OF THE ONE GIVEN AS PARAMETER
		try {
			directories = directoryDAO.findSubdirectories(directoryId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error in the server occurred, it was not possible to get your directories");
			return;
		}
		
		//QUERY THE DATABASE TO FIND ALL THE DOCUMENTS OF THE DIRECTORY GIVEN AS PARAMETER
		try {
			documents = documentDAO.findDocumentsByDirectory(directoryId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error in the server occurred, it was not possible to get your directories");
			return;
		}

		//ACCESS THE DIRECTORY CONTENT PAGE AND ADD THE SUBDIRECTORIES AND THE DOCUMENTS TO THE TEMPLATE CONTEXT
		String path = "/WEB-INF/DirectoryContent.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("directoryName", directoryName);
		if (previousId != -1)
			ctx.setVariable("previousId", previousId);
		ctx.setVariable("directories", directories);
		ctx.setVariable("documents", documents);
		templateEngine.process(path, ctx, response.getWriter());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
