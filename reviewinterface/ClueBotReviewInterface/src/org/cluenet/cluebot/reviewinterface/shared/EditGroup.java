package org.cluenet.cluebot.reviewinterface.shared;

import java.io.Serializable;
import java.util.List;


public class EditGroup implements Serializable {

	private static final long serialVersionUID = 5609605098542355175L;
	public EditGroup( String key, String name, List< Edit > edits, List< Edit > reviewed, List< Edit > done, Integer weight, Integer countDone, Integer countReviewed, Integer countLeft ) {
		this.key = key;
		this.name = name;
		this.edits = edits;
		this.reviewed = reviewed;
		this.done = done;
		this.weight = weight;
		this.countDone = countDone;
		this.countReviewed = countReviewed;
		this.countLeft = countLeft;
	}
	
	protected EditGroup() {
		
	}
	
	public String key;
	public String name;
	public List< Edit > edits;
	public List< Edit > reviewed;
	public List< Edit > done;
	public Integer weight;
	public Integer countDone;
	public Integer countReviewed;
	public Integer countLeft;
}
