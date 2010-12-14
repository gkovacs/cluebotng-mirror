package org.cluenet.cluebot.reviewinterface.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;


public class ReportInterfaceCronImpl extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3384518369577580454L;

	@Override
	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		List< Integer > edits = new ArrayList< Integer >();
		
		String reportRoot = "http://report.cluebot.cluenet.org/"; 
		
		URL url = new URL( reportRoot + "api.php" );
		BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream() ) );
		String line;
		
		while( ( line = reader.readLine() ) != null ) {
			edits.add( new Integer( line ) );
		}
		
		reader.close();
		
		Queue queue = QueueFactory.getQueue( "add-edit-queue" );
		for( int i = 0 ; i <= edits.size() / 100 ; i++ ) {
			int fromIndex = 100 * i;
			int toIndex = fromIndex + 100;
			if( toIndex >= edits.size() )
				toIndex = edits.size();
			if( fromIndex > toIndex )
				continue;
			List< TaskOptions > tasks = new ArrayList< TaskOptions >();
			for( Integer edit : edits.subList( fromIndex, toIndex ) )
				tasks.add(
						TaskOptions
						.Builder
						.param( "id", edit.toString() )
						.param( "classification", "CONSTRUCTIVE" )
						.param( "required", "2" )
						.param( "key", "ag1jbHVlYm90cmV2aWV3chELEglFZGl0R3JvdXAY7qxHDA" )
						.method( Method.GET )
				);
			queue.add( tasks );
		}
		
		try {
			url = new URL( reportRoot + "update.php" );
			reader = new BufferedReader( new InputStreamReader( url.openStream() ) );
			
			while( ( line = reader.readLine() ) != null );
			
			reader.close();
		} catch( Exception e ) {
			/* Nothing */
		}
	}
	
}
