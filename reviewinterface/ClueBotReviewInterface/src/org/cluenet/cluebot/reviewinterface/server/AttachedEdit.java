package org.cluenet.cluebot.reviewinterface.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cluenet.cluebot.reviewinterface.shared.Status;
import org.datanucleus.store.appengine.query.JDOCursorHelper;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable(detachable="true")
public class AttachedEdit extends Persist {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private Key editGroup;
	
	@Persistent
	private Key edit;
	
	@SuppressWarnings( "unused" )
	@Persistent
	private Integer editId;
	
	@Persistent
	private Status status;
	
	@SuppressWarnings( "unused" )
	@Persistent
	private Integer random;
	
	@Persistent
	private List< Key > users;
	
	public AttachedEdit( EditGroup editGroup, Edit edit, Status status ) {
		this.editGroup = editGroup.getKey();
		this.edit = edit.getKey();
		this.editId = edit.getId();
		this.status = status;
		this.random = new Random().nextInt( Integer.MAX_VALUE );
		this.users = edit.getUsers();
		this.store();
	}

	@Override
	public Key getKey() {
		return key;
	}

	@SuppressWarnings( "unchecked" )
	public static List< AttachedEdit > findByEdit( Key editKey ) {
		PersistenceManager pm = JDOFilter.getPM();
		Query q = pm.newQuery( "SELECT FROM " + AttachedEdit.class.getName() + " WHERE edit == theEdit" );
		q.declareImports( "import com.google.appengine.api.datastore.Key;" );
		q.declareParameters( "Key theEdit" );
		return (List< AttachedEdit >) q.execute( editKey );
	}
	
	public static List< AttachedEdit > findByEdit( Edit edit ) {
		return findByEdit( edit.getKey() );
	}

	public EditGroup getEditGroup() {
		return EditGroup.findByKey( editGroup );
	}

	public static AttachedEdit findByEditGroupAndEdit( EditGroup editGroup, Edit edit ) {
		return findByEditGroupAndEdit( editGroup.getKey(), edit.getKey() );
	}
	
	public static AttachedEdit findByEditGroupAndEdit( Key editGroup, Edit edit ) {
		return findByEditGroupAndEdit( editGroup, edit.getKey() );
	}
	
	public static AttachedEdit findByEditGroupAndEdit( EditGroup editGroup, Key edit ) {
		return findByEditGroupAndEdit( editGroup.getKey(), edit );
	}
	
	@SuppressWarnings( "unchecked" )
	public static AttachedEdit findByEditGroupAndEdit( Key editGroup, Key edit ) {
		PersistenceManager pm = JDOFilter.getPM();
		Query q = pm.newQuery( "SELECT FROM " + AttachedEdit.class.getName() + " WHERE edit == theEdit && editGroup == theEditGroup" );
		q.declareImports( "import com.google.appengine.api.datastore.Key;" );
		q.declareParameters( "Key theEdit, Key theEditGroup" );
		List< AttachedEdit > ae = (List< AttachedEdit >) q.execute( edit, editGroup );
		if( ae.size() > 0 )
			return (AttachedEdit) ae.get( 0 );
		return null;
	}

	public void updateEditState() {
		Edit edit = Edit.findByKey( this.edit );
		
		Status status = edit.calculateStatus();
		
		if( this.status.equals( status ) )
			return;
		
		this.status = status;
		this.store();
	}
	
	public static Boolean hasWaitingEditsForEditGroup( EditGroup eg ) {
		return hasWaitingEditsForEditGroup( eg.getKey() );
	}
	
