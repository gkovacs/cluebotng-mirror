package org.cluenet.cluebot.reviewinterface.shared;

import java.io.Serializable;


public class Edit implements Serializable {
	private static final long serialVersionUID = -3734624398810968477L;
	public Edit( Integer id, Classification classification ) {
		this.id = id;
		this.classification = classification;
		this.weight = 0;
	}
	public Edit( Integer id, Classification classification, Integer weight ) {
		this.id = id;
		this.classification = classification;
		this.weight = weight;
	}
	protected Edit() {
		
	}
	public Integer id;
	public Classification classification;
	public Integer weight;
}
