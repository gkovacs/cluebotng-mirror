/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;


/**
 * @author cobi
 *
 */
public class RunStatsImpl extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8027295471561939707L;

	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		if( Authentication.isAdmin( req ) ) {
			Queue queue = QueueFactory.getQueue( "default-queue" );
			queue.add( TaskOptions.Builder.url( "/_ah/cron/stats" ).method( Method.GET ) );
		} else
			super.doGet( req, resp );
	}
}
