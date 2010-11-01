package org.cluenet.cluebot.reviewinterface.client;

import org.cluenet.cluebot.reviewinterface.shared.AdminEdit;
import org.cluenet.cluebot.reviewinterface.shared.EditGroup;
import org.cluenet.cluebot.reviewinterface.shared.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;


public class ViewEditGroupWindow implements Refreshable {
	private DialogBox popup = null;
	private EditGroup editGroup;
	private String key;
	private final AdminServiceAsync admin = GWT.create( AdminService.class );

	private void display() {
		if( popup == null )
			popup = new DialogBox();
		popup.setText( "Edit Group " + editGroup.name );
		popup.setAnimationEnabled( true );
		popup.setModal( false );
		
		VerticalPanel vpanel = new VerticalPanel();
		FlexTable properties = new FlexTable();
		properties.setText( 0, 0, "Name:" );
		properties.setText( 0, 1, editGroup.name );
		properties.setText( 1, 0, "Weight:" );
		properties.setText( 1, 1, editGroup.weight.toString() );
		vpanel.add( properties );

		FlexTable editTable = new FlexTable();
		vpanel.add( new Label( "Active:" ) );
		vpanel.add( editTable );
		editTable.setBorderWidth( 1 );
		
		editTable.setText( 0, 0, "ID" );
		editTable.setText( 0, 1, "Weight" );
		editTable.setText( 0, 2, "Type" );
		editTable.setText( 0, 3, "Vandalism" );
		editTable.setText( 0, 4, "Constructive" );
		editTable.setText( 0, 5, "Skipped" );
		editTable.setText( 0, 6, "Required" );
		editTable.setText( 0, 7, "Comments" );
		editTable.setText( 0, 8, "Users" );
		
		int i = 1;
		
		for( final AdminEdit edit : editGroup.edits ) {
			String comments = "";
			for( String comment : edit.comments )
				comments += comment + "\n";
			
			String users = "";
			for( User user : edit.users )
				users += user.userName + " (" + user.classifications.toString() + ")" + ( user.isAdmin ? " (admin)" : "" ) + "\n";
			
			editTable.setText( i, 0, edit.id.toString() );
			editTable.setText( i, 1, edit.weight.toString() );
			editTable.setText( i, 2, edit.classification.toString() );
			editTable.setText( i, 3, edit.vandalism.toString() );
			editTable.setText( i, 4, edit.constructive.toString() );
			editTable.setText( i, 5, edit.skipped.toString() );
			editTable.setText( i, 6, edit.required.toString() );
			editTable.setText( i, 7, comments );
			editTable.setText( i, 8, users );
			
			i++;
		}
		
		vpanel.add( new Label( "Finished:" ) );
		FlexTable doneTable = new FlexTable();
		vpanel.add( doneTable );
		doneTable.setBorderWidth( 1 );
		
		doneTable.setText( 0, 0, "ID" );
		doneTable.setText( 0, 1, "Weight" );
		doneTable.setText( 0, 2, "Type" );
		doneTable.setText( 0, 3, "Vandalism" );
		doneTable.setText( 0, 4, "Constructive" );
		doneTable.setText( 0, 5, "Skipped" );
		doneTable.setText( 0, 6, "Required" );
		doneTable.setText( 0, 7, "Comments" );
		doneTable.setText( 0, 8, "Users" );
		
		i = 1;
		
		for( final AdminEdit edit : editGroup.done ) {
			String comments = "";
			for( String comment : edit.comments )
				comments += comment + "<br />\n";
			
			String users = "";
			for( User user : edit.users )
				users += user.userName + " (" + user.classifications.toString() + ")" + ( user.isAdmin ? " (admin)" : "" ) + "<br />\n";
			
			doneTable.setText( i, 0, edit.id.toString() );
			doneTable.setText( i, 1, edit.weight.toString() );
			doneTable.setText( i, 2, edit.classification.toString() );
			doneTable.setText( i, 3, edit.vandalism.toString() );
			doneTable.setText( i, 4, edit.constructive.toString() );
			doneTable.setText( i, 5, edit.skipped.toString() );
			doneTable.setText( i, 6, edit.required.toString() );
			doneTable.setHTML( i, 7, comments );
			doneTable.setHTML( i, 8, users );
			
			i++;
		}
		
		Button newButton = new Button("New");
		newButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				addEdits();
			}
			
		});
		
		Button refreshButton = new Button("Refresh");
		refreshButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
			
		});
		
		Button closeButton = new Button("Close");
		closeButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				popup.hide();
				popup.clear();
			}
			
		});
		
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.add( newButton );
		buttons.add( refreshButton );
		buttons.add( closeButton );
		
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vpanel.add(buttons);
		
		popup.setWidget( vpanel );
		if( !popup.isShowing() )
			popup.show();
	}
	
	private void addEdits() {
		new NewEditGroupWindow( this, editGroup.key, editGroup.name, editGroup.weight );
	}
	
	public void refresh() {
		admin.getEditGroup( key, new AsyncCallback< EditGroup >() {

			@Override
			public void onFailure( Throwable caught ) {
				ClueBotReviewAdminInterface.error();
			}

			@Override
			public void onSuccess( EditGroup result ) {
				editGroup = result;
				display();
			}
			
		});
	}
	
	public ViewEditGroupWindow( String key ) {
		this.key = key;
		refresh();
	}
}
