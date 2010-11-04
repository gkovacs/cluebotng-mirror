
package org.cluenet.cluebot.reviewinterface.server;

import org.cluenet.cluebot.reviewinterface.client.ReviewService;
import org.cluenet.cluebot.reviewinterface.shared.Classification;
import org.cluenet.cluebot.reviewinterface.shared.ReturnData;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings( "serial" )
public class ReviewServiceImpl extends RemoteServiceServlet implements
		ReviewService {

	@Override
	public ReturnData reviewId( Integer id, Classification type, String comment ) throws Exception {
		User user = null;
		try {
			Transaction.begin();
			
			user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
			user.incClassifications();
			
			Transaction.end();
		} catch( Exception e ) {
			System.err.println( "Exception in user!" );
			System.err.println( e.getMessage() );
			e.printStackTrace();
			throw new IllegalArgumentException( e.getMessage() );
		} finally {
			Transaction.fin();
		}
		
		try {	
			Transaction.begin();
			
			Edit edit = Edit.findById( id );
			edit.newClassification( user, type, comment );
			
			Transaction.end();
		} catch( Exception e ) {
			System.err.println( "Exception in edit!" );
			System.err.println( e.getMessage() );
			throw new IllegalArgumentException( e.getMessage() );
		} finally {
			Transaction.fin();
		}
		return getId();
	}

	@Override
	public ReturnData getId() throws Exception {
		User user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
		if( user == null )
			throw new IllegalArgumentException( "You have no user account." );
		EditGroup eg = EditGroup.getRandomEditGroup();
		if( eg == null )
			throw new IllegalArgumentException( "No more edits available." );
		Edit randomEdit = eg.getRandomEdit( user );
		if( randomEdit == null )
			throw new IllegalArgumentException( "No more edits available." );
		return new ReturnData(
				EditGroup.getRandomEditGroup().getRandomEdit( user ).getClientClass(),
				user.getClientClass()
		);
	}
}
