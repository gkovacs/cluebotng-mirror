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
public class QueryImpl extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1938632521606810700L;

	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		if( Authentication.isAdmin( req ) ) {
			PrintWriter pw = resp.getWriter();
			pw.println( "<html><body><h1>Query</h1><form method='post' action='/query'><table>" );
			pw.println( "<tr><th>Query:</th><td><textarea name='query'></textarea></td></tr>" );
			pw.println( "<tr><th>Submit:</th><td><input type='submit' value='Submit' /></td></tr>" );
			pw.println( "</table></form></body></html>" );
		} else
			super.doGet( req, resp );
	}

	@Override
	protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		if( Authentication.isAdmin( req ) ) {
			PrintWriter pw = resp.getWriter();
			String strQuery = req.getParameter( "query" );
			EntityManager em = EMF.get().createEntityManager();
			EntityTransaction txn = em.getTransaction();
			try {
				txn.begin();
				Query q = em.createQuery( strQuery );
				q.executeUpdate();
				txn.commit();
			} catch( Exception e ) {
				pw.println( "<html><body><pre>" );
				pw.println( e.getMessage() );
				e.printStackTrace( pw );
				pw.println( "</pre></body></html>" );
			} finally {
				if( txn.isActive() )
					txn.rollback();
				em.close();
			}
		} else
			super.doPost( req, resp );
	}
}
