package it.polimi.tiw.dao;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.Document;

public class DocumentDAO {
	private Connection connection;
	
	public DocumentDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Document> findDocumentsByDirectory(int directoryId) throws SQLException {
		List<Document> documents = new ArrayList<>();
		PreparedStatement pstatement = null;
		ResultSet result = null;
		
		String query = "SELECT * FROM documenti WHERE cartellapadre = ?";
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, directoryId);

			result = pstatement.executeQuery();
			
			if (result.isBeforeFirst()){
				while (result.next()) {
					Document document = new Document();
					document.setId(result.getInt("iddocumento"));
					document.setName(result.getString("nome"));
					document.setCreationDate(result.getDate("datacreazione"));
					document.setSummary(result.getString("sommario"));
					document.setType(result.getString("tipo"));
					document.setCreator(result.getString("creatore"));
					document.setFatherDirectory(result.getInt("cartellapadre"));
					documents.add(document);
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
		
		return documents;
	}
	
	public Document findDocumentById(int documentId) throws SQLException {
		Document document = null;
		PreparedStatement pstatement = null;
		ResultSet result = null;

		String query = "SELECT * FROM documenti WHERE iddocumento = ?";
		try {
			pstatement = connection.prepareStatement(query); 
			pstatement.setInt(1, documentId);
			
			result = pstatement.executeQuery();
				
			if (result.isBeforeFirst()) {
				result.next();
					
				document = new Document();
				document.setId(result.getInt("iddocumento"));
				document.setName(result.getString("nome"));
				document.setCreationDate(result.getDate("datacreazione"));
				document.setSummary(result.getString("sommario"));
				document.setType(result.getString("tipo"));
				document.setCreator(result.getString("creatore"));
				document.setFatherDirectory(result.getInt("cartellapadre"));
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
		return document;
	}
	
	public void createDocument(String name, Date creationDate, String summary, String type, 
			String creator, int fatherDirectory) throws SQLException {
		String query = "INSERT INTO documenti (nome, datacreazione, sommario, tipo, creatore, "
				+ "cartellapadre) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			
			pstatement.setString(1, name);
			pstatement.setDate(2, new java.sql.Date(creationDate.getTime()));
			pstatement.setString(3, summary);
			pstatement.setString(4, type);
			pstatement.setString(5, creator);
			pstatement.setInt(6, fatherDirectory);

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
	
	public void moveDocumentToDirectory(int documentId, int newFatherDirectory) throws SQLException {
		String query = "UPDATE documenti SET cartellapadre = ? WHERE iddocumento = ?";
		PreparedStatement pstatement = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			
			pstatement.setInt(1, newFatherDirectory);
			pstatement.setInt(2, documentId);


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
