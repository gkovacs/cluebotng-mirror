/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;


/**
 * @author cobi
 *
 */
public class WelcomeImpl extends HttpServlet {
	@Override
	protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		UserService userService = UserServiceFactory.getUserService();
		if( !Authentication.isPrivileged( req ) &&  userService.getCurrentUser() != null ) {
			String email = userService.getCurrentUser().getEmail();
			Properties props = new Properties();
	        Session session = Session.getDefaultInstance( props, null );

	        String msgBody = "Hello,\r\n" +
					"New user:\r\n" +
					"E-mail: " + email + "\r\n" +
					"Wikipedia: " + req.getParameter( "wikipedia" ) + "\r\n" +
					"Comments: " + req.getParameter( "comments" ) + "\r\n" +
					"\r\n" +
					"<http://enwp.org/Special:Contributions/" + req.getParameter( "wikipedia" ) + ">\r\n" +
					"user add " + req.getParameter( "wikipedia" ) + ":" + email + ":0\r\n" +
					"\r\n" +
					"Thanks,\r\n" +
					"ClueBot NG Review Interface.";

	        try {
				Message msg = new MimeMessage( session );
				msg.setFrom( new InternetAddress( "interface@cluebotreview.appspotmail.com", "ClueBot-NG Review Interface" ) );
				for( User user : User.list() )
					if( user.isAdmin() )
						msg.addRecipient( Message.RecipientType.TO, new InternetAddress( user.getEmail().getEmail(), user.getNick() ) );
				msg.setSubject( "New user request" );
				msg.setText( msgBody );
				Transport.send( msg );
				XMPPService xmpp = XMPPServiceFactory.getXMPPService();
				xmpp.sendMessage( new MessageBuilder().withRecipientJids( new JID( "cobi@cluenet.org" ) ).withBody( msgBody ).build() );
				resp.getWriter().println( "<html><body>Your request has been stored.  An admin will review it shortly.  If you are approved, you will receive an e-mail at <a href='mailto:" + email + "'>" + email + "</a>.</body></html>" );
	        } catch( Exception e ) {
	        	resp.getWriter().println( "<html><body>There was an error.</body></html>" );
	        	resp.getWriter().println( e.getMessage() );
	        	e.printStackTrace( resp.getWriter() );
	        }
		} else
			doGet( req, resp );
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6283555296135234325L;

	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();
		pw.println( "<!doctype html>" );
		pw.println( "<html>" );
		pw.println( "  <head>" );
		pw.println( "    <meta http-equiv='content-type' content='text/html; charset=UTF-8'>" );
		pw.println( "    <link type='text/css' rel='stylesheet' href='Welcome.css'>" );
		pw.println( "    <title>ClueBot Review Interface</title>" );
		pw.println( "  </head>" );
		pw.println( "  <body>" );
		pw.println( "    <div id='box'>" );
		pw.println( "    <div id='content'>" );
		pw.println( "      <p>" );
		pw.println( "        One of the keys to Cluebot-NG functioning well is its dataset." );
		pw.println( "        The larger and more accurate its dataset it, the better it will function, with fewer false positives, and more caught vandalism." );
		pw.println( "        It's impossible for just a few people to manually review the thousands of edits necessary, so Cobi wrote a dataset review interface to allow people to review edits and classify them as vandalism or constructive." );
		pw.println( "      </p>" );
		pw.println( "      <p>" );
		pw.println( "        This interface is used for a few things." );
		pw.println( "        Firstly, it's used to make sure the dataset we already have is accurate." );
		pw.println( "        False positives and false negatives from the trial dataset are put in the review queue, because we've found that a very few edits in the dataset may not be correctly classified." );
		pw.println( "        This causes problems in the bot's training and threshold calculations." );
		pw.println( "      </p>" );
		pw.println( "      <p>" );
		pw.println( "        Also, random edits from Wikipedia may be added to the review queue to grow the overall size of the dataset." );
		pw.println( "      </p>" );
		pw.println( "      <p>" );
		pw.println( "        Classifying edits in this review interface can actually help Wikipedia more with your time than just hunting vandalism." );
		pw.println( "        Hunting vandalism manually may catch a small fraction of a percent of vandalism on Wikipedia." );
		pw.println( "        Classifying edits in this interface may allow Cluebot-NG to catch 5% or more of additional vandalism." );
		pw.println( "      </p>" );
		
