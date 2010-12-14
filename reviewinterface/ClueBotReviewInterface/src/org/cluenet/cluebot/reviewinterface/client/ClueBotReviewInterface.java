
package org.cluenet.cluebot.reviewinterface.client;

import java.util.ArrayList;
import java.util.List;

import org.cluenet.cluebot.reviewinterface.shared.Classification;
import org.cluenet.cluebot.reviewinterface.shared.Edit;
import org.cluenet.cluebot.reviewinterface.shared.ReturnData;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ClueBotReviewInterface implements EntryPoint, AsyncCallback< ReturnData > {

	private DialogBox pleaseWait;
	private Edit currentEdit;
	private List< RadioButton > options;
	
	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final ReviewServiceAsync review = GWT.create( ReviewService.class );
	
	private void setURL( String url ) {
		RootPanel.get( "iframe" ).getElement().setAttribute( "src", url );
	}
	
	private void setUser( String name, Integer count ) {
		RootPanel.get( "username" ).getElement().setInnerText( name );
		
		Anchor anchor = new Anchor( "javascript:void;" );
		anchor.setText( count.toString() );
		anchor.addClickHandler( new ClickHandler() {

			@Override
			public void onClick( ClickEvent event ) {
				viewContributions();
			}
			
		});
		
		RootPanel.get( "count" ).clear();
		RootPanel.get( "count" ).add( anchor );
	}
	
	private void viewContributions() {
		new ViewContributions( this );
	}
	
	private void setId( Integer id, final String url, Boolean isAdmin ) {
		Anchor anchor;
		
		if( !isAdmin )
			if( Window.Location.getHash().equals( "#admin" ) )
				isAdmin = true;
		
		if( isAdmin )
			anchor = new Anchor( "javascript:void;" );
		else
			anchor = new Anchor( url );
		
		anchor.setText( id.toString() );
		
		if( isAdmin )
			anchor.addClickHandler( new ClickHandler() {
	
				@Override
				public void onClick( ClickEvent event ) {
					new EditInfoWindow( currentEdit, url );
				}
				
			});
		
		RootPanel.get( "editid" ).clear();
		RootPanel.get( "editid" ).add( anchor );
		
		if( isAdmin ) {
			final TextBox gotoId = new TextBox();
			gotoId.addKeyPressHandler( new KeyPressHandler(){
	
				@Override
				public void onKeyPress( KeyPressEvent event ) {
					if( event.getCharCode() == 13 )
						gotoPage( new Integer( gotoId.getText() ) );
				}
			
			});
			
			RootPanel.get( "goto" ).clear();
			RootPanel.get( "goto" ).add( gotoId );
		}
	}
	
	public void gotoPage( Integer id ) {
		doWait();
		review.getId( id, this );
	}
	
	public static void error( String error ) {
		VerticalPanel hpanel = new VerticalPanel();
		hpanel.add( new Label( "An error occurred.  Please refresh the page." ) );
		hpanel.add( new Label( error ) );
		DialogBox errorBox = new DialogBox();
		errorBox.setText( "Error" );
		errorBox.setAnimationEnabled( true );
		errorBox.add( hpanel );
		errorBox.center();
		errorBox.setModal( true );
		errorBox.show();
	}
	
	public void doWait() {
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
	
	private void classify( Classification type, String comment, Boolean sure ) {
		if( currentEdit.classification != Classification.UNKNOWN && !currentEdit.classification.equals( type ) && !sure ) {
			getSure( type, comment );
			return;
		}
		
		doWait();
		if( comment.equals("") )
			comment = null;
		review.reviewId( currentEdit.id, type, comment, this );
	}
	
	private void getSure( final Classification type, final String comment ) {
		final DialogBox sure = new DialogBox();
		VerticalPanel vpanel = new VerticalPanel();
		vpanel.add( new Label( "Are you very sure?" ) );
		
		HorizontalPanel buttons = new HorizontalPanel();
		vpanel.add( buttons );
		
		Button yes = new Button( "Yes" );
		yes.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				sure.hide();
				classify( type, comment, true );
			}
			
		});
		buttons.add( yes );
		
		Button no = new Button( "No" );
		no.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				sure.hide();
			}
			
		});
		buttons.add( no );
		sure.setText( "Are you sure?" );
		sure.setAnimationEnabled( true );
		sure.setWidget( vpanel );
		sure.center();
		sure.setModal( true );
		sure.show();
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
				classify( Classification.VANDALISM, comment.getText(), false );
				comment.setText( "" );
			}
			
		});
		
		Button skip = new Button( "Skip" );
		skip.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				classify( Classification.SKIPPED, comment.getText(), false );
				comment.setText( "" );
			}
			
		});
		
		Button constructive = new Button( "Constructive" );
		constructive.addClickHandler( new ClickHandler(){

			@Override
			public void onClick( ClickEvent event ) {
				classify( Classification.CONSTRUCTIVE, comment.getText(), false );
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
	
	public boolean isParsableToInt( String i ) {
		try {
			Integer.parseInt(i);
			return true;
		} catch( NumberFormatException nfe ) {
			return false;
		}
	}
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		showButtons();
		setOptions();
		doWait();
		if( Window.Location.getHash() != null && isParsableToInt( Window.Location.getHash().substring( 1 ) ) )
			review.getId( new Integer( Window.Location.getHash().substring( 1 ) ), this );	
		else
			review.getId( this );
	}

	@Override
	public void onFailure( Throwable caught ) {
		doneWait();
		VerticalPanel hpanel = new VerticalPanel();
		hpanel.add( new Label( "An error occurred.  Please refresh the page." ) );
		hpanel.add( new Label( caught.getMessage() ) );
		DialogBox errorBox = new DialogBox();
		errorBox.setText( "Error" );
		errorBox.setAnimationEnabled( true );
		errorBox.add( hpanel );
		errorBox.center();
		errorBox.setModal( true );
		errorBox.show();
	}

	@Override
	public void onSuccess( ReturnData result ) {
		doneWait();
		setUser( result.user.userName, result.user.classifications );
		currentEdit = result.edit;
		setURL( getURL() + currentEdit.id );
		setId( currentEdit.id, getURL() + currentEdit.id, result.user.isAdmin );
	}
	
	private String getURL() {
		String option = "Normal";
		for( RadioButton rb : options )
			if( rb.getValue() )
				option = rb.getText();
		
		if( option.equals( "Normal" ) )
			return "http://en.wikipedia.org/w/index.php?action=view&diff=";
		if( option.equals( "Diff only" ) )
			return "http://en.wikipedia.org/w/index.php?action=view&diffonly=1&diff=";
		if( option.equals( "Render" ) )
			return "http://en.wikipedia.org/w/index.php?action=render&diffonly=1&diff=";
		
		return "http://en.wikipedia.org/w/index.php?action=view&diff=";
	}

	private void setOptions() {
		options = new ArrayList< RadioButton >();
		HorizontalPanel hpanel = new HorizontalPanel();
		RadioButton rb;
		rb = new RadioButton( "url", "Normal" );
		rb.setValue( true );
		hpanel.add( rb );
		options.add( rb );
		
		rb = new RadioButton( "url", "Diff only" );
		hpanel.add( rb );
		options.add( rb );
		
		rb = new RadioButton( "url", "Render" );
		hpanel.add( rb );
		options.add( rb );
		
		hpanel.setStyleName( "options" );
		
		RootPanel.get( "options" ).add( hpanel );
	}
}