	@SuppressWarnings( "unchecked" )
	public static Boolean hasWaitingEditsForEditGroup( Key egKey ) {
		PersistenceManager pm = JDOFilter.getPM();
		Query q = pm.newQuery( "SELECT FROM " + AttachedEdit.class.getName() + " WHERE editGroup == theEditGroup && status != theStatus" );
		q.setRange( 0, 1 );
		q.declareImports( "import com.google.appengine.api.datastore.Key; import org.cluenet.cluebot.reviewinterface.shared.Status;" );
		q.declareParameters( "Key theEditGroup, Status theStatus" );
		List< AttachedEdit > list = (List< AttachedEdit >) q.execute( egKey, Status.DONE );
		return list.size() > 0;
	}

	public void addUser( User user ) {
		users.add( user.getKey() );
		this.store();
	}
	
	public static AttachedEdit getRandomByEditGroupUser( Key egKey, Key uKey ) {
		AttachedEdit ae = getRandomByEditGroupUser( egKey, uKey, Status.PARTIAL );
		if( ae != null )
			return ae;
		ae = getRandomByEditGroupUser( egKey, uKey, Status.NOTDONE );
		return ae;
	}
	
	@SuppressWarnings( "unchecked" )
	private static AttachedEdit getRandomByEditGroupUserHelperOne( Key egKey, Key uKey, Status status ) {
		Integer random = new Random().nextInt( Integer.MAX_VALUE );
		PersistenceManager pm = JDOFilter.getPM();
		Query q = pm.newQuery( AttachedEdit.class );
		q.setFilter( "editGroup == theEditGroup && status == theStatus && random >= theRandom" );
		q.setRange( 0, 1 );
		q.declareImports( "import com.google.appengine.api.datastore.Key; import org.cluenet.cluebot.reviewinterface.shared.Status;" );
		q.declareParameters( "Key theEditGroup, Status theStatus, Integer theRandom" );
		List< AttachedEdit > list = new ArrayList< AttachedEdit >( (List< AttachedEdit >) q.executeWithArray( new Object[]{ egKey, status, random } ) );
		if( list.size() > 0 )
			if( !list.get( 0 ).users.contains( uKey ) )
				return list.get( 0 );
		
		q = pm.newQuery( AttachedEdit.class );
		q.setFilter( "editGroup == theEditGroup && status == theStatus && random < theRandom" );
		q.setRange( 0, 1 );
		q.declareImports( "import com.google.appengine.api.datastore.Key; import org.cluenet.cluebot.reviewinterface.shared.Status;" );
		q.declareParameters( "Key theEditGroup, Status theStatus, Integer theRandom" );
		list = new ArrayList< AttachedEdit >( (List< AttachedEdit >) q.executeWithArray( new Object[]{ egKey, status, random } ) );
		if( list.size() > 0 )
			if( !list.get( 0 ).users.contains( uKey ) )
				return list.get( 0 );
		
		return null;
	}
	
	@SuppressWarnings( "unchecked" )
	private static AttachedEdit getRandomByEditGroupUserHelperAll( Key egKey, Key uKey, Status status ) {
		PersistenceManager pm = JDOFilter.getPM();
		Query q = pm.newQuery( AttachedEdit.class );
		q.setFilter( "editGroup == theEditGroup && status == theStatus" );
		q.declareImports( "import com.google.appengine.api.datastore.Key; import org.cluenet.cluebot.reviewinterface.shared.Status;" );
		q.declareParameters( "Key theEditGroup, Status theStatus" );
		List< AttachedEdit > list = (List< AttachedEdit >) q.executeWithArray( egKey, status );
		for( AttachedEdit ae : list )
			if( !ae.users.contains( uKey ) )
				return ae;
		return null;
	}
	
	public static AttachedEdit getRandomByEditGroupUser( Key egKey, Key uKey, Status status ) {
		AttachedEdit ae = null;
		
		ae = getRandomByEditGroupUserHelperOne( egKey, uKey, status );
		if( ae != null )
			return ae;
		
		if( status.equals( Status.NOTDONE ) )
			return null;
		else
			return getRandomByEditGroupUserHelperAll( egKey, uKey, status );
	}

