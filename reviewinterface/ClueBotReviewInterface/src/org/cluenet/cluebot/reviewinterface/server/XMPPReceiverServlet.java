package org.cluenet.cluebot.reviewinterface.server;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.*;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.xmpp.*;

@SuppressWarnings( "serial" )
public class XMPPReceiverServlet extends HttpServlet {
	
	public void doPost( HttpServletRequest req, HttpServletResponse res ) throws IOException {
		XMPPService xmpp = XMPPServiceFactory.getXMPPService();
		com.google.appengine.api.xmpp.Message message = xmpp.parseMessage( req );
		
		JID fromJid = message.getFromJid();
		String body = message.getBody();
		String[] parts = body.split( " ", 2 );
		String command = parts[ 0 ].toLowerCase();
		String rest = "";
		if( parts.length == 2 )
			 rest = parts[ 1 ];

		String fromEmail = fromJid.getId();
		fromEmail = fromEmail.split( "/" )[ 0 ];
		
		User user = User.findByEmail( new Email( fromEmail ) );
		
		if( user == null ) {
			xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "I don't know you.  Go away." ).build() );
			return;
		}
		
		if( command.equals( "whoami" ) ) {
			xmpp.sendMessage(
					new MessageBuilder()
					.withRecipientJids( fromJid )
					.withBody(
							"You are '" + user.getNick() + "' (" + user.getEmail().getEmail() + ").  "
									+ "You have classified " + user.getClassifications() + " edits."
									+ ( user.isAdmin() ? "  You are an admin." : "" )
					).build()
			);
			return;
		} else if( !user.isAdmin() ) {
			xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Unknown command." ).build() );
			return;
		}
		
		String[] subParts = rest.split( " ", 2 );
		String subCommand = subParts[ 0 ].toLowerCase();
		String subRest = null;
		String[] other = null;
		if( subParts.length == 2 ) {
			subRest = subParts[ 1 ];
			other = subRest.split( ":" );
		}
		
		if( command.equals( "user" ) ) {
			
			if( subCommand.equals( "add" ) ) {
				if( other == null || other.length != 3 ) {
					xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Expected user add Nick Name:email@address:count" ).build() );
					return;
				}
				User u = new User( other[ 0 ], new Email( other[ 1 ] ), false, new Integer( other[ 2 ] ) );
				Properties props = new Properties();
		        Session session = Session.getDefaultInstance( props, null );

		        String msgBody = "Hello, " + u.getNick() + ".\r\n" +
		        		"Your e-mail address has been approved on the ClueBot-NG Review Interface.\r\n" +
		        		"You may enter the interface here: <http://cluebotreview.g.cluenet.org/>\r\n" +
		        		"\r\n" +
		        		"Thanks,\r\n" +
		        		"ClueBot NG Review Interface Admin Team.";

		        try {
		        	Message msg = new MimeMessage( session );
		        	msg.setFrom( new InternetAddress( "interface@cluebotreview.appspotmail.com", "ClueBot-NG Review Interface" ) );
		        	msg.addRecipient( Message.RecipientType.TO, new InternetAddress( u.getEmail().getEmail(), u.getNick() ) );
		        	msg.setSubject( "ClueBot-NG Review Interface User Account Approved" );
		        	msg.setText( msgBody );
		        	Transport.send( msg );
		        } catch( Exception e ) {
		        	xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Error sending e-mail: " + e.getMessage() ).build() );
		        }
				xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Added as " + KeyFactory.keyToString( u.getKey() ) ).build() );
				
			} else if( subCommand.equals( "delete" ) ) {
				if( other == null ) {
					xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Expected user delete email@address" ).build() );
					return;
				}
				User u = User.findByEmail( new Email( other[ 0 ] ) );
				String key = KeyFactory.keyToString( u.getKey() );
				u.delete();
				xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Deleted user " + key ).build() );
				
			} else if( subCommand.equals( "admin" ) ) {
				if( other == null ) {
					xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Expected user admin email@address" ).build() );
					return;
				}
				User u = User.findByEmail( new Email( other[ 0 ] ) );
				String key = KeyFactory.keyToString( u.getKey() );
				u.setAdmin( true );
				xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Set admin on user " + key ).build() );
				
			} else if( subCommand.equals( "deadmin" ) ) {
				if( other == null ) {
					xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Expected user deadmin email@address" ).build() );
					return;
				}
				User u = User.findByEmail( new Email( other[ 0 ] ) );
				String key = KeyFactory.keyToString( u.getKey() );
				u.setAdmin( false );
				xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Unset admin on user " + key ).build() );
				
			} else if( subCommand.equals( "list" ) ) {
				xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Key | Nick name | Email | Count | Admin" ).build() );
				for( User u : User.list() ) {
					String key = KeyFactory.keyToString( u.getKey() );
					String nick = u.getNick();
					String email = u.getEmail().getEmail();
					String count = u.getClassifications().toString();
					String admin = u.isAdmin() ? "true" : "false";
					xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( key + " | " + nick + " | " + email + " | " + count + " | " + admin ).build() );
				}
				
			} else
				xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Expected user <add|delete|admin|deadmin|list>" ).build() );
			
		} else if( command.equals( "editgroup" ) ) {
			xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Not implemented." ).build() );
		} else
			xmpp.sendMessage( new MessageBuilder().withRecipientJids( fromJid ).withBody( "Expected <whoami|user|editgroup>." ).build() );
	}
}
