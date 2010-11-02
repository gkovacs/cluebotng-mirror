/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.Serializable;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * @author cobi
 *
 */
public abstract class Persist implements Serializable {
	public abstract Key getKey();
	public static void persist( Persist o ) {
		String strKey = KeyFactory.keyToString( o.getKey() );
		
		EntityManager em = EMF.get().createEntityManager();
		try {
			em.getTransaction().begin();
			em.persist( o );
			em.getTransaction().commit();
		} finally {
			em.close();
		}
		
		TheCache.cache().put( strKey, o );
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
		
		if( TheCache.cache().containsKey( strKey ) )
			TheCache.cache().remove( strKey );
		
		EntityManager em = EMF.get().createEntityManager();
		try {
			em.getTransaction().begin();
			Query q = em.createQuery( "DELETE FROM " + o.getClass().getName() + " WHERE key = :key" );
			q.setParameter( "key", o.getKey() );
			q.executeUpdate();
			em.getTransaction().commit();
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
		}
	}
	public void delete() {
		Persist.delete( this );
	}
}
