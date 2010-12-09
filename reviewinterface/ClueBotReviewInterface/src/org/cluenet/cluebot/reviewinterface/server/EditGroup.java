package org.cluenet.cluebot.reviewinterface.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cluenet.cluebot.reviewinterface.shared.Status;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

@PersistenceCapable(detachable="true")
public class EditGroup extends Persist {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2554029973803187193L;

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private String name;
	
	@Persistent
	private Integer weight;
	
	
	public EditGroup( String name, Integer weight ) {
		this.name = name;
		this.weight = weight;
		this.store();
	}


	@Override
	public Key getKey() {
		return key;
	}


	public static EditGroup findByKey( String key ) {
		return findByKey( KeyFactory.stringToKey( key ) );
	}


	public String getName() {
		return name;
	}


	private List< AttachedEdit > getEditsByStatus( Integer start, Integer count, Status status ) {
		return AttachedEdit.getByStatus( this, start, count, status );
	}
	
	public List< Key > getEdits() {
		return AttachedEdit.getEditKeyByStatus( this, Status.NOTDONE );
	}
	
	public List< AttachedEdit > getEdits( Integer start, Integer count ) {
		return getEditsByStatus( start, count, Status.NOTDONE );
	}

	public List< Key > getReviewed() {
		return AttachedEdit.getEditKeyByStatus( this, Status.PARTIAL );
	}
	
	public List< AttachedEdit > getReviewed( Integer start, Integer count ) {
		return getEditsByStatus( start, count, Status.PARTIAL );
	}

	public List< Key > getDone() {
		return AttachedEdit.getEditKeyByStatus( this, Status.DONE );
	}
	
	public List< AttachedEdit > getDone( Integer start, Integer count ) {
		return getEditsByStatus( start, count, Status.DONE );
	}


	@SuppressWarnings( "unchecked" )
	public static List< EditGroup > list() {
		PersistenceManager pm = JDOFilter.getPM();
		Query q = pm.newQuery( "SELECT FROM " + EditGroup.class.getName() );
		return new ArrayList< EditGroup >( (List< EditGroup >) q.execute() );
	}


	public Integer getWeight() {
		return weight;
	}


	public org.cluenet.cluebot.reviewinterface.shared.EditGroup getClientClass( Integer editStart, Integer editCount, Integer reviewStart, Integer reviewCount, Integer doneStart, Integer doneCount ) {
		List< org.cluenet.cluebot.reviewinterface.shared.Edit > edits = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.Edit >();
		List< org.cluenet.cluebot.reviewinterface.shared.Edit > reviewed = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.Edit >();
		List< org.cluenet.cluebot.reviewinterface.shared.Edit > done = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.Edit >();
		
		for( AttachedEdit ae : getEdits( editStart, editCount ) )
			edits.add( ae.getEdit().getAdminClass() );
		
		for( AttachedEdit ae : getReviewed( reviewStart, reviewCount ) )
			reviewed.add( ae.getEdit().getAdminClass() );
		
		for( AttachedEdit ae : getDone( doneStart, doneCount ) )
			done.add( ae.getEdit().getAdminClass() );
		
		return new org.cluenet.cluebot.reviewinterface.shared.EditGroup(
				KeyFactory.keyToString( key ),
				name,
				edits,
				reviewed,
				done,
				weight,
				AttachedEdit.countByStatus( this, Status.DONE ),
				AttachedEdit.countByStatus( this, Status.PARTIAL ),
				AttachedEdit.countByStatus( this, Status.NOTDONE )
		);
	}


	public org.cluenet.cluebot.reviewinterface.shared.EditGroup getLightClientClass() {
		List< org.cluenet.cluebot.reviewinterface.shared.Edit > edits = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.Edit >();
		List< org.cluenet.cluebot.reviewinterface.shared.Edit > reviewed = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.Edit >();
		List< org.cluenet.cluebot.reviewinterface.shared.Edit > done = new ArrayList< org.cluenet.cluebot.reviewinterface.shared.Edit >();
		
		return new org.cluenet.cluebot.reviewinterface.shared.EditGroup(
				KeyFactory.keyToString( key ),
				name,
				edits,
				reviewed,
				done,
				weight,
				AttachedEdit.countByStatus( this, Status.DONE ),
				AttachedEdit.countByStatus( this, Status.PARTIAL ),
				AttachedEdit.countByStatus( this, Status.NOTDONE )
		);
	}


