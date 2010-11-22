package org.cluenet.cluebot.reviewinterface.shared;

import java.io.Serializable;
import java.util.List;


public class Edit implements Serializable {
	private static final long serialVersionUID = -8820112631083306302L;
	public Edit( Integer id, Classification classification, Integer vandalism, Integer constructive, Integer skipped, Integer required, List< String > comments, List< User > users, List< ClientClassification > classifications ) {
		this.id = id;
		this.classification = classification;
		this.vandalism = vandalism;
		this.constructive = constructive;
		this.skipped = skipped;
		this.required = required;
		this.comments = comments;
		this.users = users;
		this.classifications = classifications;
	}
	protected Edit() {
		
	}
	public Integer id;
	public Classification classification;
	public Integer vandalism;
	public Integer constructive;
	public Integer skipped;
	public Integer required;
	public List< String > comments;
	public List< User > users;
	public List< ClientClassification > classifications;
}
