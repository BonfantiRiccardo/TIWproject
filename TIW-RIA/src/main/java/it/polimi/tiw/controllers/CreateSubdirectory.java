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
 * Servlet implementation class CreateSubdirectory
 */
@WebServlet("/CreateSubdirectory")
@MultipartConfig
public class CreateSubdirectory extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CreateSubdirectory() {
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
		int fatherId = -1;
		Date creationDate = null;

		try {
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			fatherId = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("fatherId")));

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String today = sdf.format(Calendar.getInstance().getTime());
			creationDate = (Date) sdf.parse(today);

			isBadRequest = name == null || name.isEmpty() || fatherId == -1 || today == null || today.isEmpty();

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

		// CHECK THAT THE USER IS ACTUALLY THE CREATOR OF THE FATHER DIRECTORY AND THAT
		// THE NAME IS NOT A DUPLICATE
		try {
			directories = directoryDAO.findDirectoriesByUser(user.getUsername());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Could not query database, retry later");
			return;
		}

		boolean fatherDirectoryOk = false;
		boolean nameOk = true;
		for (Directory d : directories) {
			if (d.getId() == fatherId) {
				for (Directory sub : d.getSubdirectories()) {
					if (sub.getName().equals(name)) {
						nameOk = false;
						break;
					}
				}
				fatherDirectoryOk = true;
				break;
			}
		}
		if (!fatherDirectoryOk) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("You don't have a directory with this id");
			return;
		}
		if (!nameOk) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("A subdirectory with the same name already exist in the father directory");
			return;
		}

		// EXECUTE SQL COMMAND TO CREATE THE DIRECTORY
		try {
			directoryDAO.createSubdirectory(name, creationDate, user.getUsername(), fatherId);
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
