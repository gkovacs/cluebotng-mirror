package org.cluenet.cluebot.reviewinterface.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cluenet.cluebot.reviewinterface.shared.Classification;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable="true")
public class EditClassification extends Persist {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7276592967846541799L;
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private Key edit;
	
	@Persistent
	private Classification classification;
	
	@Persistent
	private Key user;
	
	@Persistent
	private String comment;
	
	public EditClassification( Edit edit, Classification classification, User user, String comment ) {
		this.edit = edit.getKey();
		this.classification = classification;
		this.user = user.getKey();
		this.comment = comment;
		this.store();
	}

	
	public Classification getClassification() {
		return classification;
	}

	
	public void setClassification( Classification classification ) {
		this.classification = classification;
		this.store();
	}

	
	public String getComment() {
		return comment;
	}

	
	public void setComment( String comment ) {
		this.comment = comment;
		this.store();
	}

	
	public Edit getEdit() {
		return Edit.findByKey( edit );
	}

	
	public User getUser() {
		return User.findByKey( user );
	}

	@Override
	public Key getKey() {
		return key;
	}
	
}
