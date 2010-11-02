/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * @author cobi
 *
 */
public abstract class Persist implements Serializable {
	private static EntityManager em;
	private static Boolean use = false;
	
	
	public static void use( EntityManager em ) {
		Persist.em = em;
		Persist.use = true;
	}
	
	public static void unuse() {
		Persist.em = null;
		Persist.use = false;
	}
	
	public abstract Key getKey();
	public static void persist( Persist o ) {
		if( use )
			if( Persist.em.contains( o ) )
				Persist.em.flush();
			else
				Persist.em.persist( o );
		else {
			EntityManager em = EMF.get().createEntityManager();
			try {
				EntityTransaction txn = em.getTransaction();
				txn.begin();
				try {
					if( em.contains( o ) )
						em.flush();
					else
						em.persist( o );
					txn.commit();
				} catch( Exception e ) {
					txn.rollback();
				}
			} finally {
				em.close();
			}
		}
		
		String strKey = KeyFactory.keyToString( o.getKey() );
		try {
			TheCache.cache().put( strKey, o );
		} catch( Exception e ) {
			
		}
	}
	
	public void persist() {
		Persist.persist( this );
	}
	public static void store( Persist o ) {
		Persist.persist( o );
	}
	public void store() {
		Persist.store( this );
	}
	public static void delete( Persist o ) {
		String strKey = KeyFactory.keyToString( o.getKey() );
		try {
			if( TheCache.cache().containsKey( strKey ) )
				TheCache.cache().remove( strKey );
		} catch( Exception e ) {
			
		}
		
		if( use ) {
			Query q = Persist.em.createQuery( "DELETE FROM " + o.getClass().getName() + " WHERE key = :key" );
			q.setParameter( "key", o.getKey() );
			q.executeUpdate();
		} else {
			EntityManager em = EMF.get().createEntityManager();
			try {
				EntityTransaction txn = em.getTransaction();
				txn.begin();
				Query q = em.createQuery( "DELETE FROM " + o.getClass().getName() + " WHERE key = :key" );
				q.setParameter( "key", o.getKey() );
				q.executeUpdate();
				txn.commit();
			} catch( Exception e ) {
				/* Do nothing */
			} finally {
				em.close();
			}
		}
	}
	public void delete() {
		Persist.delete( this );
	}
}
