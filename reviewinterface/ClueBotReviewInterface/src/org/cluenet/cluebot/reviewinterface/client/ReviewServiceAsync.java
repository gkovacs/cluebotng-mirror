
package org.cluenet.cluebot.reviewinterface.client;

import java.util.List;

import org.cluenet.cluebot.reviewinterface.shared.Classification;
import org.cluenet.cluebot.reviewinterface.shared.ClientClassification;
import org.cluenet.cluebot.reviewinterface.shared.ReturnData;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface ReviewServiceAsync {
	
	void getId( AsyncCallback< ReturnData > callback );

	void reviewId( Integer id, Classification type, String comment, AsyncCallback< ReturnData > callback );

	void getId( Integer id, AsyncCallback< ReturnData > callback );

	void getClassifications( String start, Integer count, AsyncCallback< List< ClientClassification >> callback );

}
