package org.cluenet.cluebot.reviewinterface.shared;

import java.io.Serializable;
import java.util.List;


public class EditGroup implements Serializable {

	private static final long serialVersionUID = 5609605098542355175L;
	public EditGroup( String key, String name, List< AdminEdit > edits, List< AdminEdit > done, Integer weight ) {
		this.key = key;
		this.name = name;
		this.edits = edits;
		this.done = done;
		this.weight = weight;
	}
	
	protected EditGroup() {
		
	}
	
	public String key;
	public String name;
	public List< AdminEdit > edits;
	public List< AdminEdit > done;
	public Integer weight;
}
