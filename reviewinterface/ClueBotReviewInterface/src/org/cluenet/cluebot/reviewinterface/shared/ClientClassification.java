package org.cluenet.cluebot.reviewinterface.shared;

import java.io.Serializable;


public class ClientClassification implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4327593001742801414L;
	public ClientClassification( String strKey, User user, String comment, Classification classification, String editKey ) {
		super();
		this.key = strKey;
		this.user = user;
		this.comment = comment;
		this.classification = classification;
		this.editKey = editKey;
		this.edit = null;
	}
	
	public ClientClassification( String strKey, User user, String comment, Classification classification, Edit edit ) {
		super();
		this.key = strKey;
		this.user = user;
		this.comment = comment;
		this.classification = classification;
		this.editKey = null;
		this.edit = edit;
	}
	
	protected ClientClassification() {
		
	}
	
	public String key;
	public User user;
	public String comment;
	public Classification classification;
	public String editKey;
	public Edit edit;
}
