package org.cluenet.cluebot.reviewinterface.client;

import org.cluenet.cluebot.reviewinterface.shared.ClientClassification;
import org.cluenet.cluebot.reviewinterface.shared.Edit;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


public class EditInfoWindow {

	public EditInfoWindow( Edit currentEdit, String url ) {
		final DialogBox popup = new DialogBox();
		popup.setText( "Edit " + currentEdit.id );
		popup.setAnimationEnabled( true );
		popup.setModal( false );
		
		VerticalPanel vpanel = new VerticalPanel();
		FlexTable properties = new FlexTable();
		properties.setText( 0, 0, "ID:" );
		properties.setText( 0, 1, currentEdit.id.toString() );
		properties.setText( 1, 0, "Original Classification:" );
		properties.setText( 1, 1, currentEdit.classification.toString() );
		properties.setText( 2, 0, "Vandalism:" );
		properties.setText( 2, 1, currentEdit.vandalism.toString() );
		properties.setText( 3, 0, "Constructive:" );
		properties.setText( 3, 1, currentEdit.constructive.toString() );
		properties.setText( 4, 0, "Skipped:" );
		properties.setText( 4, 1, currentEdit.skipped.toString() );
		properties.setText( 5, 0, "Required:" );
		properties.setText( 5, 1, currentEdit.required.toString() );
		properties.setText( 6, 0, "URL:" );
		properties.setWidget( 6, 1, new Anchor( "diff", url ) );
		vpanel.add( properties );
		
		FlexTable classifications = new FlexTable();
		classifications.setBorderWidth( 1 );
		
		classifications.setText( 0, 0, "User" );
		classifications.setText( 0, 1, "Classification" );
		classifications.setText( 0, 2, "Comment" );
		
		Integer i = 1;
		for( ClientClassification cc : currentEdit.classifications ) {
			classifications.setText( i, 0, cc.user.nickName );
			classifications.setText( i, 1, cc.classification.toString() );
			classifications.setText( i, 2, cc.comment );
			i++;
		}
		
		vpanel.add( classifications );
		
		Button closeButton = new Button("Close");
		closeButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				popup.hide();
				popup.clear();
			}
			
		});
		
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.add( closeButton );
		
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vpanel.add(buttons);
		
		popup.setWidget( vpanel );
		popup.center();
		popup.show();
	}
}
