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
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Directory;
import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.DirectoryDAO;
import it.polimi.tiw.dao.DocumentDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class MoveDocToDir
 */
@WebServlet("/MoveDocToDir")
public class MoveDocToDir extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Connection connection;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MoveDocToDir() {
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//CHECK IF THE USER HAS LOGGED IN, OTHERWISE REDIRECT TO INDEX/LOGIN
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		
		//GET PARAMETERS FROM request, SEND ERROR IF THEY ARE MISSING
		boolean isBadRequest = false;
				
		int documentId = -1;
		int directoryId = -1;

		try {
			directoryId = Integer.parseInt(request.getParameter("directoryId"));
			documentId = Integer.parseInt(request.getParameter("documentId"));

			isBadRequest = documentId == -1 || directoryId == -1;

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
		DocumentDAO documentDAO = new DocumentDAO(connection);
		List<Document> documents = new ArrayList<>();
		Document document = null;
		Directory directory = null;
		
		//QUERY THE DATABASE TO FIND THE DOCUMENT
		try {
			document = documentDAO.findDocumentById(documentId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error in the server occurred, it was not possible to get your documents");
			return;
		}
		
		//CHECK THAT THE CREATOR OF THE DOCUMENT IS THE SAME AS THE USER OF THE SESSION
		if (document == null || !user.getUsername().equals(document.getCreator())) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You do not have a document with this id");
			return;
		}
		//CHECK THAT TARGET DIRECTORY IS NOT THE SAME ONE AS THE OLDER ONE
		if (document.getFatherDirectory() == directoryId) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are trying to move a document to the same directory as the previous one");
			return;
		}
		
		//QUERY THE DATABASE TO FIND THE DIRECTORY WHERE THE DOCUMENT IS, AND GET THE FATHER DIRECTORY PARAMETER
		try {
			directory = directoryDAO.findDirectoryById(directoryId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error in the server occurred, it was not possible to get your directories");
			return;
		}
		
		//CHECK THAT THE CREATOR OF THE DIRECTORY IS THE SAME AS THE USER OF THE SESSION
		if (directory == null || !user.getUsername().equals(directory.getCreator())) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You do not have a directory with this id");
			return;
		}
		
		//QUERY THE DATABASE TO FIND THE LIST OF DOCUMENTS OF THE DIRECTORY
		try {
			documents = documentDAO.findDocumentsByDirectory(directoryId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error in the server occurred, it was not possible to get your documents");
			return;
		}
		
		//CHECK IF THERE IS A DOCUMENT WITH THE NAME OF THE ONE I AM TRYING TO MOVE IN THE TARGET DIRECTORY, IN THAT CASE DO NOT MOVE
		boolean isDocumentOk = true;
		for (Document d: documents) {
			if (document.getName().equals(d.getName())) {
				isDocumentOk = false;
				break;
			}
		}
		if (!isDocumentOk) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A document with the same name already extists in the directory, you cannot move the doument here");
			return;
		}
		
		//UPDATE THE DATABASE MOVING THE DOCUMENT IN THE NEW DIRECTORY
		try {
			documentDAO.moveDocumentToDirectory(documentId, directoryId);
		} catch (SQLException e1) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error in the server occurred, it was not possible to move the document");
			return;
		}
		
		//REDIRECT THE USER TO THE DIRECTORY CONTENT PAGE IF ALL WENT SMOOTHLY
		String path = getServletContext().getContextPath() + "/GetContent?directoryId=" + directoryId;
		response.sendRedirect(path);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