		if( !Authentication.isPrivileged( req ) ) {
			UserService userService = UserServiceFactory.getUserService();
			String url = userService.createLoginURL( req.getRequestURI() );
			if( userService.getCurrentUser() == null ) {
				pw.println( "      <p>" );
				pw.println( "        To use the dataset review interface, you need a Google account, as the interface is built on the Google AppEngine framework." );
				pw.println( "        To be granted access to the interface, log in and fill out the form that appears." );
				pw.println( "        Once approved, please thoroughly review the directions that will appear below." );
				pw.println( "      </p>" );
				pw.println( "      <p>" );
				pw.println( "        You need to log in, <a href='" + url + "'>here is a link</a>." );
				pw.println( "      </p>" );
			} else {
				String email = userService.getCurrentUser().getEmail();
				pw.println( "      <p>" );
				pw.println( "        To be granted access to the interface, fill out the form below." );
				pw.println( "        Once approved, please thoroughly review the directions that will appear below." );
				pw.println( "      </p>" );
				pw.println( "      <p>" );
				pw.println( "        If you already have access, but need to log in, <a href='" + url + "'>here is a link</a>." );
				pw.println( "      </p>" );
				pw.println( "      <form method='post' action='/welcome'>" );
				pw.println( "        <table>" );
				pw.println( "          <tr><th>Google Account:</th><td>" + email + "</td></tr>" );
				pw.println( "          <tr><th>Wikipedia Account:</th><td><input type='text' name='wikipedia' /></td></tr>" );
				pw.println( "          <tr><th>Comments:</th><td><input type='text' name='comments' /></td></tr>" );
				pw.println( "          <tr><td colspan=2 align='right'><input type='submit' value='Submit' /></td></tr>" );
				pw.println( "        </table>" );
				pw.println( "      </form>" );
			}
		} else {
			pw.println( "      <p>" );
			pw.println( "        In the review interface, you will have a browser window with Wikipedia articles, and a window sitting on top where you can classify edits." );
			pw.println( "        You will be able to click links and such in the main browser window without interrupting the process." );
			pw.println( "        The window sitting on top allows you to classify edits as Vandalism, Constructive, or Skip." );
			pw.println( "      </p>" );
			pw.println( "      <p>" );
			pw.println( "        In general, if an edit would be classified as vandalism by a human, it should be classified as vandalism." );
			pw.println( "        Most other edits should be classified as constructive, with a few exceptions (and because many of the edits in the review queue may be borderline, you may encounter these exceptions more often than you might think)." );
			pw.println( "        Skipping an edit excludes it from the dataset entirely." );
			pw.println( "        An edit may be skipped if it's borderline vandalism, and it's not a big deal if the bot classifies edits like it as vandalism in production." );
			pw.println( "        An edit may also be skipped if you can't tell whether or not it's vandalism." );
			pw.println( "        The other case where skipping edits may be acceptable is if the edit is not vandalism," );
			pw.println( "        but is a very poor quality edit, and contains some attributes of vandalism." );
			pw.println( "        Although very poor edits made in good faith technically should not be classified as vandalism," );
			pw.println( "        classifying them as constructive could interfere with the bot's training, so they should be skipped." );
			pw.println( "      </p>" );
			pw.println( "      <p>" );
			pw.println( "        In some cases, the interface may ask \"Are you sure?\" when you select a result." );
			pw.println( "        If this happens, double-check that your classification is correct, then click Yes or No." );
			pw.println( "      </p>" );
			pw.println( "      <p>" );
			pw.println( "        There is also a Comment box along with the Vandalism, Constructive, and Skip buttons." );
			pw.println( "        This is optional." );
			pw.println( "        If you think there's something about the edit that the Cluebot-NG operators should know about," );
			pw.println( "        such as an edit that's clearly constructive but may look like vandalism based on simple statistics," );
			pw.println( "        leave a comment about it, and the Cluebot-NG operators will take that into account." );
			pw.println( "      </p>" );
			pw.println( "      <p style='font-size: 120%; color: #990000;'>" );
			pw.println( "        To be clear, if an edit is definitely vandalism, even if it's not easy-to-catch vandalism, still mark it as vandalism!" );
			pw.println( "        The bot is capable of catching many things you may not think it can - do not baby the bot!" );
			pw.println( "        If it's vandalism, mark it as such!" );
			pw.println( "      </p>" );
			pw.println( "      <p>" );
			pw.println( "        To get started, <a href='/review.jsp'>click here</a>." );
			pw.println( "      </p>" );
		}
		if( Authentication.isAdmin( req ) ) {
			pw.println( "      <p>" );
			pw.println( "        I see you are an admin.  Here are some links for you:" );
			pw.println( "      </p>" );
			pw.println( "      <ul>" );
			pw.println( "        <li><a href='/admin.jsp'>Admin panel</a></li>" );
			pw.println( "        <li><a href='/collectGarbage'>Clean out database</a></li>" );
			pw.println( "        <li><a href='http://appengine.google.com/'>AppEngine Admin</a></li>" );
			pw.println( "      </ul>" );
		}
		pw.println( "    </div>");		
		pw.println( "    <div id='stats'>");
		pw.println( "      <a name='stats' />" );
		pw.println( "      <p>" );
		pw.println( "        Here are some stats:" );
		pw.println( "      </p>" );
		pw.println( "      <table>" );
		pw.println( "        <tr><th>Nickname</th><th>Contributions</th></tr>" );
		
		List< User > users = User.list();
		Collections.sort( users, new Comparator< User >() {

			@Override
			public int compare( User o1, User o2 ) {
				return o2.getClassifications().compareTo( o1.getClassifications() );
			}
			
		});
		for( User user : users )
			pw.println( "        <tr><td>" + user.getNick() + "</td><td>" + user.getClassifications().toString() + "</td></tr>" );
		pw.println( "      </table>" );
		pw.println( "    </div>");

		pw.println( "    </div>");
		pw.println( "  </body>");
		pw.println( "</html>");
	}
}
