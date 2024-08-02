package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<String> getUsernames() throws SQLException {
		List<String> usernames = new ArrayList<>();
		PreparedStatement pstatement = null;
		ResultSet result = null;

		String query = "SELECT username FROM user";
		try {
			pstatement = connection.prepareStatement(query);
			result = pstatement.executeQuery();
			if (result.isBeforeFirst())
				while (result.next()) {
					String username = new String();
					username = result.getString("username");
					usernames.add(username);
				}
			
			
		} catch(SQLException e) {
			throw new SQLException(e);
		
		} finally {
			try {
				if(result != null)
					result.close();
				if(pstatement != null)
					pstatement.close();
				
			}catch(Exception e2) {
				throw new SQLException(e2);
		    }
		}
		
		return usernames;
	}
	
	public User checkCredentials(String username, String password) throws SQLException {
		User user = null;
		PreparedStatement pstatement = null;
		ResultSet result = null;
		
		String query = "SELECT username, password FROM user  WHERE username = ? AND password = ?";
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setString(2, password);

			result = pstatement.executeQuery();
			if (result.isBeforeFirst()) {
				result.next();
				user = new User();
				user.setUsername(result.getString("username"));
				user.setPassword(result.getString("password"));
			}
		
		} finally {
			try {
				if(result != null)
					result.close();
				if(pstatement != null)
					pstatement.close();
				
			}catch(Exception e2) {
				throw new SQLException(e2);
		    }
		}
		return user;
	}
	
	public void createUser(String username, String email, String password) throws SQLException {
		String query = "INSERT INTO user (username, email, password) VALUES (?, ?, ?)";
		PreparedStatement pstatement = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setString(2, email);
			pstatement.setString(3, password);

			pstatement.executeUpdate();
		} finally {
			try {
				if(pstatement != null)
					pstatement.close();
				
			}catch(Exception e2) {
				throw new SQLException(e2);
		    }
		}
	}
}