	public static AttachedEdit getRandomByEditGroupUser( EditGroup editGroup, User user ) {
		return getRandomByEditGroupUser( editGroup.getKey(), user.getKey() );
	}

	public Edit getEdit() {
		return Edit.findByKey( edit );
	}

	@SuppressWarnings( "unchecked" )
	public static List< AttachedEdit > getByStatus( EditGroup editGroup, Integer start, Integer count, Status status ) {
		PersistenceManager pm = JDOFilter.getPM();
		List< AttachedEdit > list = new ArrayList< AttachedEdit >();
		
		String inequality = count < 0 ? "<" : ">";
		String order = count < 0 ? "desc" : "asc";
		count = Math.abs( count );
		
		try {
			Query q = pm.newQuery( AttachedEdit.class );
			q.setFilter( "editGroup == theEditGroup && status == theStatus && editId " + inequality + " theId" );
			q.setOrdering( "editId " + order );
			q.setRange( 0, count );
			q.declareImports( "import com.google.appengine.api.datastore.Key; import org.cluenet.cluebot.reviewinterface.shared.Status;" );
			q.declareParameters( "Key theEditGroup, Status theStatus, Integer theId" );
			list = (List< AttachedEdit >) q.execute( editGroup.getKey(), status, start );
			if( order.equals( "desc" ) ) {
				list = new ArrayList< AttachedEdit >( list );
				Collections.reverse( list );
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	@SuppressWarnings( "unchecked" )
	public static Integer countByStatus( EditGroup editGroup, Status status ) {
		PersistenceManager pm = JDOFilter.getPM();
		Integer count = 0;
		List< Key > list = new ArrayList< Key >();
		
		try {
			Cursor cursor = null;
			do {
				Query q = pm.newQuery( AttachedEdit.class );
				q.setResult( "key" );
				q.setFilter( "editGroup == theEditGroup && status == theStatus" );
				if( cursor != null ) {
					Map< String, Object > extensionMap = new HashMap< String, Object >();
			        extensionMap.put( JDOCursorHelper.CURSOR_EXTENSION, cursor );
			        q.setExtensions( extensionMap );
				}
				q.setRange( 0, 1000 );
				q.declareImports( "import com.google.appengine.api.datastore.Key; import org.cluenet.cluebot.reviewinterface.shared.Status;" );
				q.declareParameters( "Key theEditGroup, Status theStatus" );
				list = (List< Key >) q.execute( editGroup.getKey(), status );
				cursor = JDOCursorHelper.getCursor( list );
				count += list.size();
			} while( list.size() > 0 );
			
		} catch( Exception e ) {
			
		}
		
		return count;
	}
	
	@SuppressWarnings( "unchecked" )
	public static List< Key > getEditKeyByStatus( EditGroup editGroup, Status status ) {
		PersistenceManager pm = JDOFilter.getPM();
		List< Key > list = new ArrayList< Key >();
		List< Key > currList = new ArrayList< Key >();
		
		try {
			Cursor cursor = null;
			do {
				Query q = pm.newQuery( AttachedEdit.class );
				q.setResult( "edit" );
				q.setFilter( "editGroup == theEditGroup && status == theStatus" );
				if( cursor != null ) {
					Map< String, Object > extensionMap = new HashMap< String, Object >();
			        extensionMap.put( JDOCursorHelper.CURSOR_EXTENSION, cursor );
			        q.setExtensions( extensionMap );
				}
				q.setRange( 0, 1000 );
				q.declareImports( "import com.google.appengine.api.datastore.Key; import org.cluenet.cluebot.reviewinterface.shared.Status;" );
				q.declareParameters( "Key theEditGroup, Status theStatus" );
				currList = (List< Key >) q.execute( editGroup.getKey(), status );
				list.addAll( currList );
				cursor = JDOCursorHelper.getCursor( currList );
			} while( currList.size() > 0 );
			
		} catch( Exception e ) {
			
		}
		
		return list;
	}
	
}
