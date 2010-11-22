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
	private Refreshable parent;
	private Boolean create = true;
	private String key = null;
	private String name = null;
	private Integer weight = null;
	private final AdminServiceAsync admin = GWT.create( AdminService.class );

	private void processSave( String name, String strWeight, String strRequired, String data ) {
		List< Edit > edits;
		Integer weight = new Integer( strWeight );
		Integer required = new Integer( strRequired );
		
		edits = processData( data );
		
		admin.createEditGroup( name, weight, required, edits, new AsyncCallback< Void >() {

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
			
		});

	}
	
	private void processSave( String strRequired, String data ) {
		List< Edit > edits;
		Integer required = new Integer( strRequired );
		
		edits = processData( data );
		
		admin.addEditsToEditGroup( key, required, edits, new AsyncCallback< Void >() {

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
		
		if( lineParts.length == 1 )
			type = Classification.UNKNOWN;
		else if( lineParts[ 1 ].equals( "V" ) )
			type = Classification.VANDALISM;
		else if( lineParts[ 1 ].equals( "C" ) )
			type = Classification.CONSTRUCTIVE;
		
		return new Edit(
				new Integer( lineParts[ 0 ] ),
				type,
				null,
				null,
				null,
				null,
				null,
				null,
				null
		);
	}

	private void display() {
		if( popup == null )
			popup = new DialogBox();
		if( create )
			popup.setText( "Add Edit Group" );
		else
			popup.setText( "Add Edits to Edit Group " + this.name );
		popup.setAnimationEnabled( true );
		popup.setModal( false );
		
		VerticalPanel vpanel = new VerticalPanel();
		FlexTable properties = new FlexTable();
		
		final TextBox name = new TextBox();
		final TextBox weight = new TextBox();
		final TextBox required = new TextBox();
		final TextArea data = new TextArea();
		
		properties.setText( 0, 0, "Name:" );
		if( create )
			properties.setWidget( 0, 1, name );
		else
			properties.setText( 0, 1, this.name );
		properties.setText( 1, 0, "Weight:" );
		if( create )
			properties.setWidget( 1, 1, weight );
		else
			properties.setText( 1, 1, this.weight.toString() );
		properties.setText( 2, 0, "Required scores:" );
		properties.setWidget( 2, 1, required );
		properties.setText( 3, 0, "Data:" );
		properties.setWidget( 3, 1, data );

		vpanel.add( properties );
		
		Button saveButton = new Button("Save");
		saveButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if( create )
					processSave( name.getText(), weight.getText(), required.getText(), data.getText() );
				else
					processSave( required.getText(), data.getText() );
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
	
	public NewEditGroupWindow( Refreshable parent ) {
		this.parent = parent;
		create = true;
		key = null;
		name = null;
		weight = null;
		display();
	}
	
	public NewEditGroupWindow( Refreshable parent, String key, String name, Integer weight ) {
		this.parent = parent;
		create = false;
		this.key = key;
		this.name = name;
		this.weight = weight;
		display();
	}
}
