/**
 * 
 */
package org.cluenet.cluebot.reviewinterface.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tools.tar.TarConstants;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;
import org.cluenet.cluebot.reviewinterface.shared.Classification;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * @author cobi
 *
 */
public class DownloadImpl extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7646394607043654042L;

	
	private byte[] CSVEscape( String data ) {
		data = "\"" + data.replaceAll( "\"", "\\\"" ) + "\"";
		return data.getBytes();
	}
	
	private byte[] getCSVFromEditList( List< Key > editList ) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		for( Key key : editList ) {
			Edit edit = Edit.findByKey( key );
			bos.write( CSVEscape( KeyFactory.keyToString( edit.getKey() ) ) );
			bos.write( ',' );
			bos.write( CSVEscape( edit.getId().toString() ) );
			bos.write( ',' );
			bos.write( CSVEscape( edit.getKnown().toString() ) );
			bos.write( ',' );
			bos.write( CSVEscape( edit.getVandalism().toString() ) );
			bos.write( ',' );
			bos.write( CSVEscape( edit.getConstructive().toString() ) );
			bos.write( ',' );
			bos.write( CSVEscape( edit.getSkipped().toString() ) );
			bos.write( ',' );
			bos.write( CSVEscape( edit.getRequired().toString() ) );
			bos.write( '\n' );
		}
		
		return bos.toByteArray();
	}
	
	private void processComments( String prefix, TarOutputStream tos, List< Key > edits ) throws IOException {
		TarEntry te;
		for( Key key : edits ) {
			Edit edit = Edit.findByKey( key );
			te = new TarEntry( prefix + edit.getId().toString() );
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			for( String comment : edit.getComments() )
				bos.write( ( comment + "\n" ).getBytes() );
			
			byte[] data = bos.toByteArray();
			
			te.setSize( data.length );
			
			tos.putNextEntry( te );
			tos.write( data );
			tos.closeEntry();
		}
	}
	
	private void processUsers( String prefix, TarOutputStream tos, List< Key > edits ) throws IOException {
		TarEntry te;
		for( Key key : edits ) {
			Edit edit = Edit.findByKey( key );
			te = new TarEntry( prefix + edit.getId().toString() );
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			for( Key ukey : edit.getUsers() )
				bos.write( ( User.findByKey( ukey ).getEmail().getEmail() + "\n" ).getBytes() );
			
			byte[] data = bos.toByteArray();
			
			te.setSize( data.length );
			
			tos.putNextEntry( te );
			tos.write( data );
			tos.closeEntry();
		}
	}
	
	private void processEditGroup( String prefix, TarOutputStream tos, EditGroup eg ) throws IOException {
		byte[] csv;
		TarEntry te;
		String folderName = prefix + eg.getName() + "/";
		te = new TarEntry( folderName, TarConstants.LF_DIR );
		te.setSize( 0 );
		tos.putNextEntry( te );
		tos.closeEntry();
		
		// active.csv
		te = new TarEntry( folderName + "active.csv" );
		csv = getCSVFromEditList( eg.getEdits() );
		te.setSize( csv.length );
		tos.putNextEntry( te );
		tos.write( csv );
		tos.closeEntry();
		
		// done.csv
		te = new TarEntry( folderName + "done.csv" );
		csv = getCSVFromEditList( eg.getDone() );
		te.setSize( csv.length );
		tos.putNextEntry( te );
		tos.write( csv );
		tos.closeEntry();
		
		// comments/
		te = new TarEntry( folderName + "comments/", TarConstants.LF_DIR );
		te.setSize( 0 );
		tos.putNextEntry( te );
		tos.closeEntry();
		
		processComments( folderName + "comments/", tos, eg.getEdits() );
		processComments( folderName + "comments/", tos, eg.getDone() );
		
		
		// users/
		te = new TarEntry( folderName + "users/", TarConstants.LF_DIR );
		te.setSize( 0 );
		tos.putNextEntry( te );
		tos.closeEntry();
		
		processUsers( folderName + "users/", tos, eg.getEdits() );
		processUsers( folderName + "users/", tos, eg.getDone() );
		
	}
	
	private byte[] baseCSV( List< EditGroup > egs ) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		for( EditGroup eg : egs ) {
			bos.write( CSVEscape( KeyFactory.keyToString( eg.getKey() ) ) );
			bos.write( ',' );
			bos.write( CSVEscape( eg.getName() ) );
			bos.write( ',' );
			bos.write( CSVEscape( eg.getWeight().toString() ) );
			bos.write( '\n' );
		}
		
		return bos.toByteArray();
	}

	private void getAsTar( HttpServletResponse resp ) throws IOException {
		String prefix = "cbng-review-" + System.currentTimeMillis();
		resp.setContentType( "application/octet-stream" );
		resp.setHeader( "Content-Disposition", "attachment; filename=\"" + prefix + ".tar.gz\"" );
		ServletOutputStream respOs = resp.getOutputStream();
		GZIPOutputStream gzos = new GZIPOutputStream( respOs );
		TarOutputStream tos = new TarOutputStream( gzos );
		
		prefix += "/";
		
		TarEntry te = new TarEntry( prefix, TarConstants.LF_DIR );
		te.setSize( 0 );
		tos.putNextEntry( te );
		tos.closeEntry();
		
		List< EditGroup > egs = EditGroup.list();
		byte[] csv = baseCSV( egs );
		te = new TarEntry( prefix + "edit_groups.csv" );
		te.setSize( csv.length );
		tos.putNextEntry( te );
		tos.write( csv );
		tos.closeEntry();
		
		for( EditGroup eg : egs )
			processEditGroup( prefix, tos, eg );
		
		tos.close();
		gzos.close();
		respOs.close();
	}
	
	private void getDone( HttpServletResponse resp, String editGroup ) throws IOException {
		PrintWriter pw = resp.getWriter();
		EditGroup eg = EditGroup.findByKey( editGroup );
		if( eg == null ) {
			pw.println( "No such group." );
			return;
		}
		
		for( Key key : eg.getDone() ) {
			Edit edit = Edit.findByKey( key );
			pw.print( edit.getId().toString() + " " );
			
			Classification cls = edit.calculateClassification();
			
			if( cls.equals( Classification.CONSTRUCTIVE ) )
				pw.println( "C" );
			else if( cls.equals( Classification.VANDALISM ) )
				pw.println( "V" );
			else if( cls.equals( Classification.SKIPPED ) )
				pw.println( "S" );
			else
				pw.println( "U" );
		}
	}
	
	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		String format = req.getParameter( "format" );
		if( format == null )
			format = "tar";
		if( format.equals( "done" ) )
			getDone( resp, req.getParameter( "editgroup" ) );
		else
			getAsTar( resp );
	}
}
