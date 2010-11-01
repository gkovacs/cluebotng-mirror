
package org.cluenet.cluebot.reviewinterface.client;

import org.cluenet.cluebot.reviewinterface.shared.Classification;
import org.cluenet.cluebot.reviewinterface.shared.Edit;
import org.cluenet.cluebot.reviewinterface.shared.ReturnData;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ClueBotReviewInterface implements EntryPoint, AsyncCallback< ReturnData > {

	private DialogBox pleaseWait;
	private Edit currentEdit;
	
	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final ReviewServiceAsync review = GWT.create( ReviewService.class );
	
	private void setURL( String url ) {
		RootPanel.get( "iframe" ).getElement().setAttribute( "src", url );
	}
	
	private void setUser( String name, Integer count ) {
		RootPanel.get( "username" ).getElement().setInnerText( name );
		RootPanel.get( "count" ).getElement().setInnerText( count.toString() );
	}
	
	private void doWait() {
		pleaseWait = new DialogBox();
		pleaseWait.setText( "Please Wait" );
		pleaseWait.setAnimationEnabled( true );
		pleaseWait.add( new Label( "Please Wait" ) );
		pleaseWait.center();
		pleaseWait.setModal( true );
		pleaseWait.show();
	}
	
	private void doneWait() {
		pleaseWait.hide();
		pleaseWait = null;
	}
	
	private void classify( Classification type, String comment ) {
		doWait();
		if( comment.equals("") )
			comment = null;
		review.reviewId( currentEdit.id, type, comment, this );
	}
	
	private void showButtons() {
		VerticalPanel vpanel = new VerticalPanel();
		HorizontalPanel commentPanel = new HorizontalPanel();
		commentPanel.add( new Label( "Comment: " ) );
		final TextBox comment = new TextBox();
		commentPanel.add( comment );
		vpanel.add( commentPanel );
		Button vandalism = new Button( "Vandalism" );
		vandalism.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				classify( Classification.VANDALISM, comment.getText() );
				comment.setText( "" );
			}
			
		});
		
		Button skip = new Button( "Skip" );
		skip.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				classify( Classification.SKIPPED, comment.getText() );
				comment.setText( "" );
			}
			
		});
		
		Button constructive = new Button( "Constructive" );
		constructive.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				classify( Classification.CONSTRUCTIVE, comment.getText() );
				comment.setText( "" );
			}
			
		});
		
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.add( vandalism );
		buttons.add( skip );
		buttons.add( constructive );
		vpanel.add( buttons );
		DialogBox buttonBox = new DialogBox();
		buttonBox.setText( "Commands" );
		buttonBox.setAnimationEnabled( true );
		buttonBox.setWidget( vpanel );
		buttonBox.setModal( false );
		buttonBox.center();
		buttonBox.show();
	}
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		showButtons();
		doWait();
		review.getId( this );
	}

	@Override
	public void onFailure( Throwable caught ) {
		doneWait();
		DialogBox errorBox = new DialogBox();
		errorBox.setText( "Error" );
		errorBox.setAnimationEnabled( true );
		errorBox.add( new Label( "An error occurred.  Please refresh the page." ) );
		errorBox.center();
		errorBox.setModal( true );
		errorBox.show();
	}

	@Override
	public void onSuccess( ReturnData result ) {
		doneWait();
		setUser( result.user.userName, result.user.classifications );
		currentEdit = result.edit;
		setURL( "http://en.wikipedia.org/w/index.php?action=view&diff=" + currentEdit.id );
	}
}
