/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * @author cobi
 *
 */
@PersistenceCapable(detachable="true")
public class User extends Persist implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1480988301383776284L;

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private Email email;
	
	@Persistent
	private Boolean admin = false;
	
	@Persistent
	private String nick = null;
	
	@Persistent
	private Integer classifications = 0;
	
	
	/**
	 * @param count 
	 * 
	 */
	public User( String nick, Email email, Boolean admin, Integer count ) {
		this.nick = nick;
		this.email = email;
		this.admin = admin;
		this.classifications = count;
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
		this.store();
	}

	public Boolean isAdmin() {
		return admin;
	}
	
	public void setAdmin( Boolean admin ) {
		this.admin = admin;
		this.store();
	}
	
	public Integer getClassifications() {
		return classifications;
	}
	
	public org.cluenet.cluebot.reviewinterface.shared.User getClientClass() {
		return new org.cluenet.cluebot.reviewinterface.shared.User( nick, email.getEmail(), classifications, admin, KeyFactory.keyToString( key ) );
	}
	
	public org.cluenet.cluebot.reviewinterface.shared.User getPublicClientClass() {
		return new org.cluenet.cluebot.reviewinterface.shared.User( nick, "[hidden]", classifications, admin, KeyFactory.keyToString( key ) );
	}
	
	public void incClassifications() {
		classifications++;
		this.store();
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


	@SuppressWarnings( "unchecked" )
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
	
	@SuppressWarnings( "unchecked" )
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
		
		PersistenceManager pm = JDOFilter.getPM();
		User person = null;
		try {
			person = pm.getObjectById( User.class, key );
		} catch( Exception e ) {
			/* Do nothing */
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
	
	@SuppressWarnings( "unchecked" )
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
		
		PersistenceManager pm = JDOFilter.getPM();
		User person = null;
		try {
			Query query = pm.newQuery( "select from " + User.class.getName() + " where email == theEmail" );
			query.declareImports( "import com.google.appengine.api.datastore.Email;" );
			query.declareParameters( "Email theEmail" );
			List< User > list = (List< User >) query.execute( email );
			if( list.size() > 0 )
				person = list.get( 0 );
		} catch( Exception e ) {
			/* Do nothing */
		}
		
		try {
			TheCache.cache().put( strKey, person );
		} catch( Exception e ) {
			
		}
		return person;
	}
	
	@SuppressWarnings( "unchecked" )
	public static List< User > list() {
		PersistenceManager pm = JDOFilter.getPM();
		List< User > list = new ArrayList< User >();
		try {
			Query query = pm.newQuery( "select from " + User.class.getName() );
			list = (List< User >) query.execute();
		} catch( Exception e ) {
			/* Do nothing */
		}

		return list;
	}
}
