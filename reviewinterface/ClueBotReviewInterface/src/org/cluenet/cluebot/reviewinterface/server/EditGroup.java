/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

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


/**
 * @author cobi
 *
 */
@Entity
public class EditGroup extends Persist {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
	@Basic
	private String name;

	@Basic
	private List< Key > edits;
	
	@Basic
	private List< Key > done;
	
	@Basic
	private Integer weight;

	public EditGroup( String name, List< Edit > edits, Integer weight ) {
		this.name = name;
		this.edits = new ArrayList< Key >();
		this.weight = weight;
		this.done = new ArrayList< Key >();
		addEdits( edits );
		this.persist();
	}
	
	public void addEdits( List< Edit > edits ) {
		for( Edit edit : edits )
			if( !this.edits.contains( edit.getKey() ) && !this.done.contains( edit.getKey() ) )
				if( edit.getVandalism() >= edit.getRequired() || edit.getConstructive() >= edit.getRequired() || edit.getSkipped() >= edit.getRequired() )
					this.done.add( edit.getKey() );
				else
					this.edits.add( edit.getKey() );
		this.persist();
	}

	
	public String getName() {
		return name;
	}

	
	public Integer getWeight() {
		if( edits.size() == 0 )
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
	
	public List< Key > getDone() {
		return new ArrayList< Key >( done );
	}
	
	public Edit getRandomEdit( User user ) {
		List< Key > edits = new ArrayList< Key >( this.edits );
		
		if( edits.size() == 0 )
			return null;
		
		Collections.shuffle( edits );
		
		for( Key key : edits ) {
			Edit edit = Edit.findByKey( key );
			if( !edit.getUsers().contains( user.getKey() ) )
				return edit;
		}
		
		return null;
	}

	public void editDone( Edit edit ) {
		this.done.add( edit.getKey() );
		this.edits.remove( edit.getKey() );
		this.persist();
	}
	
	public org.cluenet.cluebot.reviewinterface.shared.EditGroup getClientClass() {
		List< AdminEdit > edits = new ArrayList< AdminEdit >();
		List< AdminEdit > done = new ArrayList< AdminEdit >();
		
		for( Key key : this.edits )
			edits.add( Edit.findByKey( key ).getAdminClass() );
		
		for( Key key : this.done )
			done.add( Edit.findByKey( key ).getAdminClass() );
			
		
		return new org.cluenet.cluebot.reviewinterface.shared.EditGroup(
				KeyFactory.keyToString( key ), name, edits, done, weight
		);
	}
	
	public org.cluenet.cluebot.reviewinterface.shared.EditGroup getLightClientClass() {
		List< AdminEdit > edits = new ArrayList< AdminEdit >();
		List< AdminEdit > done = new ArrayList< AdminEdit >();
		
		return new org.cluenet.cluebot.reviewinterface.shared.EditGroup(
				KeyFactory.keyToString( key ), name, edits, done, weight
		);
	}
	
	public static EditGroup findByKey( Key key ) {
		EntityManager em = EMF.get().createEntityManager();
		EditGroup editGroup = null;
		try {
			editGroup = em.find( EditGroup.class, key );
		} catch( Exception e ) {
			/* Do nothing */
		} finally {
			em.close();
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
			if( eg.edits.contains( edit.getKey() ) )
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
		for( List< Key > list : types )
			for( Key key : list ) {
				Edit edit = Edit.findByKey( key );
				if( EditGroup.findByEdit( edit ).size() == 0 )
					edit.delete();
			}
	}
}
