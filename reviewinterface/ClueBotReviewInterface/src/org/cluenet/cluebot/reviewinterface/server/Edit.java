/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.Serializable;
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
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;


/**
 * @author cobi
 *
 */
@Entity
public class Edit extends Persist implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4076138576500078491L;

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
		this.store();
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
		this.merge();
		Queue queue = QueueFactory.getQueue( "edit-done-queue" );
		for( EditGroup eg : EditGroup.findByEdit( this ) )
			queue.add( TaskOptions.Builder.param( "ekey", KeyFactory.keyToString( key ) ).param( "egkey", KeyFactory.keyToString( eg.getKey() ) ).method( Method.GET ) );
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
		if( required < 2 )
			return 2;
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
		return new AdminEdit( id, classification, vandalism, constructive, skipped, getRequired(), weight, new ArrayList< String >( comments ), users );
	}
	
	@Override
	public void delete() {
		try {
			if( TheCache.cache().containsKey( "Edit-Id-" + this.id.toString() ) )
				TheCache.cache().remove( "Edit-Id-" + this.id.toString() );
		} catch( Exception e ) {
			
		}
		super.delete();
	}


	@Override
	public void merge() {
		super.merge();
		try {
			TheCache.cache().put( "Edit-Id-" + this.id.toString(), this );
		} catch( Exception e ) {
			
		}
	}


	@Override
	public void store() {
		super.store();
		try {
			TheCache.cache().put( "Edit-Id-" + this.id.toString(), this );
		} catch( Exception e ) {
			
		}
	}


	public static Edit findByKey( Key key ) {
		String strKey = KeyFactory.keyToString( key );
		try {
			if( TheCache.cache().containsKey( strKey ) ) {
				Edit obj = (Edit) TheCache.cache().get( strKey );
				if( obj != null )
					return obj;
			}
				
		} catch( Exception e ) {
			
		}
		
		EntityManager em = EMF.get().createEntityManager();
		Edit edit = null;
		try {
			edit = em.find( Edit.class, key );
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
		}
		
		try {
			TheCache.cache().put( strKey, edit );
		} catch( Exception e ) {
			
		}
		return edit;
	}
	
	public static Edit findByKey( String key ) {
		return findByKey( KeyFactory.stringToKey( key ) );
	}
	
	public static Edit findById( Integer id ) {
		String strKey = "Edit-Id-" + id.toString();
		try {
			if( TheCache.cache().containsKey( strKey ) ) {
				Edit obj = (Edit) TheCache.cache().get( strKey );
				if( obj != null )
					return obj;
			}
		} catch( Exception e ) {
			
		}
		
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
		
		try {
			TheCache.cache().put( strKey, edit );
		} catch( Exception e ) {
			
		}
		return edit;
	}
	
	public static Edit newFromId( Integer id, Classification classification, Integer required, Integer weight ) {
		Edit edit = findById( id );
		if( edit == null )
			edit = new Edit( id, classification, required, weight );
		return edit;
	}
	
	@SuppressWarnings( "unchecked" )
	public static List< Key > list() {
		EntityManager em = EMF.get().createEntityManager();
		List< Key > list = new ArrayList< Key >();
		try {
			Query query = em.createQuery( "select from " + Edit.class.getName() );
			for( Edit edit : (List< Edit >) query.getResultList() )
				list.add( edit.getKey() );
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
		}
		return list;
	}
}
