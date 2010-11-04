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

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * @author cobi
 *
 */
@Entity
public class User extends Persist implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1480988301383776284L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
	@Basic
	private Email email;
	
	@Basic
	private Boolean admin = false;
	
	@Basic
	private String nick = null;
	
	@Basic
	private Integer classifications = 0;
	
	
	/**
	 * 
	 */
	public User( String nick, Email email, Boolean admin ) {
		this.nick = nick;
		this.email = email;
		this.admin = admin;
		this.store();
	}

	
	/**
	 * @return the email
	 */
	public Email getEmail() {
		return email;
	}
	
	public String getNick() {
		if( nick == null )
			return email.getEmail();
		return nick;
	}
	
	public void setNick( String nick ) {
		this.nick = nick;
		this.merge();
	}

	public Boolean isAdmin() {
		return admin;
	}
	
	public void setAdmin( Boolean admin ) {
		this.admin = admin;
		this.merge();
	}
	
	public Integer getClassifications() {
		return classifications;
	}
	
	public org.cluenet.cluebot.reviewinterface.shared.User getClientClass() {
		return new org.cluenet.cluebot.reviewinterface.shared.User( nick, email.getEmail(), classifications, admin, KeyFactory.keyToString( key ) );
	}
	
	public void incClassifications() {
		classifications++;
		this.merge();
	}
	
	/**
	 * @return the key
	 */
	public Key getKey() {
		return key;
	}
	
	@Override
	public void delete() {
		try {
			if( TheCache.cache().containsKey( "User-Email-" + this.email.toString() ) )
				TheCache.cache().remove( "User-Email-" + this.email.toString() );
		} catch( Exception e ) {
			
		}
		super.delete();
	}


	@Override
	public void merge() {
		super.merge();
		try {
			TheCache.cache().put( "User-Email-" + this.email.toString(), this );
		} catch( Exception e ) {
			
		}
	}


	@Override
	public void store() {
		super.store();
		try {
			TheCache.cache().put( "User-Email-" + this.email.toString(), this );
		} catch( Exception e ) {
			
		}
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
		String strKey = KeyFactory.keyToString( key );
		try {
			if( TheCache.cache().containsKey( strKey ) ) {
				User obj = (User) TheCache.cache().get( strKey );
				if( obj != null )
					return obj;
			}
		} catch( Exception e ) {
			
		}
		
		EntityManager em = EMF.get().createEntityManager();
		User person = null;
		try {
			person = em.find( User.class, key );
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
		}
		
		try {
			TheCache.cache().put( strKey, person );
		} catch( Exception e ) {
			
		}
		return person;
	}
	
	public static User findByKey( String key ) {
		return findByKey( KeyFactory.stringToKey( key ) );
	}
	
	public static User findByEmail( Email email ) {
		String strKey = "User-Email-" + email.toString();
		try {
			if( TheCache.cache().containsKey( strKey ) ) {
				User obj = (User) TheCache.cache().get( strKey );
				if( obj != null )
					return obj;
			}
		} catch( Exception e ) {
			
		}
		
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
		
		try {
			TheCache.cache().put( strKey, person );
		} catch( Exception e ) {
			
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
