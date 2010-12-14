package org.cluenet.cluebot.reviewinterface.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cluenet.cluebot.reviewinterface.shared.Classification;
import org.cluenet.cluebot.reviewinterface.shared.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

@PersistenceCapable(detachable="true")
public class Edit extends Persist {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5914711884888506485L;

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private Integer id;
	
	@Persistent
	private Classification known;
	
	@Persistent
	private Integer required;
	
	
	private Edit( Integer id, Classification known, Integer required ) {
		this.id = id;
		this.known = known;
		this.required = required;
		this.store();
	}

	@Override
	public Key getKey() {
		return key;
	}

	
	public Classification getKnown() {
		return known;
	}

	
	public void setKnown( Classification known ) {
		this.known = known;
		this.store();
	}

	
	public Integer getRequired() {
		return required;
	}

	
	public void setRequired( Integer required ) {
		this.required = required;
		this.store();
	}

	
	public Integer getId() {
		return id;
	}
	
	public Status calculateStatus() {
		Integer required = getRequired();
		Map< Classification, Integer > map = count();
		Integer constructive = map.containsKey( Classification.CONSTRUCTIVE ) ? map.get( Classification.CONSTRUCTIVE ) : 0;
		Integer vandalism = map.containsKey( Classification.VANDALISM ) ? map.get( Classification.VANDALISM ) : 0;
		Integer skipped = map.containsKey( Classification.SKIPPED ) ? map.get( Classification.SKIPPED ) : 0;
		Integer sum = constructive + vandalism + skipped;
		Integer max = Math.max( constructive, Math.max( vandalism, skipped ) );

		if( sum == 0 )
			return Status.NOTDONE;

		if( max >= required )
			if( !calculateClassification().equals( Classification.UNKNOWN ) )
				return Status.DONE;
		
		return Status.PARTIAL;
	}
	
	public Classification calculateClassification() {
		Map< Classification, Integer > map = count();
		Integer constructive = map.containsKey( Classification.CONSTRUCTIVE ) ? map.get( Classification.CONSTRUCTIVE ) : 0;
		Integer vandalism = map.containsKey( Classification.VANDALISM ) ? map.get( Classification.VANDALISM ) : 0;
		Integer skipped = map.containsKey( Classification.SKIPPED ) ? map.get( Classification.SKIPPED ) : 0;
		Integer sum = constructive + vandalism + skipped;
		Integer max = Math.max( constructive, Math.max( vandalism, skipped ) );

		if( max < required )
			return Classification.UNKNOWN;
		
		if( 2 * skipped > sum )
			return Classification.SKIPPED;
		if( constructive >= 3 * vandalism )
			return Classification.CONSTRUCTIVE;
		if( vandalism >= 3 * constructive )
			return Classification.VANDALISM;

		return Classification.UNKNOWN;
	}
	
	@SuppressWarnings( "unchecked" )
	private List< AttachedEdit > attached() {
		PersistenceManager pm = JDOFilter.getPM();
		Query q = pm.newQuery( AttachedEdit.class );
		q.setFilter( "edit == thisEdit" );
		q.declareImports( "import com.google.appengine.api.datastore.Key;" );
		q.declareParameters( "Key thisEdit" );
		return new ArrayList< AttachedEdit >( (List< AttachedEdit >) q.execute( key ) );
	}
	
	@SuppressWarnings( "unchecked" )
	public List< EditClassification > classifications() {
		PersistenceManager pm = JDOFilter.getPM();
		Query q = pm.newQuery( EditClassification.class );
		q.setFilter( "edit == thisEdit" );
		q.declareImports( "import com.google.appengine.api.datastore.Key;" );
		q.declareParameters( "Key thisEdit" );
		return new ArrayList< EditClassification >( (List< EditClassification >) q.execute( key ) );
	}
	
	private Map< Classification, Integer > count() {
		Map< Classification, Integer > map = new HashMap< Classification, Integer >();
		for( EditClassification classification : classifications() ) {
			Classification type = classification.getClassification();
			if( !map.containsKey( type ) )
				map.put( type, 0 );
			map.put( type, map.get( type ) + 1 );
		}
		return map;	
	}
	
	private Integer count( Classification type ) {
		Map< Classification, Integer > map = count();
		if( map.containsKey( type ) )
			return map.get( type );
		return 0;
	}
	
	public Integer getVandalism() {
		return count( Classification.VANDALISM );
	}
	
	public Integer getConstructive() {
		return count( Classification.CONSTRUCTIVE );
	}
	
	public Integer getSkipped() {
		return count( Classification.SKIPPED );
	}
	
	public org.cluenet.cluebot.reviewinterface.shared.Edit getClientClass() {
		return getClass( false, false );
	}
	
	public org.cluenet.cluebot.reviewinterface.shared.Edit getAdminClass() {
		return getClass( true, false );
	}
	
	public org.cluenet.cluebot.reviewinterface.shared.Edit getLightClass() {
		return getClass( false, true );
	}
	
