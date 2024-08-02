package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
 * Servlet implementation class GetDocumentDetails
 */
@WebServlet("/GetDocumentDetails")
public class GetDocumentDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetDocumentDetails() {
        super();
    }
    
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		//GET PARAMETERS FROM request, SEND ERROR IF THEY ARE MISSING
		boolean isBadRequest = false;
				
		int documentId = -1;

		try {
			documentId = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("documentid")));

			isBadRequest = documentId == -1;

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
		Document document = null;
		Directory directoryName = null;
		
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
		
		//QUERY THE DATABASE TO FIND THE DIRECTORY WHERE THE DOCUMENT IS, AND ASSIGN THE NAME OF TO THE PARAMETER
		try {
			directoryName = directoryDAO.findDirectoryById(document.getFatherDirectory());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
			return;
		}
		
		if (directoryName == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error in the server occurred, data in the DB is malformed");
			return;
		}
		
		//CREATE JSON AND INSERT IT IN THE RESPONSE
		document.setFatherName(directoryName.getName());
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
