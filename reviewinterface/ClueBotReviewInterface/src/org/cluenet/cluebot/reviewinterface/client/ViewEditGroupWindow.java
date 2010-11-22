package org.cluenet.cluebot.reviewinterface.client;

import org.cluenet.cluebot.reviewinterface.shared.Edit;
import org.cluenet.cluebot.reviewinterface.shared.EditGroup;
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


public class ViewEditGroupWindow implements Refreshable {
	private DialogBox popup = null;
	private EditGroup editGroup;
	private Integer editStart = 0;
	private Integer editCount = 20;
	private Integer reviewStart = 0;
	private Integer reviewCount = 20;
	private Integer doneStart = 0;
	private Integer doneCount = 20;
	
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
		properties.setText( 2, 0, "Done:" );
		properties.setText( 2, 1, editGroup.countDone.toString() );
		properties.setText( 3, 0, "Partial:" );
		properties.setText( 3, 1, editGroup.countReviewed.toString() );
		properties.setText( 4, 0, "To do:" );
		properties.setText( 4, 1, editGroup.countLeft.toString() );
		properties.setText( 5, 0, "Links:" );
		properties.setWidget( 5, 1, new Anchor( "Dump done", "/download?format=done&editgroup=" + editGroup.key ) );
		vpanel.add( properties );

		FlexTable editTable = new FlexTable();
		vpanel.add( new Label( "Active:" ) );
		vpanel.add( editTable );
		editTable.setBorderWidth( 1 );
		
		editTable.setText( 0, 0, "ID" );
		editTable.setText( 0, 1, "Type" );
		editTable.setText( 0, 2, "Vandalism" );
		editTable.setText( 0, 3, "Constructive" );
		editTable.setText( 0, 4, "Skipped" );
		editTable.setText( 0, 5, "Required" );
		editTable.setText( 0, 6, "Comments" );
		editTable.setText( 0, 7, "Users" );
		
		int i = 1;
		
