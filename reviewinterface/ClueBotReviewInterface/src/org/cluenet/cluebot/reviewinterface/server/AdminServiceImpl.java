
package org.cluenet.cluebot.reviewinterface.server;

import java.util.ArrayList;
import java.util.List;

import org.cluenet.cluebot.reviewinterface.client.AdminService;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings( "serial" )
public class AdminServiceImpl extends RemoteServiceServlet implements
		AdminService {

	@Override
	public void createEditGroup( String name, Integer weight, Integer required, List< org.cluenet.cluebot.reviewinterface.shared.Edit > edits ) throws IllegalArgumentException {
		List< Edit > list = new ArrayList< Edit >();
		EditGroup eg = new EditGroup( name, list, weight );
		String key = KeyFactory.keyToString( eg.getKey() );
		
		addEditsToEditGroup( key, required, edits );
	}

	@Override
	public void createUser( String email, Boolean isAdmin ) throws IllegalArgumentException {
		new User( new Email( email ), isAdmin );
	}

	@Override
	public void deleteEditGroup( String key ) throws IllegalArgumentException {
		EditGroup.findByKey( key ).delete();
	}

	@Override
	public void deleteUser( String key ) throws IllegalArgumentException {
		User.findByKey( key ).delete();
	}

	@Override
	public org.cluenet.cluebot.reviewinterface.shared.EditGroup getEditGroup( String key ) throws IllegalArgumentException {
		return EditGroup.findByKey( key ).getClientClass();
	}

	@Override
	public List< org.cluenet.cluebot.reviewinterface.shared.EditGroup > getEditGroups() throws IllegalArgumentException {
		List< org.cluenet.cluebot.reviewinterface.shared.EditGroup > list = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.EditGroup >();
		for( EditGroup eg : EditGroup.list() )
			list.add( eg.getLightClientClass() );
		return list;
	}

	@Override
	public List< org.cluenet.cluebot.reviewinterface.shared.User > getUsers() throws IllegalArgumentException {
		List< org.cluenet.cluebot.reviewinterface.shared.User > list = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.User >();
		for( User user : User.list() )
			list.add( user.getClientClass() );
		return list;
	}

	@Override
	public void setAdmin( String key, Boolean isAdmin ) throws IllegalArgumentException {
		User.findByKey( key ).setAdmin( isAdmin );
	}

	@Override
	public void addEditsToEditGroup( String key, Integer required, List< org.cluenet.cluebot.reviewinterface.shared.Edit > edits ) throws IllegalArgumentException {
		Queue queue = QueueFactory.getQueue( "add-edit-queue" );
		for( int i = 0 ; i < edits.size() / 100 ; i++ ) {
			int fromIndex = 100 * i;
			int toIndex = fromIndex + 99;
			if( toIndex >= edits.size() )
				toIndex = edits.size() - 1;
			List< TaskOptions > tasks = new ArrayList< TaskOptions >();
			for( org.cluenet.cluebot.reviewinterface.shared.Edit edit : edits.subList( fromIndex, toIndex ) )
				tasks.add( TaskOptions.Builder.param( "id", edit.id.toString() ).param( "classification", edit.classification.toString() ).param( "required", required.toString() ).param( "weight", edit.weight.toString() ).param( "key", key ).method( Method.GET ) );
			queue.add( tasks );
		}
	}

}
