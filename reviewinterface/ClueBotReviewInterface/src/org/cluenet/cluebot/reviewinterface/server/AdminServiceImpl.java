
package org.cluenet.cluebot.reviewinterface.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.cluenet.cluebot.reviewinterface.client.AdminService;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings( "serial" )
public class AdminServiceImpl extends RemoteServiceServlet implements
		AdminService {

	@Override
	public void createEditGroup( String name, Integer weight, Integer required, List< org.cluenet.cluebot.reviewinterface.shared.Edit > edits ) throws IllegalArgumentException {
		EditGroup eg = new EditGroup( name, weight );
		String key = KeyFactory.keyToString( eg.getKey() );
		
		addEditsToEditGroup( key, required, edits );
	}

	@Override
	public void createUser( String nick, String email, Boolean isAdmin, Boolean sendEmail, Integer count ) throws IllegalArgumentException {
		new User( nick, new Email( email ), isAdmin, count );
		if( sendEmail ) {
			Properties props = new Properties();
	        Session session = Session.getDefaultInstance( props, null );

	        String msgBody = "Hello, " + nick + ".\r\n" +
	        		"Your e-mail address has been approved on the ClueBot-NG Review Interface.\r\n" +
	        		"You may enter the interface here: <http://cluebotreview.g.cluenet.org/>\r\n" +
	        		"\r\n" +
	        		"Thanks,\r\n" +
	        		"ClueBot NG Review Interface Admin Team.";

	        try {
	        	Message msg = new MimeMessage( session );
	        	msg.setFrom( new InternetAddress( "interface@cluebotreview.appspotmail.com", "ClueBot-NG Review Interface" ) );
	        	msg.addRecipient( Message.RecipientType.TO, new InternetAddress( email, nick ) );
	        	msg.setSubject( "ClueBot-NG Review Interface User Account Approved" );
	        	msg.setText( msgBody );
	        	Transport.send( msg );
	        } catch( Exception e ) {
	        	throw new IllegalArgumentException( "Error sending e-mail: " + e.getMessage() );
	        }
		}
	}

	@Override
	public void deleteEditGroup( String key ) throws IllegalArgumentException {
		EditGroup.findByKey( key ).delete();
	}

	@Override
	public void deleteUser( String key ) throws IllegalArgumentException {
		User.findByKey( key ).delete();
	}

	@Override
	public org.cluenet.cluebot.reviewinterface.shared.EditGroup getEditGroup( String key, Integer editStart, Integer editCount, Integer reviewStart, Integer reviewCount, Integer doneStart, Integer doneCount ) throws IllegalArgumentException {
		return EditGroup.findByKey( key ).getClientClass( editStart, editCount, reviewStart, reviewCount, doneStart, doneCount );
	}

	@Override
	public List< org.cluenet.cluebot.reviewinterface.shared.EditGroup > getEditGroups() throws IllegalArgumentException {
		List< org.cluenet.cluebot.reviewinterface.shared.EditGroup > list = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.EditGroup >();
		for( EditGroup eg : EditGroup.list() )
			list.add( eg.getLightClientClass() );
		return list;
	}

	@Override
	public List< org.cluenet.cluebot.reviewinterface.shared.User > getUsers() throws IllegalArgumentException {
		List< org.cluenet.cluebot.reviewinterface.shared.User > list = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.User >();
		for( User user : User.list() )
			list.add( user.getClientClass() );
		return list;
	}

	@Override
	public void setAdmin( String key, Boolean isAdmin ) throws IllegalArgumentException {
		User.findByKey( key ).setAdmin( isAdmin );
	}

	@Override
	public void addEditsToEditGroup( String key, Integer required, List< org.cluenet.cluebot.reviewinterface.shared.Edit > edits ) throws IllegalArgumentException {
		Queue queue = QueueFactory.getQueue( "add-edit-queue" );
		for( int i = 0 ; i <= edits.size() / 100 ; i++ ) {
			int fromIndex = 100 * i;
			int toIndex = fromIndex + 100;
			if( toIndex >= edits.size() )
				toIndex = edits.size();
			if( fromIndex > toIndex )
				continue;
			List< TaskOptions > tasks = new ArrayList< TaskOptions >();
			for( org.cluenet.cluebot.reviewinterface.shared.Edit edit : edits.subList( fromIndex, toIndex ) )
				tasks.add(
						TaskOptions
						.Builder
						.param( "id", edit.id.toString() )
						.param( "classification", edit.classification.toString() )
						.param( "required", required.toString() )
						.param( "key", key )
						.method( Method.GET )
				);
			queue.add( tasks );
		}
	}

	@Override
	public void setNick( String key, String nick ) throws IllegalArgumentException {
		User.findByKey( key ).setNick( nick );
	}

	@Override
	public void reevalEditsInEditGroup( String key ) throws IllegalArgumentException {
		EditGroup eg = EditGroup.findByKey( key );
		List< Key > edits = new ArrayList< Key >();
		edits.addAll( eg.getDone() );
		edits.addAll( eg.getReviewed() );
		Queue queue = QueueFactory.getQueue( "edit-done-queue" );
		for( int i = 0 ; i <= edits.size() / 100 ; i++ ) {
			int fromIndex = 100 * i;
			int toIndex = fromIndex + 100;
			if( toIndex >= edits.size() )
				toIndex = edits.size();
			if( fromIndex > toIndex )
				continue;
			List< TaskOptions > tasks = new ArrayList< TaskOptions >();
			for( Key eKey : edits.subList( fromIndex, toIndex ) )
				tasks.add( TaskOptions.Builder.param( "egkey", key ).param( "ekey", KeyFactory.keyToString( eKey ) ).method( Method.GET ) );
			queue.add( tasks );
		}
	}

}
