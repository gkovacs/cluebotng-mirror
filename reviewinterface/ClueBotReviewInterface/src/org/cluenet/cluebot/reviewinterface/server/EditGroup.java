/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Query;

import org.cluenet.cluebot.reviewinterface.shared.AdminEdit;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;


/**
 * @author cobi
 *
 */
@Entity
public class EditGroup extends Persist implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8009102135611412035L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
	@Basic
	private String name;

	@Basic
	private List< Key > edits = null;
	
	@Basic
	private List< Key > reviewed = null;
	
	@Basic
	private List< Key > done = null;
	
	@Basic
	private Integer weight;

	public EditGroup( String name, List< Edit > edits, Integer weight ) {
		this.name = name;
		this.edits = new ArrayList< Key >();
		this.reviewed = new ArrayList< Key >();
		this.weight = weight;
		this.done = new ArrayList< Key >();
		addEdits( edits );
		this.store();
	}
	
	public void addEdits( List< Edit > edits ) {
		for( Edit edit : edits )
			if( !this.edits.contains( edit.getKey() ) && !this.done.contains( edit.getKey() ) )
				if( edit.getVandalism() >= edit.getRequired() || edit.getConstructive() >= edit.getRequired() || edit.getSkipped() >= edit.getRequired() )
					this.done.add( edit.getKey() );
				else
					this.edits.add( edit.getKey() );
		this.merge();
	}

	
	public String getName() {
		return name;
	}

	
	public Integer getWeight() {
		checkNulls();
		
		if( edits.size() == 0 && reviewed.size() == 0 )
			return 0;
		return weight;
	}

	@Override
	public Key getKey() {
		return this.key;
	}
	
	public List< Key > getEdits() {
		return new ArrayList< Key >( edits );
	}
	
	public List< Key > getReviewed() {
		return new ArrayList< Key >( reviewed );
	}
	
	public List< Key > getDone() {
		return new ArrayList< Key >( done );
	}
	
	public Edit getRandomEdit( User user ) {
		checkNulls();
		
		List< Key > edits = new ArrayList< Key >( this.edits );
		List< Key > reviewed = new ArrayList< Key >( this.reviewed );
		
		if( edits.size() == 0 && reviewed.size() == 0 )
			return null;
		
		Collections.shuffle( reviewed );
		
		for( Key key : reviewed ) {
			Edit edit = Edit.findByKey( key );
			if( !edit.getUsers().contains( user.getKey() ) )
				return edit;
		}
		
		Collections.shuffle( edits );
		
		for( Key key : edits ) {
			Edit edit = Edit.findByKey( key );
			if( !edit.getUsers().contains( user.getKey() ) )
				return edit;
		}
		
		return null;
	}
	
	public void updateEditState( Edit edit ) {
		checkNulls();
		
		edits.remove( edit.getKey() );
		reviewed.remove( edit.getKey() );
		done.remove( edit.getKey() );
		
		if( edit.getConstructive() >= edit.getRequired() || edit.getSkipped() >= edit.getRequired() || edit.getVandalism() >= edit.getRequired() )
			done.add( edit.getKey() );
		else if( edit.getConstructive() > 0 || edit.getVandalism() > 0 || edit.getSkipped() > 0 )
			reviewed.add( edit.getKey() );
		else
			edits.add( edit.getKey() );
		
		this.merge();
	}
	
	private void checkNulls() {
		if( edits == null )
			edits = new ArrayList< Key >();
		if( reviewed == null )
			reviewed = new ArrayList< Key >();
		if( done == null )
			done = new ArrayList< Key >();
	}

	public org.cluenet.cluebot.reviewinterface.shared.EditGroup getClientClass( Integer editStart, Integer editEnd, Integer reviewStart, Integer reviewEnd, Integer doneStart, Integer doneEnd ) {
		checkNulls();
		List< AdminEdit > edits = new ArrayList< AdminEdit >();
		List< AdminEdit > reviewed = new ArrayList< AdminEdit >();
		List< AdminEdit > done = new ArrayList< AdminEdit >();
		
		try {
			if( editEnd > this.edits.size() )
				editEnd = this.edits.size();
			if( editStart > editEnd || editStart >= this.edits.size() )
				editEnd = editStart = 0;
			if( editStart < 0 )
				editStart = 0;
			
			for( Key key : this.edits.subList( editStart, editEnd ) )
				edits.add( Edit.findByKey( key ).getAdminClass() );
		} catch( Exception e ) {
			/* Do nothing. */
		}
		
		try {
			if( reviewEnd > this.reviewed.size() )
				reviewEnd = this.reviewed.size();
			if( reviewStart > reviewEnd || reviewStart >= this.reviewed.size() )
				reviewEnd = reviewStart = 0;
			if( reviewStart < 0 )
				reviewStart = 0;
			
			for( Key key : this.reviewed.subList( reviewStart, reviewEnd ) )
				reviewed.add( Edit.findByKey( key ).getAdminClass() );
		} catch( Exception e ) {
			/* Do nothing. */
		}
		
		try {
			if( doneEnd > this.done.size() )
				doneEnd = this.done.size();
			if( doneStart > doneEnd || doneStart >= this.done.size() )
				doneEnd = doneStart = 0;
			if( doneStart < 0 )
				doneStart = 0;
			
			for( Key key : this.done.subList( doneStart, doneEnd ) )
				done.add( Edit.findByKey( key ).getAdminClass() );
		} catch( Exception e ) {
			/* Do nothing. */
		}
		
		return new org.cluenet.cluebot.reviewinterface.shared.EditGroup(
				KeyFactory.keyToString( key ), name, edits, reviewed, done, weight, this.done.size(), this.reviewed.size(), this.edits.size()
		);
	}
	
	public org.cluenet.cluebot.reviewinterface.shared.EditGroup getLightClientClass() {
		checkNulls();
		List< AdminEdit > edits = new ArrayList< AdminEdit >();
		List< AdminEdit > reviewed = new ArrayList< AdminEdit >();
		List< AdminEdit > done = new ArrayList< AdminEdit >();
		
		return new org.cluenet.cluebot.reviewinterface.shared.EditGroup(
				KeyFactory.keyToString( key ), name, edits, reviewed, done, weight, this.done.size(), this.reviewed.size(), this.edits.size()
		);
	}
	
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
		
		EntityManager em = EMF.get().createEntityManager();
		EditGroup editGroup = null;
		try {
			editGroup = em.find( EditGroup.class, key );
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
		}
		
		try {
			TheCache.cache().put( strKey, editGroup );
		} catch( Exception e ) {
			
		}
		return editGroup;
	}
	
	public static EditGroup findByKey( String key ) {
		return findByKey( KeyFactory.stringToKey( key ) );
	}

	@SuppressWarnings( "unchecked" )
	public static List< EditGroup > list() {
		EntityManager em = EMF.get().createEntityManager();
		List< EditGroup > list = new ArrayList< EditGroup >();
		try {
			Query query = em.createQuery( "select from " + EditGroup.class.getName() );
			list = new ArrayList< EditGroup >( (List< EditGroup >) query.getResultList() );
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
		}
		return list;
	}
	
	public static List< EditGroup > findByEdit( Edit edit ) {
		List< EditGroup > list = new ArrayList< EditGroup >();
		for( EditGroup eg : list() )
			if( eg.edits.contains( edit.getKey() ) || eg.reviewed.contains( edit.getKey() ) || eg.done.contains( edit.getKey() ) )
				list.add( eg );
		return list;
	}
	
	public static EditGroup getRandomEditGroup() {
		List< EditGroup > editGroups = list();
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


	@Override
	public void delete() {
		super.delete();
		List< List< Key > > types = new ArrayList< List< Key > >();
		types.add( edits );
		types.add( done );
		for( List< Key > list : types ) {
			Queue queue = QueueFactory.getQueue( "garbage-collection-queue" );
			if( list.size() == 0 )
				continue;
			for( int i = 0 ; i <= list.size() / 100 ; i++ ) {
				int fromIndex = 100 * i;
				int toIndex = fromIndex + 99;
				if( toIndex >= list.size() )
					toIndex = list.size() - 1;
				if( toIndex > fromIndex )
					continue;
				List< TaskOptions > tasks = new ArrayList< TaskOptions >();
				for( Key key : list.subList( fromIndex, toIndex ) )
					tasks.add( TaskOptions.Builder.param( "key", KeyFactory.keyToString( key ) ).method( Method.GET ) );
				queue.add( tasks );
			}
		}
	}
}
