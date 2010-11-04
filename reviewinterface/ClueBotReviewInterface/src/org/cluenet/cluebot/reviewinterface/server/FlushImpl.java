/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author cobi
 *
 */
public class FlushImpl extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1938632521606810700L;

	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		if( Authentication.isAdmin( req ) ) {
			TheCache.cache().clear();
		} else
			super.doGet( req, resp );
	}
}
