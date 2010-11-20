/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.jdo.Transaction;


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
			PersistenceManager pm = JDOFilter.getPM();
			Transaction txn = pm.currentTransaction();
			try {
				txn.begin();
				Query q = pm.newQuery( strQuery );
				q.execute();
				txn.commit();
			} catch( Exception e ) {
				pw.println( "<html><body><pre>" );
				pw.println( e.getMessage() );
				e.printStackTrace( pw );
				pw.println( "</pre></body></html>" );
			} finally {
				if( txn.isActive() )
					txn.rollback();
			}
		} else
			super.doPost( req, resp );
	}
}
