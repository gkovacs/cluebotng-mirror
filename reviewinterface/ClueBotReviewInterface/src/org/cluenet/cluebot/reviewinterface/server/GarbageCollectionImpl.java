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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;


/**
 * @author cobi
 *
 */
public class GarbageCollectionImpl extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6283555296135234325L;

	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		List< Key > list = Edit.list();
		Queue queue = QueueFactory.getQueue( "garbage-collection-queue" );
		for( int i = 0 ; i <= list.size() / 100 ; i++ ) {
			int fromIndex = 100 * i;
			int toIndex = fromIndex + 99;
			if( toIndex >= list.size() )
				toIndex = list.size() - 1;
			if( fromIndex > toIndex )
				continue;
			List< TaskOptions > tasks = new ArrayList< TaskOptions >();
			for( Key key : list.subList( fromIndex, toIndex ) )
				tasks.add( TaskOptions.Builder.param( "key", KeyFactory.keyToString( key ) ).method( Method.GET ) );
			queue.add( tasks );
		}
	}
}
