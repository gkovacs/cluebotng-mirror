
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
		User user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
		Edit edit = Edit.findById( id );
		edit.newClassification( user, type, comment );
		user.incClassifications();
		return getId();
	}

	@Override
	public ReturnData getId() throws Exception {
		User user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
		Edit randomEdit = EditGroup.getRandomEditGroup().getRandomEdit( user );
		if( randomEdit == null )
			throw new Exception( "No more edits available." );
		return new ReturnData(
				EditGroup.getRandomEditGroup().getRandomEdit( user ).getClientClass(),
				user.getClientClass()
		);
	}
}
