/**
 * 
 */

package org.cluenet.cluebot.reviewinterface.server;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author cobi
 * 
 */
public final class EMF {
	
	private static final EntityManagerFactory emfInstance =
			Persistence.createEntityManagerFactory( "transactions-optional" );
	
	private EMF() {
	}
	
	public static EntityManagerFactory get() {
		return EMF.emfInstance;
	}
}
