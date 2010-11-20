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
	private String key = null;
	private final AdminServiceAsync admin = GWT.create( AdminService.class );

	
	private void display() {
		if( popup == null )
			popup = new DialogBox();
		if( key == null )
			popup.setText( "Add User" );
		else
			popup.setText( "Change User" );
		popup.setAnimationEnabled( true );
		popup.setModal( false );
		
		VerticalPanel vpanel = new VerticalPanel();
		FlexTable properties = new FlexTable();
		
		final TextBox nick = new TextBox();
		final TextBox email = new TextBox();
		final CheckBox isadmin = new CheckBox();
		final CheckBox sendemail = new CheckBox();
		final TextBox count = new TextBox();
		count.setText( "0" );
		
		properties.setText( 0, 0, "Nickname:" );
		properties.setWidget( 0, 1, nick );
		if( key == null ) {
			properties.setText( 1, 0, "E-mail:" );
			properties.setWidget( 1, 1, email );
			properties.setText( 2, 0, "Admin:" );
			properties.setWidget( 2, 1, isadmin );
			properties.setText( 3, 0, "Send welcome e-mail?:" );
			properties.setWidget( 3, 1, sendemail );
			properties.setText( 4, 0, "Count:" );
			properties.setWidget( 4, 1, count );
		}

		vpanel.add( properties );
		
		Button saveButton = new Button("Save");
		saveButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				AsyncCallback< Void > callback = new AsyncCallback< Void >() {

					@Override
					public void onFailure( Throwable caught ) {
						ClueBotReviewAdminInterface.error( caught.getMessage() );
					}

					@Override
					public void onSuccess( Void result ) {
						popup.hide();
						popup.clear();
						parent.refresh();
					}
					
				};
				if( key == null )
					admin.createUser( nick.getText(), email.getText(), isadmin.getValue(), sendemail.getValue(), new Integer( count.getValue() ), callback );
				else
					admin.setNick( key, nick.getText(), callback );
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
		if( !popup.isShowing() ) {
			popup.center();
			popup.show();
		}
	}
	
	public NewUserWindow( Refreshable parent ) {
		this.parent = parent;
		this.key = null;
		display();
	}

	public NewUserWindow( Refreshable parent, String key ) {
		this.parent = parent;
		this.key = key;
		display();
	}
	
}
