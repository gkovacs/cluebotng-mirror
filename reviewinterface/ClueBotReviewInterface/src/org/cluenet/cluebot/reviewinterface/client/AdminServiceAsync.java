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
	 * @see org.cluenet.cluebot.reviewinterface.client.AdminService#createUser(java.lang.String, java.lang.Boolean)
	 */
	void createUser( String email, Boolean isAdmin, AsyncCallback< Void > callback );
	
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
	 * @see org.cluenet.cluebot.reviewinterface.client.AdminService#getEditGroup(java.lang.String)
	 */
	void getEditGroup( String key, AsyncCallback< EditGroup > callback );
	
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
	
}
