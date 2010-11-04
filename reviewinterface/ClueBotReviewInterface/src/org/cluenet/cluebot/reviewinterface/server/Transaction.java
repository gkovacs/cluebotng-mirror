package org.cluenet.cluebot.reviewinterface.server;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;


public class Transaction {
	private static EntityManager em;
	private static EntityTransaction txn;
	public static void begin() {
		em = EMF.get().createEntityManager();
		txn = em.getTransaction();
		txn.begin();
		Persist.use( em );
	}
	public static void end() {
		txn.commit();
	}
	public static void fin() {
		Persist.unuse();
		if( txn.isActive() )
			txn.rollback();
		em.close();
	}
}
