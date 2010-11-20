
package org.cluenet.cluebot.reviewinterface.client;

import java.util.List;
import org.cluenet.cluebot.reviewinterface.shared.Edit;
import org.cluenet.cluebot.reviewinterface.shared.EditGroup;
import org.cluenet.cluebot.reviewinterface.shared.User;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath( "admin" )
public interface AdminService extends RemoteService {
	List< EditGroup > getEditGroups() throws IllegalArgumentException;
	EditGroup getEditGroup( String key, Integer editStart, Integer editCount, Integer reviewStart, Integer reviewCount, Integer doneStart, Integer doneCount ) throws IllegalArgumentException;
	void createEditGroup( String name, Integer weight, Integer required, List< Edit > edits ) throws IllegalArgumentException;
	void deleteEditGroup( String key ) throws IllegalArgumentException;
	void addEditsToEditGroup( String key, Integer required, List< Edit > edits ) throws IllegalArgumentException;
	void reevalEditsInEditGroup( String key ) throws IllegalArgumentException;
	
	List< User > getUsers() throws IllegalArgumentException;
	void createUser( String nick, String email, Boolean isAdmin, Boolean sendEmail, Integer count ) throws IllegalArgumentException;
	void setAdmin( String key, Boolean isAdmin ) throws IllegalArgumentException;
	void setNick( String key, String nick ) throws IllegalArgumentException;
	void deleteUser( String key ) throws IllegalArgumentException;
}
