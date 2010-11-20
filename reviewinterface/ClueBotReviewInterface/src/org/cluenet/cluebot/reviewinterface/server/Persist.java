/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.Serializable;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * @author cobi
 *
 */
public abstract class Persist implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4649217473218364991L;
	
	public abstract Key getKey();
	
	@SuppressWarnings( "unchecked" )
	public static void persist( Persist o ) {
		PersistenceManager pm = JDOFilter.getPM();
		Transaction txn = pm.currentTransaction();
		try {
			txn.begin();
			pm.makePersistent( o );
			txn.commit();
		} catch( Exception e ) {
			if( txn.isActive() )
				txn.rollback();
			System.err.println( "Exception!" );
			e.printStackTrace( System.err );
		}
		
		String strKey = KeyFactory.keyToString( o.getKey() );
		try {
			TheCache.cache().put( strKey, o );
		} catch( Exception e ) {
			
		}
	}
	
	public static void store( Persist o ) {
		Persist.persist( o );
	}
	public void store() {
		Persist.persist( this );
	}
	public static void delete( Persist o ) {
		String strKey = KeyFactory.keyToString( o.getKey() );
		try {
			if( TheCache.cache().containsKey( strKey ) )
				TheCache.cache().remove( strKey );
		} catch( Exception e ) {
			
		}
		
		PersistenceManager pm = JDOFilter.getPM();
		try {
			Transaction txn = pm.currentTransaction();
			txn.begin();
			pm.setDetachAllOnCommit( true );
			pm.deletePersistent( o );
			txn.commit();
		} catch( Exception e ) {
			/* Do nothing */
		}
	}
	public void delete() {
		Persist.delete( this );
	}
}
