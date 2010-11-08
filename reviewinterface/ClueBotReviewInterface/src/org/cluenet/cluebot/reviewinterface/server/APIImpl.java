package org.cluenet.cluebot.reviewinterface.server;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

@SuppressWarnings( "serial" )
public class APIImpl extends HttpServlet {
	private Element getEdit( Document doc, String key ) {
		return null;
		
	}
	
	private Element processGetEdit( Document doc, HttpServletRequest req ) {
		return getEdit( doc, req.getParameter( "gekey" ) );
	}
	
	private Element getEditGroup( Document doc, String key ) {
		return null;
		
	}
	
	private Element processGetEditGroup( Document doc, HttpServletRequest req ) {
		return getEditGroup( doc, req.getParameter( "gegKey" ) );
	}
	
	public void doGet( HttpServletRequest req, HttpServletResponse res ) throws IOException {
		
		
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element root = doc.createElement( "API" );
			doc.appendChild( root );
			
			
		} catch( Exception e ) {
			
		}
	}
}
