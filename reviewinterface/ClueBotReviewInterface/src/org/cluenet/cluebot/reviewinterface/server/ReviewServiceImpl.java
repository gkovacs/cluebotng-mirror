
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
		try {
			User user = null;
			user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
			user.incClassifications();
			
			Edit edit = Edit.findById( id );
			edit.newClassification( user, type, comment );
	
			return getId();
		} catch( Exception e ) {
			e.printStackTrace();
			throw new IllegalArgumentException( e.getMessage() );
		}
	}

	@Override
	public ReturnData getId() throws Exception {
		try {
			User user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
			if( user == null )
				throw new IllegalArgumentException( "You have no user account." );
			EditGroup eg = EditGroup.getRandomEditGroup();
			if( eg == null )
				throw new IllegalArgumentException( "No more edit groups available." );
			Edit randomEdit = eg.getRandomEdit( user );
			if( randomEdit == null )
				throw new IllegalArgumentException( "No more edits available for " + eg.getName() + "." );
			return new ReturnData(
					randomEdit.getClientClass(),
					user.getClientClass()
			);
		} catch( Exception e ) {
			e.printStackTrace();
			throw new IllegalArgumentException( e.getMessage() );
		}
	}
}
