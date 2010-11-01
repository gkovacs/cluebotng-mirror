
package org.cluenet.cluebot.reviewinterface.client;

import org.cluenet.cluebot.reviewinterface.shared.Classification;
import org.cluenet.cluebot.reviewinterface.shared.ReturnData;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath( "review" )
public interface ReviewService extends RemoteService {
	ReturnData reviewId( Integer id, Classification type, String comment ) throws Exception;
	ReturnData getId() throws Exception;
}
