package org.cluenet.cluebot.reviewinterface.client;

import java.util.List;

import org.cluenet.cluebot.reviewinterface.shared.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


public class UserListWindow {
	private DialogBox popup = null;
	private List< User > users;
	private final AdminServiceAsync admin = GWT.create( AdminService.class );

	private void display() {
		if( popup == null )
			popup = new DialogBox();
		popup.setText( "User List" );
		popup.setAnimationEnabled( true );
		popup.setModal( false );
		
		VerticalPanel vpanel = new VerticalPanel();

		FlexTable usertable = new FlexTable();
		vpanel.add( usertable );
		usertable.setBorderWidth( 1 );
		
		usertable.setText( 0, 0, "Actions" );
		usertable.setText( 0, 1, "Email" );
		usertable.setText( 0, 2, "Admin" );
		usertable.setText( 0, 3, "Count" );
		
		int i = 1;
		
		for( final User user : users ) {
			HorizontalPanel actions = new HorizontalPanel();
			usertable.setWidget( i, 0, actions );
			usertable.setText( i, 1, user.userName );
			usertable.setText( i, 2, user.isAdmin.toString() );
			usertable.setText( i, 3, user.classifications != null ? user.classifications.toString() : "0" );
			
			Button delete = new Button("X");
			delete.addClickHandler( new ClickHandler(){

				@Override
				public void onClick( ClickEvent event ) {
					admin.deleteUser( user.key, new AsyncCallback< Void >() {
						@Override
						public void onFailure( Throwable caught ) {
							ClueBotReviewAdminInterface.error();
						}

						@Override
						public void onSuccess( Void result ) {
							refresh();
						}
					});
				}
				
			});
			actions.add( delete );
			
			Button setadmin = new Button("+");
			setadmin.addClickHandler( new ClickHandler(){

				@Override
				public void onClick( ClickEvent event ) {
					admin.setAdmin( user.key, true, new AsyncCallback< Void >() {
						@Override
						public void onFailure( Throwable caught ) {
							ClueBotReviewAdminInterface.error();
						}

						@Override
						public void onSuccess( Void result ) {
							refresh();
						}
					});
				}
				
			});
			actions.add( setadmin );
			
			Button deadmin = new Button("-");
			deadmin.addClickHandler( new ClickHandler(){

				@Override
				public void onClick( ClickEvent event ) {
					admin.setAdmin( user.key, false, new AsyncCallback< Void >() {
						@Override
						public void onFailure( Throwable caught ) {
							ClueBotReviewAdminInterface.error();
						}

						@Override
						public void onSuccess( Void result ) {
							refresh();
						}
					});
				}
				
			});
			actions.add( deadmin );
			
			i++;
		}
		
		Button newButton = new Button("New");
		newButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				newUser();
			}
			
		});
		
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.add( newButton );
		
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vpanel.add(buttons);
		
		popup.setWidget( vpanel );
		if( !popup.isShowing() )
			popup.show();
	}
	
	private void newUser() {
		new NewUserWindow( this );
	}
	
	public void refresh() {
		admin.getUsers( new AsyncCallback< List< User > >() {

			@Override
			public void onFailure( Throwable caught ) {
				ClueBotReviewAdminInterface.error();
			}

			@Override
			public void onSuccess( List< User > result ) {
				users = result;
				display();
			}
			
		});
	}
	
	public UserListWindow() {
		refresh();
	}
}
