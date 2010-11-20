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
public class EditDoneQueueImpl extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6283555296135234325L;

	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		String eKey = req.getParameter( "ekey" );
		String egKey = req.getParameter( "egkey" );
		
		Edit edit = Edit.findByKey( eKey );
		
		EditGroup eg = EditGroup.findByKey( egKey );
		eg.updateEditState( edit );
	}
}
