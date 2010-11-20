/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.client;

import java.util.List;
import org.cluenet.cluebot.reviewinterface.shared.Edit;
import org.cluenet.cluebot.reviewinterface.shared.EditGroup;
import org.cluenet.cluebot.reviewinterface.shared.User;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * @author cobi
 *
 */
public interface AdminServiceAsync {
	
	/**
	 * 
	 * @see org.cluenet.cluebot.reviewinterface.client.AdminService#createEditGroup(java.lang.String, java.lang.Integer, java.util.List)
	 */
	void createEditGroup( String name, Integer weight, Integer required, List< Edit > edits, AsyncCallback< Void > callback );
	
	/**
	 * 
	 * @see org.cluenet.cluebot.reviewinterface.client.AdminService#deleteEditGroup(java.lang.String)
	 */
	void deleteEditGroup( String key, AsyncCallback< Void > callback );
	
	/**
	 * 
	 * @see org.cluenet.cluebot.reviewinterface.client.AdminService#deleteUser(java.lang.String)
	 */
	void deleteUser( String key, AsyncCallback< Void > callback );
	
	/**
	 * 
	 * @see org.cluenet.cluebot.reviewinterface.client.AdminService#getEditGroups()
	 */
	void getEditGroups( AsyncCallback< List< EditGroup >> callback );
	
	/**
	 * 
	 * @see org.cluenet.cluebot.reviewinterface.client.AdminService#getUsers()
	 */
	void getUsers( AsyncCallback< List< User >> callback );

	void setAdmin( String key, Boolean isAdmin, AsyncCallback< Void > callback );

	void addEditsToEditGroup( String key, Integer required, List< Edit > edits, AsyncCallback< Void > callback );

	void getEditGroup( String key, Integer editStart, Integer editCount, Integer reviewStart, Integer reviewCount, Integer doneStart, Integer doneCount, AsyncCallback< EditGroup > callback );

	void setNick( String key, String nick, AsyncCallback< Void > callback );

	void reevalEditsInEditGroup( String key, AsyncCallback< Void > callback );

	void createUser( String nick, String email, Boolean isAdmin, Boolean sendEmail, Integer count, AsyncCallback< Void > callback );
	
}
