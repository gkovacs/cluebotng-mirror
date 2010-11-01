package org.cluenet.cluebot.reviewinterface.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


public class NewUserWindow {
	private DialogBox popup = null;
	private Refreshable parent;
	private final AdminServiceAsync admin = GWT.create( AdminService.class );

	
	private void display() {
		if( popup == null )
			popup = new DialogBox();
		popup.setText( "Add User" );
		popup.setAnimationEnabled( true );
		popup.setModal( false );
		
		VerticalPanel vpanel = new VerticalPanel();
		FlexTable properties = new FlexTable();
		
		final TextBox email = new TextBox();
		final CheckBox isadmin = new CheckBox();
		
		properties.setText( 0, 0, "E-mail:" );
		properties.setWidget( 0, 1, email );
		properties.setText( 1, 0, "Admin:" );
		properties.setWidget( 1, 1, isadmin );

		vpanel.add( properties );
		
		Button saveButton = new Button("Save");
		saveButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				admin.createUser( email.getText(), isadmin.getValue(), new AsyncCallback< Void >() {

					@Override
					public void onFailure( Throwable caught ) {
						ClueBotReviewAdminInterface.error();
					}

					@Override
					public void onSuccess( Void result ) {
						popup.hide();
						popup.clear();
						parent.refresh();
					}
					
				});
			}
			
		});
		
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				popup.hide();
				popup.clear();
			}
			
		});
		
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.add( cancelButton );
		buttons.add( saveButton );
		
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vpanel.add(buttons);
		
		popup.setWidget( vpanel );
		if( !popup.isShowing() )
			popup.show();
	}
	
	public NewUserWindow( Refreshable parent ) {
		this.parent = parent;
		display();
	}
	
}
