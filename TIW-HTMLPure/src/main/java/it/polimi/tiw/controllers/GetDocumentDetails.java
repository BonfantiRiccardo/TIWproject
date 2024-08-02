package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
 * Servlet implementation class GetDocumentDetails
 */
@WebServlet("/GetDocumentDetails")
public class GetDocumentDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Connection connection;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetDocumentDetails() {
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
				
		int documentId = -1;

		try {
			documentId = Integer.parseInt(request.getParameter("documentId"));

			isBadRequest = documentId == -1;

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
		Document document = null;
		Directory directoryName = null;
		
		//QUERY THE DATABASE TO FIND THE DOCUMENT
		try {
			document = documentDAO.findDocumentById(documentId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error in the server occurred, it was not possible to get your directories");
			return;
		}
		
		//CHECK THAT THE CREATOR OF THE DOCUMENT IS THE SAME AS THE USER OF THE SESSION
		if (document == null || !user.getUsername().equals(document.getCreator())) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You do not have a document with this id");
			return;
		}
		
		//QUERY THE DATABASE TO FIND THE DIRECTORY WHERE THE DOCUMENT IS, AND ASSIGN THE NAME OF TO THE PARAMETER
		try {
			directoryName = directoryDAO.findDirectoryById(document.getFatherDirectory());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error in the server occurred, it was not possible to get your directories");
			return;
		}
		
		if (directoryName == null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error in the server occurred, data in the DB is malformed");
			return;
		}
		
		//ACCESS THE DOCUMENT DETAILS PAGE AND ADD THE DOCUMENT TO THE TEMPLATE CONTEXT
		String path = "/WEB-INF/DocumentDetails.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("document", document);
		ctx.setVariable("fatherId", directoryName.getName());
		ctx.setVariable("previousId", document.getFatherDirectory());
		templateEngine.process(path, ctx, response.getWriter());
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
