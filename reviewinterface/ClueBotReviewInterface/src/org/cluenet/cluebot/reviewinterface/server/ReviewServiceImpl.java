
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
	public ReturnData reviewId( Integer id, Classification type, String comment ) throws IllegalArgumentException {
		User user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
		Edit edit = Edit.findById( id );
		System.err.println( "Edit: id=" + id + " class=" + type + " cmt=" + comment );
		edit.newClassification( user, type, comment );
		return getId();
	}

	@Override
	public ReturnData getId() throws IllegalArgumentException {
		User user = User.findByEmail( new Email( UserServiceFactory.getUserService().getCurrentUser().getEmail() ) );
		return new ReturnData(
				EditGroup.getRandomEditGroup().getRandomEdit( user ).getClientClass(),
				user.getClientClass()
		);
	}
}
