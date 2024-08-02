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

import it.polimi.tiw.beans.Directory;
import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.DirectoryDAO;
import it.polimi.tiw.dao.DocumentDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class EliminateDirectory
 */
@WebServlet("/DeleteDirectory")
public class DeleteDirectory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteDirectory() {
        super();
    }
    
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// GET PARAMETERS FROM request, SEND ERROR IF THEY ARE MISSING
		boolean isBadRequest = false;

		int directoryId = -1;

		try {
			directoryId = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("directoryid")));

			isBadRequest = (directoryId == -1);

		} catch (NumberFormatException | NullPointerException e) {
			isBadRequest = true;
			e.printStackTrace();
		}
		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}

		// PREPARE OBJECTS FOR QUERIES
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		List<Directory> directories = new ArrayList<>();
		DocumentDAO documentDAO = new DocumentDAO(connection);
		List<Document> documents = new ArrayList<>();

		// CHECK THAT THE USER IS ACTUALLY THE CREATOR OF THE DIRECTORY
		try {
			directories = directoryDAO.findDirectoriesByUser(user.getUsername());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
			return;
		}

		boolean directoryOk = false;
		Directory toDelete = null;
		for (Directory d : directories) {
			if (d.getId() == directoryId) {
				toDelete = d;
				directoryOk = true;
				break;
			}
		}
		if (!directoryOk || toDelete == null || !toDelete.getCreator().equals(user.getUsername())) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("You don't have a directory with this id");
			return;
		}

		//PREPARE THE LIST OF ALL THE DOCUMENTS OF THE USER
		try {
			documents = documentDAO.findDocumentsByUser(user.getUsername());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
			return;
		}
		
		// EXECUTE SQL COMMAND TO DELETE THE DIRECTORY AND ANY SUBDIRECTORIES / DOCUMENTS WITHIN
		try {
			deleteContent(toDelete, documents);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
			return;
		}

		// NOTIFY BROWSER REQUEST WAS SUCCESSFULL
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
	}
	
	private void deleteContent(Directory toDelete, List<Document> documents) throws SQLException {
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		DocumentDAO documentDAO = new DocumentDAO(connection);
		
		for (Directory d: toDelete.getSubdirectories()) {
			deleteContent(d, documents);
		}
		
		for (Document d: documents) {
			if (toDelete.getId() == d.getFatherDirectory()) {
				System.out.println("Deleting doc: " + d.getName());
				documentDAO.deleteDocument(d.getId());
			}
		}
		System.out.println("Deleting Dir: " + toDelete.getName());
		directoryDAO.deleteDirectory(toDelete.getId());
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
