
package org.cluenet.cluebot.reviewinterface.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ClueBotReviewAdminInterface implements EntryPoint {
	
	public static void error() {
		DialogBox errorBox = new DialogBox();
		errorBox.setText( "Error" );
		errorBox.setAnimationEnabled( true );
		errorBox.add( new Label( "An error occurred.  Please refresh the page." ) );
		errorBox.center();
		errorBox.setModal( true );
		errorBox.show();
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
	 */
	@Override
	public void onModuleLoad() {
		new UserListWindow();
		new EditGroupListWindow();
	}
}
