package org.cluenet.cluebot.reviewinterface.server;

import java.util.Collections;

import javax.cache.Cache;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;


public class TheCache {
	private static CacheFactory factory = null;
	private static Cache cache = null;
	
	private static void init() {
		try {
			factory = CacheManager.getInstance().getCacheFactory();
			cache = factory.createCache( Collections.emptyMap() );
		} catch( Exception e ) {
			/* Do nothing. */
		}
	}
	
	public static Cache cache() {
		if( cache == null )
			init();
		return cache;
	}
}
