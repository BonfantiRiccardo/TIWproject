package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.Directory;

public class DirectoryDAO {
	private Connection connection;
	
	public DirectoryDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Directory> findDirectoriesByUser(String username) throws SQLException {
		List<Directory> directories = new ArrayList<>();
		PreparedStatement pstatement = null;
		ResultSet result = null;
		
		String query = "SELECT * FROM cartelle WHERE creatore = ?";
		try  {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			
			result = pstatement.executeQuery();
			
			if (result.isBeforeFirst()) {
				while (result.next()) {
					Directory directory = new Directory();
					directory.setId(result.getInt("idcartella"));
					directory.setName(result.getString("nome"));
					directory.setCreationDate(result.getDate("dataCreazione"));
					directory.setCreator(result.getString("creatore"));
					directory.setFatherDirectory(result.getInt("cartellaPadre"));
					directories.add(directory);
				}
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
		
		return directories;
	}
	
	public List<Directory> findTopDirectoriesByUser(String username) throws SQLException {
		List<Directory> directories = new ArrayList<>();
		PreparedStatement pstatement = null;
		ResultSet result = null;
		
		String query = "SELECT * FROM cartelle WHERE creatore = ? AND cartellaPadre=-1";
		try  {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			
			result = pstatement.executeQuery();
			
			if (result.isBeforeFirst()) {
				while (result.next()) {
					Directory directory = new Directory();
					directory.setId(result.getInt("idcartella"));
					directory.setName(result.getString("nome"));
					directory.setCreationDate(result.getDate("dataCreazione"));
					directory.setCreator(result.getString("creatore"));
					directory.setFatherDirectory(result.getInt("cartellaPadre"));
					directory.setSubdirectories(findSubdirectories(directory.getId()));
					directories.add(directory);
				}
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
		
		return directories;
	}

	public List<Directory> findSubdirectories(int toCheck) throws SQLException {
		List<Directory> subdirectories = new ArrayList<>();
		PreparedStatement pstatement = null;
		ResultSet result = null;
		
		String query = "SELECT * FROM cartelle WHERE cartellaPadre = ?";
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, toCheck);
					
			result = pstatement.executeQuery();
				
			if (result.isBeforeFirst()) {
				while (result.next()) {
					Directory directory = new Directory();
					directory.setId(result.getInt("idcartella"));
					directory.setName(result.getString("nome"));
					directory.setCreationDate(result.getDate("dataCreazione"));
					directory.setCreator(result.getString("creatore"));
					directory.setFatherDirectory(result.getInt("cartellaPadre"));
					directory.setSubdirectories(findSubdirectories(directory.getId()));
					subdirectories.add(directory);
				}
					
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
		return subdirectories;
	}

	public Directory findDirectoryById(int id) throws SQLException {
		PreparedStatement pstatement = null;
		ResultSet result = null;
		Directory directory = null;
		
		String query = "SELECT * FROM cartelle WHERE idcartella = ?";
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, id);
			
			result = pstatement.executeQuery();
			if (result.isBeforeFirst()) {
				result.next();
					
				directory = new Directory();
				directory.setId(result.getInt("idcartella"));
				directory.setName(result.getString("nome"));
				directory.setCreationDate(result.getDate("dataCreazione"));
				directory.setCreator(result.getString("creatore"));
				directory.setFatherDirectory(result.getInt("cartellaPadre"));

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

		return directory;
	}
	
	public void createDirectory(String name, Date creationDate, String creator) throws SQLException { 
		PreparedStatement pstatement = null;
		
		String query = "INSERT INTO cartelle (nome, dataCreazione, creatore, cartellaPadre) VALUES (?, ?, ?, ?)";
		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, name);
			pstatement.setDate(2, new java.sql.Date(creationDate.getTime()));
			pstatement.setString(3, creator);
			pstatement.setInt(4, -1);

			pstatement.executeUpdate();
		}  finally {
			try {
				if(pstatement != null)
					pstatement.close();
				
			}catch(Exception e2) {
				throw new SQLException(e2);
		    }
		}
	}
	
	public void createSubdirectory(String name, Date creationDate, String creator, int fatherDirectory) throws SQLException {
		PreparedStatement pstatement = null;
		
		String query = "INSERT INTO cartelle (nome, dataCreazione, creatore, cartellaPadre) VALUES (?, ?, ?, ?)";
		
		try {
			pstatement = connection.prepareStatement(query);
		
			pstatement.setString(1, name);
			pstatement.setDate(2, new java.sql.Date(creationDate.getTime()));
			pstatement.setString(3, creator);
			pstatement.setInt(4, fatherDirectory);

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
