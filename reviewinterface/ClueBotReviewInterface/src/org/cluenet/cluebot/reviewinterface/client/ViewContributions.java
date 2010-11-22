package org.cluenet.cluebot.reviewinterface.client;

import java.util.List;

import org.cluenet.cluebot.reviewinterface.shared.Classification;
import org.cluenet.cluebot.reviewinterface.shared.ClientClassification;
import org.cluenet.cluebot.reviewinterface.shared.Edit;
import org.cluenet.cluebot.reviewinterface.shared.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ViewContributions implements Refreshable {
	private DialogBox popup = null;
	private List< ClientClassification > list;
	private String start = "";
	private Integer count = 5;
	private ClueBotReviewInterface parent;
	private final ReviewServiceAsync rs = GWT.create( ReviewService.class );

	private void display() {
		if( popup == null )
			popup = new DialogBox();
		popup.setText( "Contributions" );
		popup.setAnimationEnabled( true );
		popup.setModal( false );
		
		VerticalPanel vpanel = new VerticalPanel();

		FlexTable contibutions = new FlexTable();
		vpanel.add( new Label( "Contributions:" ) );
		vpanel.add( contibutions );
		contibutions.setBorderWidth( 1 );
		
		contibutions.setText( 0, 0, "ID" );
		contibutions.setText( 0, 1, "We Thought" );
		contibutions.setText( 0, 2, "Vandalism" );
		contibutions.setText( 0, 3, "Constructive" );
		contibutions.setText( 0, 4, "Skipped" );
		contibutions.setText( 0, 5, "Required" );
		contibutions.setText( 0, 6, "Comments" );
		contibutions.setText( 0, 7, "Users" );
		
		int i = 1;
		
		for( final ClientClassification cc : list ) {
			final Edit edit = cc.edit;
			
			String comments = "";
			
			edit.comments.remove( cc.comment );
			edit.comments.add( "*" + cc.comment );
			
			for( String comment : edit.comments )
				comments += comment + "<br />\n";
			
			String users = "";
			for( User user : edit.users )
				users += user.nickName + " (" + user.classifications.toString() + ")" + ( user.isAdmin ? " (admin)" : "" ) + "<br />\n";
			Anchor anchor = new Anchor( edit.id.toString(), "javascript:void;" );
			
			anchor.addClickHandler( new ClickHandler(){

				@Override
				public void onClick( ClickEvent event ) {
					parent.doWait();
					rs.getId( edit.id, parent );
				}
				
			});
			
			contibutions.setWidget( i, 0, anchor );
			contibutions.setText( i, 1, edit.classification.toString() );
			contibutions.setText( i, 2, ( cc.classification.equals( Classification.VANDALISM ) ? "*" : "" ) + edit.vandalism.toString() );
			contibutions.setText( i, 3, ( cc.classification.equals( Classification.CONSTRUCTIVE ) ? "*" : "" ) + edit.constructive.toString() );
			contibutions.setText( i, 4, ( cc.classification.equals( Classification.SKIPPED ) ? "*" : "" ) + edit.skipped.toString() );
			contibutions.setText( i, 5, edit.required.toString() );
			contibutions.setHTML( i, 6, comments );
			contibutions.setHTML( i, 7, users );
			
			i++;
		}
		HorizontalPanel pagination = new HorizontalPanel();
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vpanel.add(pagination);
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_DEFAULT);
		
		final TextBox pageSize = new TextBox();
		final TextBox pageStart = new TextBox();
		KeyPressHandler gotoPage = new KeyPressHandler(){

			@Override
			public void onKeyPress( KeyPressEvent event ) {
				if( event.getCharCode() == 13 )
					pageTo( pageStart.getText(), new Integer( pageSize.getText() ) );
			}
			
		};
		
		pageStart.setText( start.toString() );
		pageStart.addKeyPressHandler( gotoPage );
		pagination.add( pageStart );
		
		pageSize.setText( count.toString() );
		pageSize.addKeyPressHandler( gotoPage );
		pagination.add( pageSize );
		
		Button pageLeft = new Button( "&lt;" );
		pageLeft.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				pageTo( list.get( 0 ).key, - new Integer( pageSize.getText() ) );
			}
			
		});
		pagination.add( pageLeft );
		
		Button pageRight = new Button( ">" );
		pageRight.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				pageTo( list.get( list.size() - 1 ).key, new Integer( pageSize.getText() ) );
			}
			
		});
		pagination.add( pageRight );
		
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vpanel.add( new Label( "* indicates your responses" ) );
		
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
		buttons.add( refreshButton );
		buttons.add( closeButton );
		
		vpanel.add(buttons);
		
		popup.setWidget( vpanel );
		if( !popup.isShowing() ) {
			popup.center();
			popup.show();
		}
	}
	
	public void pageTo( String start, Integer count ) {
		this.start = start;
		this.count = count;
		refresh();
	}
	
	public void refresh() {
		rs.getClassifications( start, count, new AsyncCallback< List< ClientClassification > >(){

			@Override
			public void onFailure( Throwable caught ) {
				ClueBotReviewInterface.error( caught.getMessage() );
			}

			@Override
			public void onSuccess( List< ClientClassification > result ) {
				count = Math.abs( count );
				list = result;
				display();
			}
			
		});
	}
	
	public ViewContributions( ClueBotReviewInterface parent ) {
		this.parent = parent;
		refresh();
	}
}
