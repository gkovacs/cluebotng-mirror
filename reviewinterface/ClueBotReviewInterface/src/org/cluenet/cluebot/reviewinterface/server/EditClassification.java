package org.cluenet.cluebot.reviewinterface.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cluenet.cluebot.reviewinterface.shared.Classification;
import org.cluenet.cluebot.reviewinterface.shared.ClientClassification;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

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

	public ClientClassification getClientClass( Boolean admin ) {
		return getClientClass( admin, false );
	}

	public ClientClassification getClientClass( Boolean admin, Boolean fill ) {
		org.cluenet.cluebot.reviewinterface.shared.User user;
		if( admin )
			user = getUser().getClientClass();
		else
			user = getUser().getPublicClientClass();
		if( fill )
			return new ClientClassification(
					KeyFactory.keyToString( key ),
					user,
					comment,
					classification,
					getEdit().getLightClass()
			);
		else
			return new ClientClassification(
					KeyFactory.keyToString( key ),
					user,
					comment,
					classification,
					KeyFactory.keyToString( edit )
			);
	}
	
	@SuppressWarnings( "unchecked" )
	public static List< EditClassification > findByUser( User user, Key start, Integer count ) {
		PersistenceManager pm = JDOFilter.getPM();
		List< EditClassification > list = new ArrayList< EditClassification >();
		
		String inequality = count < 0 ? ">" : "<";
		String order = count < 0 ? "asc" : "desc";
		count = Math.abs( count );
		
		try {
			Query q = pm.newQuery( EditClassification.class );
			if( start != null )
				q.setFilter( "user == theUser && key " + inequality + " theKey" );
			else
				q.setFilter( "user == theUser" );
			q.setOrdering( "key " + order );
			q.setRange( 0, count );
			q.declareImports( "import com.google.appengine.api.datastore.Key;" );
			if( start != null ) {
				q.declareParameters( "Key theUser, Key theKey" );
				list = (List< EditClassification >) q.execute( user.getKey(), start );
			} else {
				q.declareParameters( "Key theUser" );
				list = (List< EditClassification >) q.execute( user.getKey() );
			}
			if( order.equals( "asc" ) ) {
				list = new ArrayList< EditClassification >( list );
				Collections.reverse( list );
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	@SuppressWarnings( "unchecked" )
	public static EditClassification findByEditUser( Edit edit, User user ) {
		PersistenceManager pm = JDOFilter.getPM();
		
		try {
			Query q = pm.newQuery( EditClassification.class );
			q.setFilter( "user == theUser && edit == theEdit" );
			q.setRange( 0, 1 );
			q.declareImports( "import com.google.appengine.api.datastore.Key;" );
			q.declareParameters( "Key theUser, Key theEdit" );
			List< EditClassification > list = (List< EditClassification >) q.execute( user.getKey(), edit.getKey() );
			if( list.size() > 0 )
				return list.get( 0 );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		
		return null;
	}
}
