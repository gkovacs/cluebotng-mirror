package org.cluenet.cluebot.reviewinterface.shared;

import java.io.Serializable;


public class ReturnData implements Serializable {

	private static final long serialVersionUID = -4163940901833898539L;
	public Edit edit;
	public User user;
	public ReturnData( Edit edit, User user ) {
		this.edit = edit;
		this.user = user;
	}
	
	protected ReturnData() {
		
	}
}
