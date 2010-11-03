package org.cluenet.cluebot.reviewinterface.server;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


public class Authentication implements Filter {
    private FilterConfig filterConfig;

	private static void redirectToLogin( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		resp.sendRedirect( userService.createLoginURL( req.getRequestURI() ) );
	}
	
	public static boolean isAdmin( HttpServletRequest req ) {
		if( req.getUserPrincipal() == null )
			return false;
		UserService userService = UserServiceFactory.getUserService();
		if( userService.isUserAdmin() )
			return true;
		User user = User.findByEmail( new Email( req.getUserPrincipal().getName() ) );
		if( user == null )
			return false;
		return user.isAdmin();
	}
	
	public static boolean isPrivileged( HttpServletRequest req ) {
		if( isAdmin( req ) )
			return true;
		Principal user = req.getUserPrincipal();
		if( user == null )
			return false;
		if( User.findByEmail( new Email( user.getName() ) ) != null )
			return true;
		return false;
	}
	
	private static boolean requireAdmin( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
		if( isAdmin( req ) )
			return true;
		redirectToLogin(req, resp);
		return false;
	}
	
	private static boolean requirePrivileged( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
		if( isPrivileged( req ) )
			return true;
		redirectToLogin(req, resp);
		return false;
	}

	@Override
	public void destroy() {}

	@Override
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException {
		HttpServletRequest req;
		HttpServletResponse resp;
		if( !( request instanceof HttpServletRequest ) )
			return;
		
		if( !( response instanceof HttpServletResponse ) ) 
			return;
		
		req = (HttpServletRequest) request;
		resp = (HttpServletResponse) response;
			
		if( this.filterConfig.getInitParameter( "authType" ).equals( "requireAdmin" ) )
			if( !Authentication.requireAdmin( req, resp ) )
				return;
		
		if( this.filterConfig.getInitParameter( "authType" ).equals( "requirePrivileged" ) )
			if( !Authentication.requirePrivileged( req, resp ) )
				return;
		
		chain.doFilter( request, response );
	}

	@Override
	public void init( FilterConfig filterConfig ) throws ServletException {
		this.filterConfig = filterConfig;
	}
}
