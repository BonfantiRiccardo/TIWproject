package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
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
 * Servlet implementation class CreateDocument
 */
@WebServlet("/CreateDocument")
public class CreateDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Connection connection;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateDocument() {
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

		//CHECK IF THE USER HAS LOGGED IN, OTHERWISE REDIRECT TO INDEX/LOGIN
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			String loginpath = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(loginpath);
			return;
		}

		//GET PARAMETERS FROM request, SEND ERROR IF THEY ARE MISSING
		boolean isBadRequest = false;
		
		String name = null;
		String summary = null;
		String type = null;
		int fatherId = -1;
		Date creationDate = null;

		try {
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			fatherId = Integer.parseInt(request.getParameter("fatherId"));
			summary = StringEscapeUtils.escapeJava(request.getParameter("summary"));
			type = StringEscapeUtils.escapeJava(request.getParameter("type"));
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String today = sdf.format(Calendar.getInstance().getTime());
			creationDate = (Date) sdf.parse(today);
			
			isBadRequest = name == null || name.isEmpty() || fatherId == -1 || summary == null || summary.isEmpty() || type == null
					|| type.isEmpty() || today == null || today.isEmpty();
			
		} catch (NumberFormatException | NullPointerException | ParseException e) {
			isBadRequest = true;
			e.printStackTrace();
		}
		if (isBadRequest) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}
		
		//PREPARE OBJECTS FOR QUERIES
		User user = (User) session.getAttribute("user");
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		DocumentDAO documentDAO = new DocumentDAO(connection);
		List<Directory> directories = new ArrayList<>();
		List<Document> documents = new ArrayList<>();
		
		//CHECK THAT THE USER IS ACTUALLY THE CREATOR OF THE FATHER DIRECTORY
		try {
			directories = directoryDAO.findDirectoriesByUser(user.getUsername());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create document");
			return;
		}
		
		boolean fatherDirectoryOk = false;
		for (Directory d: directories) {
			if (d.getId() == fatherId) {
				fatherDirectoryOk = true;
				break;
			}
		}
		if (!fatherDirectoryOk) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You don't have a directory with this id");
			return;
		}
		
		//CHECK THAT THE NAME OF THE DOCUMENT IS NOT A DUPLICATE IN THE DIRECTORY
		try {
			documents = documentDAO.findDocumentsByDirectory(fatherId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create document");
			return;
		}
		boolean nameOk = true;
		for (Document doc: documents) {
			if (doc.getName().equals(name)) {
				nameOk = false;
				break;
			}
		}
		if (!nameOk) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A document with the same name already exist in the directory");
			return;
		}
		
		// EXECUTE SQL COMMAND TO CREATE THE DIRECTORY
		try {
			documentDAO.createDocument(name, creationDate, summary, type, user.getUsername(), fatherId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create mission");
			return;
		}

		//REDIRECT THE USER TO THE DIRECTORY WHERE HE ADDED THE DOCUMENT
		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/GetContent?directoryId=" + fatherId;
		
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