	private org.cluenet.cluebot.reviewinterface.shared.Edit getClass( Boolean admin, Boolean light ) {
		List< org.cluenet.cluebot.reviewinterface.shared.User > users = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.User >();
		List< org.cluenet.cluebot.reviewinterface.shared.ClientClassification > classifications = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.ClientClassification >();
		Integer vandalism = 0, constructive = 0, skipped = 0;
		List< String > comments = new ArrayList< String >();
		for( EditClassification classification : classifications() ) {
			if( classification.getClassification().equals( Classification.CONSTRUCTIVE ) )
				constructive++;
			else if( classification.getClassification().equals( Classification.VANDALISM ) )
				vandalism++;
			else if( classification.getClassification().equals( Classification.SKIPPED ) )
				skipped++;
			if( admin )
				users.add( classification.getUser().getClientClass() );
			else
				users.add( classification.getUser().getPublicClientClass() );
			comments.add( classification.getComment() );
			if( !light )
				classifications.add( classification.getClientClass( admin ) );
		}
		return new org.cluenet.cluebot.reviewinterface.shared.Edit(
				id,
				known,
				vandalism,
				constructive,
				skipped,
				getRequired(),
				comments,
				users,
				classifications
		);
	}

	
	@Override
	public void delete() {
		try {
			if( TheCache.cache().containsKey( "Edit-Id-" + this.id.toString() ) )
				TheCache.cache().remove( "Edit-Id-" + this.id.toString() );
		} catch( Exception e ) {
			
		}
		
		for( AttachedEdit ae : attached() )
			ae.delete();
		
		for( EditClassification ec : classifications() )
			ec.delete();
		
		super.delete();
	}
	
	@SuppressWarnings( "unchecked" )
	@Override
	public void store() {
		super.store();
		try {
			TheCache.cache().put( "Edit-Id-" + this.id.toString(), this );
		} catch( Exception e ) {
			
		}
	}

	@SuppressWarnings( "unchecked" )
	public static Edit findByKey( Key key ) {
		String strKey = KeyFactory.keyToString( key );
		try {
			if( TheCache.cache().containsKey( strKey ) ) {
				Edit obj = (Edit) TheCache.cache().get( strKey );
				if( obj != null )
					return obj;
			}
				
		} catch( Exception e ) {
			
		}
		
		PersistenceManager pm = JDOFilter.getPM();
		Edit edit = null;
		try {
			edit = pm.getObjectById( Edit.class, key );
		} catch( Exception e ) {
			/* Do nothing */
		}
		
		try {
			TheCache.cache().put( strKey, edit );
		} catch( Exception e ) {
			
		}
		return edit;
	}
	
	@SuppressWarnings( "unchecked" )
	public static Edit findById( Integer id ) {
		String strKey = "Edit-Id-" + id.toString();
		try {
			if( TheCache.cache().containsKey( strKey ) ) {
				Edit obj = (Edit) TheCache.cache().get( strKey );
				if( obj != null )
					return obj;
			}
		} catch( Exception e ) {
			
		}
		
		PersistenceManager pm = JDOFilter.getPM();
		Edit edit = null;
		try {
			Query q = pm.newQuery( "SELECT FROM " + Edit.class.getName() + " WHERE id == theId" );
			q.declareParameters( "Integer theId" );
			List< Edit > editList = (List< Edit >) q.execute( id );
			if( editList.size() > 0 )
				edit = editList.get( 0 );
		} catch( Exception e ) {
			
		}
		
		return edit;
	}

	public static Edit findByKey( String key ) {
		return findByKey( KeyFactory.stringToKey( key ) );
	}
	
	public static Edit newFromId( Integer id, Classification classification, Integer required ) {
		Edit edit = findById( id );
		if( edit == null )
			edit = new Edit( id, classification, required );
		return edit;
	}

	public List< String > getComments() {
		List< String > list = new ArrayList< String >();
		for( EditClassification classification : classifications() )
			list.add( classification.getComment() );
		return list;
	}
	
	public List< Key > getUsers() {
		List< Key > list = new ArrayList< Key >();
		for( EditClassification classification : classifications() )
			list.add( classification.getUser().getKey() );
		return list;
	}

	public void newClassification( User user, Classification type, String comment ) {
		EditClassification ec = EditClassification.findByEditUser( this, user );
		if( ec == null )
			new EditClassification( this, type, user, comment );
		else {
			ec.setClassification( type );
			ec.setComment( comment );
		}
		Queue queue = QueueFactory.getQueue( "edit-done-queue" );
		for( AttachedEdit ae : attached() ) {
			ae.addUser( user );
			queue.add(
            		TaskOptions
            		.Builder
            		.param( "ekey", KeyFactory.keyToString( key ) )
            		.param( "egkey", KeyFactory.keyToString( ae.getEditGroup().getKey() ) )
            		.method( Method.GET )
            );
		}
	}

	@SuppressWarnings( "unchecked" )
	public static List< Key > list() {
		List< Key > list = new ArrayList< Key >();
		PersistenceManager pm = JDOFilter.getPM();
		try {
			Query q = pm.newQuery( "SELECT FROM " + Edit.class.getName() );
			List< Edit > editList = (List< Edit >) q.execute();
			for( Edit edit : editList )
				list.add( edit.getKey() );
		} catch( Exception e ) {
			
		}
		
		return list;
	}
}
