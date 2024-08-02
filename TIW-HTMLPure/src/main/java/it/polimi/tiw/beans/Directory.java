package it.polimi.tiw.beans;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Directory {
	private int id;
	private String name;
	private Date creationDate;
	private String creator;
	private int fatherDirectory;
	private Boolean isTop;
	private List<Directory> subdirectories = new ArrayList<>();
	
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public int getFatherDirectory() {
		return fatherDirectory;
	}

	public void setFatherDirectory(int fatherDirectory) {
		this.fatherDirectory = fatherDirectory;
		if (fatherDirectory == -1)
			setTop(true);
		else
			setTop(false);
	}

	public boolean isTop() {
		return isTop;
	}

	public void setTop(boolean isTop) {
		this.isTop = isTop;
	}

	public List<Directory> getSubdirectories() {
		return subdirectories;
	}

	public void setSubdirectories(List<Directory> subdirectories) {
		this.subdirectories = subdirectories;
	}
}
