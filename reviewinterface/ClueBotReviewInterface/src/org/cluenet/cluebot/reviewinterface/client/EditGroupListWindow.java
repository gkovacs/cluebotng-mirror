package org.cluenet.cluebot.reviewinterface.client;

import java.util.List;

import org.cluenet.cluebot.reviewinterface.shared.EditGroup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


public class EditGroupListWindow implements Refreshable {
	private DialogBox popup = null;
	private List< EditGroup > editGroups;
	private final AdminServiceAsync admin = GWT.create( AdminService.class );

	private void display() {
		if( popup == null )
			popup = new DialogBox();
		popup.setText( "Edit Group List" );
		popup.setAnimationEnabled( true );
		popup.setModal( false );
		
		VerticalPanel vpanel = new VerticalPanel();

		FlexTable editGroupTable = new FlexTable();
		vpanel.add( editGroupTable );
		editGroupTable.setBorderWidth( 1 );
		
		editGroupTable.setText( 0, 0, "Actions" );
		editGroupTable.setText( 0, 1, "Name" );
		editGroupTable.setText( 0, 2, "Weight" );
		editGroupTable.setText( 0, 3, "Done" );
		editGroupTable.setText( 0, 4, "Partial" );
		editGroupTable.setText( 0, 5, "To Do" );
		
		
		int i = 1;
		
		for( final EditGroup eg : editGroups ) {
			HorizontalPanel actions = new HorizontalPanel();
			editGroupTable.setWidget( i, 0, actions );
			editGroupTable.setText( i, 1, eg.name );
			editGroupTable.setText( i, 2, eg.weight.toString() );
			editGroupTable.setText( i, 3, eg.countDone.toString() );
			editGroupTable.setText( i, 4, eg.countReviewed.toString() );
			editGroupTable.setText( i, 5, eg.countLeft.toString() );
			
			
			Button delete = new Button("X");
			delete.addClickHandler( new ClickHandler(){

				@Override
				public void onClick( ClickEvent event ) {
					admin.deleteEditGroup( eg.key, new AsyncCallback< Void >() {
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
			actions.add( delete );
			
			Button add = new Button("+");
			add.addClickHandler( new ClickHandler(){

				@Override
				public void onClick( ClickEvent event ) {
					addEdits( eg );
				}
				
			});
			actions.add( add );
			
			Button view = new Button("View");
			view.addClickHandler( new ClickHandler(){

				@Override
				public void onClick( ClickEvent event ) {
					viewEditGroup( eg.key );
				}
				
			});
			actions.add( view );
			
			i++;
		}
		
		Button refreshButton = new Button("Refresh");
		refreshButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
			
		});
		
		Button newButton = new Button("New");
		newButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				newEditGroup();
			}
			
		});
		
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.add( new Anchor( "Download as tar", "/download?format=tar" ) );
		buttons.add( refreshButton );
		buttons.add( newButton );
		
		vpanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vpanel.add(buttons);
		
		popup.setWidget( vpanel );
		if( !popup.isShowing() ) {
			popup.center();
			popup.show();
		}
	}
	
	private void addEdits( EditGroup editGroup ) {
		new NewEditGroupWindow( this, editGroup.key, editGroup.name, editGroup.weight );
	}
	
	private void newEditGroup() {
		new NewEditGroupWindow( this );
	}
	
	private void viewEditGroup( String key ) {
		new ViewEditGroupWindow( key );
	}
	
	public void refresh() {
		admin.getEditGroups( new AsyncCallback< List< EditGroup > >(){

			@Override
			public void onFailure( Throwable caught ) {
				ClueBotReviewAdminInterface.error( caught.getMessage() );
			}

			@Override
			public void onSuccess( List< EditGroup > result ) {
				editGroups = result;
				display();
			}
			
		});
	}
	
	public EditGroupListWindow() {
		refresh();
	}
}