	public void addEdits( List< Edit > edits ) {
		for( Edit edit : edits )
			if( AttachedEdit.findByEditGroupAndEdit( this, edit ) == null )
				new AttachedEdit( this, edit, Status.NOTDONE );
	}


	public void updateEditState( Edit edit ) {
		AttachedEdit ae = AttachedEdit.findByEditGroupAndEdit( this, edit );
		ae.updateEditState();
	}


	public static List< EditGroup > findByEdit( Edit edit ) {
		List< EditGroup > list = new ArrayList< EditGroup >();
		for( AttachedEdit ae : AttachedEdit.findByEdit( edit ) )
			list.add( ae.getEditGroup() );
		return list;
	}
	
	public Boolean isDone() {
		return !AttachedEdit.hasWaitingEditsForEditGroup( this );
	}


	public static EditGroup getRandomEditGroup() {
		List< EditGroup > editGroups = new ArrayList< EditGroup >();
		for( EditGroup eg : list() )
			if( !eg.isDone() )
				editGroups.add( eg );
		
		if( editGroups.size() == 0 )
			return null;
		
		Integer sum = 0;
		for( EditGroup eg : editGroups )
			sum += eg.getWeight();
		
		if( editGroups.size() == 0 )
			return null;
		
		Integer num = new Random().nextInt( sum );
		for( EditGroup eg : editGroups ) {
			num -= eg.getWeight();
			if( num <= 0 )
				return eg;
		}
		
		return null;
	}


	public Edit getRandomEdit( User user ) {
		AttachedEdit ae = AttachedEdit.getRandomByEditGroupUser( this, user );
		if( ae == null )
			return null;
		return ae.getEdit();
	}

	
	@SuppressWarnings( "unchecked" )
	public static EditGroup findByKey( Key key ) {
		String strKey = KeyFactory.keyToString( key );
		try {
			if( TheCache.cache().containsKey( strKey ) ) {
				EditGroup obj = (EditGroup) TheCache.cache().get( strKey );
				if( obj != null )
					return obj;
			}
		} catch( Exception e ) {
			
		}
		
		PersistenceManager pm = JDOFilter.getPM();
		EditGroup editGroup = null;
		try {
			editGroup = pm.getObjectById( EditGroup.class, key );
		} catch( Exception e ) {
			e.printStackTrace( System.err );
		}
		
		try {
			TheCache.cache().put( strKey, editGroup );
		} catch( Exception e ) {
			
		}
		return editGroup;
	}
	
	@Override
	public void delete() {
		Key egKey = this.key;
		List< List< Key > > types = new ArrayList< List< Key > >();
		types.add( new ArrayList< Key >( getEdits() ) );
		types.add( new ArrayList< Key >( getReviewed() ) );
		types.add( new ArrayList< Key >( getDone() ) );
		super.delete();
		for( List< Key > list : types ) {
			Queue queue = QueueFactory.getQueue( "garbage-collection-queue" );
			if( list.size() == 0 )
				continue;
			for( int i = 0 ; i <= list.size() / 100 ; i++ ) {
				int fromIndex = 100 * i;
				int toIndex = fromIndex + 100;
				if( toIndex >= list.size() )
					toIndex = list.size();
				if( fromIndex > toIndex )
					continue;
				List< TaskOptions > tasks = new ArrayList< TaskOptions >();
				for( Key key : list.subList( fromIndex, toIndex ) )
					tasks.add( TaskOptions.Builder.param( "key", KeyFactory.keyToString( key ) ).param( "egkey", KeyFactory.keyToString( egKey ) ).method( Method.GET ) );
				queue.add( tasks );
			}
		}
	}
}
