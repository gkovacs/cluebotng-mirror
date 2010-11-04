/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author cobi
 *
 */
public class GarbageCollectionQueueImpl extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6283555296135234325L;

	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		String strKey = req.getParameter( "key" );
		
		Edit edit = Edit.findByKey( strKey );
		if( EditGroup.findByEdit( edit ).size() == 0 )
			edit.delete();
	}
}
