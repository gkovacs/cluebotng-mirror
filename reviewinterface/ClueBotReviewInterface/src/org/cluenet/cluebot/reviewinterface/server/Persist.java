/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.appengine.api.datastore.Key;


/**
 * @author cobi
 *
 */
public abstract class Persist {
	public abstract Key getKey();
	public static void persist( Persist o ) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			em.getTransaction().begin();
			em.persist( o );
			em.getTransaction().commit();
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
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
