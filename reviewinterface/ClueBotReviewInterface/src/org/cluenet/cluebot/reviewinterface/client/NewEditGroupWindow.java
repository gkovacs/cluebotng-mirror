package org.cluenet.cluebot.reviewinterface.client;

import java.util.ArrayList;
import java.util.List;

import org.cluenet.cluebot.reviewinterface.shared.Classification;
import org.cluenet.cluebot.reviewinterface.shared.Edit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


public class NewEditGroupWindow {
	private DialogBox popup = null;
	private EditGroupListWindow parent;
	private final AdminServiceAsync admin = GWT.create( AdminService.class );

	private void processSave( String name, String strWeight, String strRequired, String data ) {
		List< Edit > edits;
		Integer weight = new Integer( strWeight );
		Integer required = new Integer( strRequired );
		
		edits = processData( data );
		
		admin.createEditGroup( name, weight, required, edits, new AsyncCallback< Void >() {

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
	
	private List< Edit > processData( String data ) {
		String[] lines = data.split( "\r?\n" );
		List< Edit > list = new ArrayList< Edit >();
		for( String line : lines )
			list.add( processLine( line ) );
		return list;
	}

	private Edit processLine( String line ) {
		String[] lineParts = line.split( " +" );
		
		Classification type = Classification.UNKNOWN;
		
		if( lineParts[ 1 ].equals( "V" ) )
			type = Classification.VANDALISM;
		else if( lineParts[ 1 ].equals( "C" ) )
			type = Classification.CONSTRUCTIVE;
		
		return new Edit(
				new Integer( lineParts[ 0 ] ),
				type,
				new Integer( lineParts[ 2 ] )
		);
	}

	private void display() {
		if( popup == null )
			popup = new DialogBox();
		popup.setText( "Add Edit Group" );
		popup.setAnimationEnabled( true );
		popup.setModal( false );
		
		VerticalPanel vpanel = new VerticalPanel();
		FlexTable properties = new FlexTable();
		
		final TextBox name = new TextBox();
		final TextBox weight = new TextBox();
		final TextBox required = new TextBox();
		final TextArea data = new TextArea();
		
		properties.setText( 0, 0, "Name:" );
		properties.setWidget( 0, 1, name );
		properties.setText( 1, 0, "Weight:" );
		properties.setWidget( 1, 1, weight );
		properties.setText( 2, 0, "Required scores:" );
		properties.setWidget( 2, 1, required );
		properties.setText( 3, 0, "Data:" );
		properties.setWidget( 3, 1, data );

		vpanel.add( properties );
		
		Button saveButton = new Button("Save");
		saveButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				processSave( name.getText(), weight.getText(), required.getText(), data.getText() );
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
	
	public NewEditGroupWindow( EditGroupListWindow parent ) {
		this.parent = parent;
		display();
	}
	
}
