/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cluenet.cluebot.reviewinterface.shared.Classification;


/**
 * @author cobi
 *
 */
public class AddEditQueueImpl extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6283555296135234325L;

	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		String strKey = req.getParameter( "key" );
		String strId = req.getParameter( "id" );
		String strRequired = req.getParameter( "required" );
		String strClassification = req.getParameter( "classification" );
		
		Integer id = Integer.decode( strId );
		Integer required = Integer.decode( strRequired );
		Classification classification;
		
		if( strClassification.equals( "CONSTRUCTIVE" ) )
			classification = Classification.CONSTRUCTIVE;
		else if( strClassification.equals( "VANDALISM" ) )
			classification = Classification.VANDALISM;
		else if( strClassification.equals( "SKIPPED" ) )
			classification = Classification.SKIPPED;
		else
			classification = Classification.UNKNOWN;
		
		Edit edit = Edit.newFromId( id, classification, required );
		
		List< Edit > edits = new ArrayList< Edit >();
		edits.add( edit );
		
		EditGroup eg = EditGroup.findByKey( strKey );
		eg.addEdits( edits );
	}
}
