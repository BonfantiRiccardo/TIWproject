package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;

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
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MoveDocToDir() {
        super();
    }

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//GET PARAMETERS FROM request, SEND ERROR IF THEY ARE MISSING
		boolean isBadRequest = false;
				
		int documentId = -1;
		int directoryId = -1;

		try {
			documentId = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("documentid")));
			directoryId = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("directoryid")));

			isBadRequest = documentId == -1 || directoryId == -1;

		} catch (NumberFormatException | NullPointerException e) {
			isBadRequest = true;
			e.printStackTrace();
		}

		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}
		
		//GET USER FROM SESSION, INITIALIZE DAO TO QUERY THE DATABASE, 
		//INITIALIZE OBJECTS THAT WILL CONTAIN THE DATA TO PASS TO THE NEXT VIEW
		HttpSession session = request.getSession();
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
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
			return;
		}
		
		//CHECK THAT THE CREATOR OF THE DOCUMENT IS THE SAME AS THE USER OF THE SESSION
		if (document == null || !user.getUsername().equals(document.getCreator())) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("You do not have a document with this id");
			return;
		}
		//CHECK THAT TARGET DIRECTORY IS NOT THE SAME ONE AS THE OLDER ONE
		if (document.getFatherDirectory() == directoryId) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("You are trying to move a document to the same directory as the previous one");
			return;
		}
		
		//QUERY THE DATABASE TO FIND THE DIRECTORY WHERE THE DOCUMENT IS, AND GET THE FATHER DIRECTORY PARAMETER
		try {
			directory = directoryDAO.findDirectoryById(directoryId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
			return;
		}
		
		//CHECK THAT THE CREATOR OF THE DIRECTORY IS THE SAME AS THE USER OF THE SESSION
		if (directory == null || !user.getUsername().equals(directory.getCreator())) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("You do not have a directory with this id");
			return;
		}
		
		//QUERY THE DATABASE TO FIND THE LIST OF DOCUMENTS OF THE DIRECTORY
		try {
			documents = documentDAO.findDocumentsByDirectory(directoryId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
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
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("A document with the same name already extists in the directory, you cannot move the doument here");
			return;
		}
		
		//UPDATE THE DATABASE MOVING THE DOCUMENT IN THE NEW DIRECTORY
		try {
			documentDAO.moveDocumentToDirectory(documentId, directoryId);
		} catch (SQLException e1) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
			return;
		}
		
		//RESPOND WITH SUCCESS MESSAGE
		response.setStatus(HttpServletResponse.SC_OK);
		String json = new Gson().toJson(document);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
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
