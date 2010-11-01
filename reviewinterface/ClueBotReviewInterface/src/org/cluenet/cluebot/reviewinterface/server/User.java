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

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * @author cobi
 *
 */
@Entity
public class User extends Persist {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
	@Basic
	private Email email;
	
	@Basic
	private Boolean admin = false;
	
	@Basic
	private Integer classifications = 0;
	
	
	/**
	 * 
	 */
	public User( Email email, Boolean admin ) {
		this.email = email;
		this.admin = admin;
		this.persist();
	}

	
	/**
	 * @return the email
	 */
	public Email getEmail() {
		return email;
	}

	public Boolean isAdmin() {
		return admin;
	}
	
	public void setAdmin( Boolean admin ) {
		this.admin = admin;
		this.persist();
	}
	
	public Integer getClassifications() {
		return classifications;
	}
	
	public org.cluenet.cluebot.reviewinterface.shared.User getClientClass() {
		return new org.cluenet.cluebot.reviewinterface.shared.User( email.getEmail(), classifications, admin, KeyFactory.keyToString( key ) );
	}
	
	public void incClassifications() {
		classifications++;
		this.persist();
	}
	
	/**
	 * @return the key
	 */
	public Key getKey() {
		return key;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "User {\n" +
				"\temail = " + this.email.toString() + "\n" +
				"\tkey = " + this.key.toString() + "\n" +
				"\tadmin = " + this.admin.toString()+ "\n" +
				"}";
	}
	
	public static User findByKey( Key key ) {
		EntityManager em = EMF.get().createEntityManager();
		User person = null;
		try {
			person = em.find( User.class, key );
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
		}
		return person;
	}
	
	public static User findByKey( String key ) {
		return findByKey( KeyFactory.stringToKey( key ) );
	}
	
	public static User findByEmail( Email email ) {
		EntityManager em = EMF.get().createEntityManager();
		User person = null;
		try {
			Query query = em.createQuery( "select from " + User.class.getName() + " where email = :email" );
			query.setParameter( "email", email );
			person = (User) query.getSingleResult();
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
		}
		return person;
	}
	
	@SuppressWarnings( "unchecked" )
	public static List< User > list() {
		EntityManager em = EMF.get().createEntityManager();
		List< User > list = new ArrayList< User >();
		try {
			Query query = em.createQuery( "select from " + User.class.getName() );
			list = new ArrayList< User >( (List< User >) query.getResultList() );
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
		}
		return list;
	}
}
