package org.cluenet.cluebot.reviewinterface.server;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


public class JDOFilter implements Filter {
	private static final ThreadLocal< PersistenceManager > PersistenceManagers = 
		new ThreadLocal< PersistenceManager > () {
		
			@Override
			protected PersistenceManager initialValue() {
				return null;
			}
			
		};

    public static PersistenceManager getPM() {
        return PersistenceManagers.get();
    }
	
	
	@Override
	public void destroy() {}
	
	@Override
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {
		JDOFilter.PersistenceManagers.set( PMF.get().getPersistenceManager() );
		chain.doFilter( request, response );
		if( !JDOFilter.getPM().isClosed() )
			JDOFilter.getPM().close();
	}
	
	@Override
	public void init( FilterConfig filterConfig ) throws ServletException {}
	
}
