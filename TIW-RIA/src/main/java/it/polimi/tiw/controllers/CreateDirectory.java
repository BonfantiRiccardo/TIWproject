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

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.Directory;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.DirectoryDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class CreateDirectory
 */
@WebServlet("/CreateDirectory")
@MultipartConfig
public class CreateDirectory extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CreateDirectory() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// GET PARAMETERS FROM request, SEND ERROR IF THEY ARE MISSING
		boolean isBadRequest = false;

		String name = null;
		Date creationDate = null;

		try {
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String today = sdf.format(Calendar.getInstance().getTime());
			creationDate = (Date) sdf.parse(today);

			isBadRequest = name == null || name.isEmpty() || today == null || today.isEmpty();

		} catch (NumberFormatException | NullPointerException | ParseException e) {
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

		// CHECK THAT THE NAME IS NOT A DUPLICATE IN THE ROOT DIRECTORIES
		try {
			directories = directoryDAO.findTopDirectoriesByUser(user.getUsername());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
			return;
		}

		boolean nameOk = true;
		for (Directory d : directories) {
			if (d.getFatherDirectory() == -1 && d.getName().equals(name)) {
				nameOk = false;
				break;
			}
		}
		if (!nameOk) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("A directory with the same name already exists");
			return;
		}

		// EXECUTE SQL COMMAND TO CREATE THE DIRECTORY
		try {
			directoryDAO.createDirectory(name, creationDate, user.getUsername());
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

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
