
package org.cluenet.cluebot.reviewinterface.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ClueBotReviewAdminInterface implements EntryPoint {
	
	public static void error( String error ) {
		VerticalPanel hpanel = new VerticalPanel();
		hpanel.add( new Label( "An error occurred.  Please refresh the page." ) );
		hpanel.add( new Label( error ) );
		DialogBox errorBox = new DialogBox();
		errorBox.setText( "Error" );
		errorBox.setAnimationEnabled( true );
		errorBox.add( hpanel );
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
