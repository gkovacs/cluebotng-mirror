package org.cluenet.cluebot.reviewinterface.shared;

import java.io.Serializable;


public class User implements Serializable {

	private static final long serialVersionUID = -4008082715014392057L;
	public User( String userName, Integer classifications, Boolean isAdmin, String key ) {
		this.userName = userName;
		this.classifications = classifications;
		this.isAdmin = isAdmin;
		this.key = key;
	}
	protected User() {
		
	}
	public String key;
	public String userName;
	public Integer classifications;
	public Boolean isAdmin;
}