		for( final Edit edit : editGroup.edits ) {
			String comments = "";
			for( String comment : edit.comments )
				comments += comment + "\n";
			
			String users = "";
			for( User user : edit.users )
				users += user.userName + " (" + user.classifications.toString() + ")" + ( user.isAdmin ? " (admin)" : "" ) + "\n";
			
			editTable.setWidget( i, 0, new Anchor( edit.id.toString(), "http://en.wikipedia.org/w/index.php?action=view&diff=" + edit.id.toString() ) );
			editTable.setText( i, 1, edit.classification.toString() );
			editTable.setText( i, 2, edit.vandalism.toString() );
			editTable.setText( i, 3, edit.constructive.toString() );
			editTable.setText( i, 4, edit.skipped.toString() );
			editTable.setText( i, 5, edit.required.toString() );
			editTable.setText( i, 6, comments );
			editTable.setText( i, 7, users );
			
			i++;
		}
		HorizontalPanel editPage = new HorizontalPanel();
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vpanel.add(editPage);
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_DEFAULT);
		
		final TextBox editPageSize = new TextBox();
		final TextBox editPageStart = new TextBox();
		KeyPressHandler gotoEditPage = new KeyPressHandler(){

			@Override
			public void onKeyPress( KeyPressEvent event ) {
				if( event.getCharCode() == 13 )
					pageTo( new Integer( editPageStart.getText() ), new Integer( editPageSize.getText() ), reviewStart, reviewCount, doneStart, doneCount );
			}
			
		};
		
		editPageStart.setText( editStart.toString() );
		editPageStart.addKeyPressHandler( gotoEditPage );
		editPage.add( editPageStart );
		
		editPageSize.setText( editCount.toString() );
		editPageSize.addKeyPressHandler( gotoEditPage );
		editPage.add( editPageSize );
		
		Button editPageLeft = new Button( "&lt;" );
		editPageLeft.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				pageTo( editGroup.edits.get( 0 ).id, - new Integer( editPageSize.getText() ), reviewStart, reviewCount, doneStart, doneCount );
			}
			
		});
		editPage.add( editPageLeft );
		
		Button editPageRight = new Button( ">" );
		editPageRight.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				pageTo( editGroup.edits.get( editGroup.edits.size() - 1 ).id, new Integer( editPageSize.getText() ), reviewStart, reviewCount, doneStart, doneCount );
			}
			
		});
		editPage.add( editPageRight );
		
		vpanel.add( new Label( "Partial:" ) );
		FlexTable reviewTable = new FlexTable();
		vpanel.add( reviewTable );
		reviewTable.setBorderWidth( 1 );
		
		reviewTable.setText( 0, 0, "ID" );
		reviewTable.setText( 0, 1, "Type" );
		reviewTable.setText( 0, 2, "Vandalism" );
		reviewTable.setText( 0, 3, "Constructive" );
		reviewTable.setText( 0, 4, "Skipped" );
		reviewTable.setText( 0, 5, "Required" );
		reviewTable.setText( 0, 6, "Comments" );
		reviewTable.setText( 0, 7, "Users" );
		
		i = 1;
		
		for( final Edit edit : editGroup.reviewed ) {
			String comments = "";
			for( String comment : edit.comments )
				comments += comment + "<br />\n";
			
			String users = "";
			for( User user : edit.users )
				users += user.userName + " (" + user.classifications.toString() + ")" + ( user.isAdmin ? " (admin)" : "" ) + "<br />\n";
			
			reviewTable.setWidget( i, 0, new Anchor( edit.id.toString(), "http://en.wikipedia.org/w/index.php?action=view&diff=" + edit.id.toString() ) );
			reviewTable.setText( i, 1, edit.classification.toString() );
			reviewTable.setText( i, 2, edit.vandalism.toString() );
			reviewTable.setText( i, 3, edit.constructive.toString() );
			reviewTable.setText( i, 4, edit.skipped.toString() );
			reviewTable.setText( i, 5, edit.required.toString() );
			reviewTable.setHTML( i, 6, comments );
			reviewTable.setHTML( i, 7, users );
			
			i++;
		}
		
		HorizontalPanel reviewPage = new HorizontalPanel();
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vpanel.add(reviewPage);
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_DEFAULT);
		
		final TextBox reviewPageSize = new TextBox();
		final TextBox reviewPageStart = new TextBox();
		KeyPressHandler gotoReviewPage = new KeyPressHandler(){

			@Override
			public void onKeyPress( KeyPressEvent event ) {
				if( event.getCharCode() == 13 )
					pageTo( editStart, editCount, new Integer( reviewPageStart.getText() ), new Integer( reviewPageSize.getText() ), doneStart, doneCount );
			}
			
		};
		
		reviewPageStart.setText( reviewStart.toString() );
		reviewPageStart.addKeyPressHandler( gotoReviewPage );
		reviewPage.add( reviewPageStart );
		
		reviewPageSize.setText( reviewCount.toString() );
		reviewPageSize.addKeyPressHandler( gotoReviewPage );
		reviewPage.add( reviewPageSize );
		
		Button reviewPageLeft = new Button( "&lt;" );
		reviewPageLeft.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				pageTo( editStart, editCount, editGroup.reviewed.get( 0 ).id, - new Integer( reviewPageSize.getText() ), doneStart, doneCount );
			}
			
		});
		reviewPage.add( reviewPageLeft );
		
		Button reviewPageRight = new Button( ">" );
		reviewPageRight.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				pageTo( editStart, editCount, editGroup.reviewed.get( editGroup.reviewed.size() - 1 ).id, new Integer( reviewPageSize.getText() ), doneStart, doneCount );
			}
			
		});
		reviewPage.add( reviewPageRight );
		
		vpanel.add( new Label( "Finished:" ) );
		FlexTable doneTable = new FlexTable();
		vpanel.add( doneTable );
		doneTable.setBorderWidth( 1 );
		
		doneTable.setText( 0, 0, "ID" );
		doneTable.setText( 0, 1, "Type" );
		doneTable.setText( 0, 2, "Vandalism" );
		doneTable.setText( 0, 3, "Constructive" );
		doneTable.setText( 0, 4, "Skipped" );
		doneTable.setText( 0, 5, "Required" );
		doneTable.setText( 0, 6, "Comments" );
		doneTable.setText( 0, 7, "Users" );
		
		i = 1;
		
		for( final Edit edit : editGroup.done ) {
			String comments = "";
			for( String comment : edit.comments )
				comments += comment + "<br />\n";
			
			String users = "";
			for( User user : edit.users )
				users += user.userName + " (" + user.classifications.toString() + ")" + ( user.isAdmin ? " (admin)" : "" ) + "<br />\n";
			
			doneTable.setWidget( i, 0, new Anchor( edit.id.toString(), "http://en.wikipedia.org/w/index.php?action=view&diff=" + edit.id.toString() ) );
			doneTable.setText( i, 1, edit.classification.toString() );
			doneTable.setText( i, 2, edit.vandalism.toString() );
			doneTable.setText( i, 3, edit.constructive.toString() );
			doneTable.setText( i, 4, edit.skipped.toString() );
			doneTable.setText( i, 5, edit.required.toString() );
			doneTable.setHTML( i, 6, comments );
			doneTable.setHTML( i, 7, users );
			
			i++;
		}
		
		HorizontalPanel donePage = new HorizontalPanel();
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vpanel.add(donePage);
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_DEFAULT);
		
		final TextBox donePageSize = new TextBox();
		final TextBox donePageStart = new TextBox();
		KeyPressHandler gotoDonePage = new KeyPressHandler(){

			@Override
			public void onKeyPress( KeyPressEvent event ) {
				if( event.getCharCode() == 13 )
					pageTo( editStart, editCount, reviewStart, reviewCount, new Integer( donePageStart.getText() ), new Integer( donePageSize.getText() ) );
			}
			
		};
		
		donePageStart.setText( doneStart.toString() );
		donePageStart.addKeyPressHandler( gotoDonePage );
		donePage.add( donePageStart );
		
		donePageSize.setText( doneCount.toString() );
		donePageSize.addKeyPressHandler( gotoDonePage );
		donePage.add( donePageSize );
		
		Button donePageLeft = new Button( "&lt;" );
		donePageLeft.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				pageTo( editStart, editCount, reviewStart, reviewCount, editGroup.done.get( 0 ).id, - new Integer( donePageSize.getText() ) );
			}
			
		});
		donePage.add( donePageLeft );
		
		Button donePageRight = new Button( ">" );
		donePageRight.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				pageTo( editStart, editCount, reviewStart, reviewCount, editGroup.done.get( editGroup.done.size() - 1 ).id, new Integer( donePageSize.getText() ) );
			}
			
		});
		donePage.add( donePageRight );
		
		
		Button newButton = new Button("New");
		newButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				addEdits();
			}
			
		});
		
		Button reevalButton = new Button("Re-sort");
		reevalButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				admin.reevalEditsInEditGroup( editGroup.key, new AsyncCallback< Void >() {

					@Override
					public void onFailure( Throwable caught ) {
						ClueBotReviewAdminInterface.error( caught.getMessage() );
					}

					@Override
					public void onSuccess( Void result ) {
						refresh();
					}
					
				});
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
		buttons.add( reevalButton );
		buttons.add( refreshButton );
		buttons.add( closeButton );
		
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vpanel.add(buttons);
		
		popup.setWidget( vpanel );
		if( !popup.isShowing() ) {
			popup.center();
			popup.show();
		}
	}
	
	private void addEdits() {
		new NewEditGroupWindow( this, editGroup.key, editGroup.name, editGroup.weight );
	}
	
	public void pageTo( Integer eStart, Integer eCount, Integer rStart, Integer rCount, Integer dStart, Integer dCount ) {
		editStart = eStart;
		editCount = eCount;
		reviewStart = rStart;
		reviewCount = rCount;
		doneStart = dStart;
		doneCount = dCount;
		refresh();
	}
	
	public void refresh() {
		admin.getEditGroup( key, editStart, editCount, reviewStart, reviewCount, doneStart, doneCount, new AsyncCallback< EditGroup >() {

			@Override
			public void onFailure( Throwable caught ) {
				ClueBotReviewAdminInterface.error( caught.getMessage() );
			}

			@Override
			public void onSuccess( EditGroup result ) {
				editGroup = result;
				editCount = Math.abs( editCount );
				reviewCount = Math.abs( reviewCount );
				doneCount = Math.abs( doneCount );
				display();
			}
			
		});
	}
	
	public ViewEditGroupWindow( String key ) {
		this.key = key;
		refresh();
	}
}
