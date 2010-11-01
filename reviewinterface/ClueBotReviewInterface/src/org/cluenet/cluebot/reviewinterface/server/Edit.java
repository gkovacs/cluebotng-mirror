/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Query;
import org.cluenet.cluebot.reviewinterface.shared.AdminEdit;
import org.cluenet.cluebot.reviewinterface.shared.Classification;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * @author cobi
 *
 */
@Entity
public class Edit extends Persist {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
	@Basic
	private Integer id;
	
	@Basic
	private Classification classification;
	
	@Basic
	private Integer vandalism;
	
	@Basic
	private Integer constructive;
	
	@Basic
	private Integer skipped;
	
	@Basic
	private Integer required;
	
	@Basic
	private Integer weight;
	
	@Basic
	private List< String > comments;
	
	@Basic
	private List< Key > users;
	
	private Edit( Integer id, Classification classification, Integer required, Integer weight ) {
		this.id = id;
		this.classification = classification;
		this.required = required;
		this.weight = weight;
		this.vandalism = this.constructive = this.skipped = 0;
		this.comments = new ArrayList< String >();
		this.users = new ArrayList< Key >();
		this.persist();
	}

	
	public Integer getId() {
		return id;
	}

	
	public Classification getClassification() {
		return classification;
	}
	
	public void newClassification( User user, Classification classification, String comment ) {
		users.add( user.getKey() );
		if( comment != null )
			comments.add( comment );
		switch( classification ) {
			case VANDALISM:
				vandalism++;
				break;
			case CONSTRUCTIVE:
				constructive++;
				break;
			case SKIPPED:
				skipped++;
				break;
		}
		this.persist();
		if( vandalism == required || constructive == required || skipped == required )
			for( EditGroup eg : EditGroup.findByEdit( this ) )
				eg.editDone( this );
	}
	
	public Integer getVandalism() {
		return vandalism;
	}
	
	public Integer getConstructive() {
		return constructive;
	}

	
	public Integer getSkipped() {
		return skipped;
	}

	
	public Integer getRequired() {
		return required;
	}

	
	public Integer getWeight() {
		return weight;
	}

	
	public List< String > getComments() {
		return new ArrayList< String >( comments );
	}

	
	public List< Key > getUsers() {
		return new ArrayList< Key >( users );
	}

	@Override
	public Key getKey() {
		return this.key;
	}
	
	public org.cluenet.cluebot.reviewinterface.shared.Edit getClientClass() {
		return new org.cluenet.cluebot.reviewinterface.shared.Edit( this.id, this.classification );
	}
	
	public AdminEdit getAdminClass() {
		List< org.cluenet.cluebot.reviewinterface.shared.User > users = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.User >();
		for( Key key : this.users ) {
			User user = User.findByKey( key );
			if( user == null )
				continue;
			users.add( user.getClientClass() );
		}
		return new AdminEdit( id, classification, vandalism, constructive, skipped, required, weight, new ArrayList< String >( comments ), users );
	}
	
	public static Edit findByKey( Key key ) {
		EntityManager em = EMF.get().createEntityManager();
		Edit edit = null;
		try {
			edit = em.find( Edit.class, key );
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
		}
		return edit;
	}
	
	public static Edit findByKey( String key ) {
		return findByKey( KeyFactory.stringToKey( key ) );
	}
	
	public static Edit findById( Integer id ) {
		EntityManager em = EMF.get().createEntityManager();
		Edit edit = null;
		try {
			Query query = em.createQuery( "select from " + Edit.class.getName() + " where id = :theId" );
			query.setParameter( "theId", id );
			edit = (Edit) query.getSingleResult();
		} catch( Exception e ) {
			System.err.println(e.getMessage());
		} finally {
			em.close();
		}
		return edit;
	}
	
	public static Edit newFromId( Integer id, Classification classification, Integer required, Integer weight ) {
		Edit edit = findById( id );
		if( edit == null )
			edit = new Edit( id, classification, required, weight );
		return edit;
	}
	
}
