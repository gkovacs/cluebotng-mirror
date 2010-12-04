package org.cluenet.cluebot.reviewinterface.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class StatisticsImpl extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1877819831217413521L;

	@Override
	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
		String report = Statistics.getStats();
		Properties props = new Properties();
        Session session = Session.getDefaultInstance( props, null );

        String msgBody = "Hello,\r\n" +
				report.replaceAll( "\n", "\r\n" ) +
				"\r\n" +
				"Thanks,\r\n" +
				"ClueBot NG Review Interface.";

        try {
			Message msg = new MimeMessage( session );
			msg.setFrom( new InternetAddress( "interface@cluebotreview.appspotmail.com", "ClueBot-NG Review Interface" ) );
			for( User user : User.list() )
				if( user.isAdmin() )
					msg.addRecipient( Message.RecipientType.TO, new InternetAddress( user.getEmail().getEmail(), user.getNick() ) );
			msg.setSubject( "Statistics Report" );
			msg.setText( msgBody );
			Transport.send( msg );
			
			String data = "";
			data += URLEncoder.encode( "action", "UTF-8" ) + "=" + URLEncoder.encode( "edit", "UTF-8" );
			data += "&" + URLEncoder.encode( "title", "UTF-8" ) + "=" + URLEncoder.encode( "User:ClueBot NG/ReviewInterface/Stats", "UTF-8" );
			data += "&" + URLEncoder.encode( "summary", "UTF-8" ) + "=" + URLEncoder.encode( "Uploading Stats", "UTF-8" );
			data += "&" + URLEncoder.encode( "text", "UTF-8" ) + "=" + URLEncoder.encode( report, "UTF-8" );
			data += "&" + URLEncoder.encode( "token", "UTF-8" ) + "=" + URLEncoder.encode( "+\\", "UTF-8" );
			
			URL url = new URL( "http://en.wikipedia.org/w/api.php" );
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput( true );
            conn.setRequestMethod( "POST" );
            
			OutputStreamWriter wr = new OutputStreamWriter( conn.getOutputStream() );
			wr.write( data );
			wr.flush();
			
			BufferedReader rd = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
			String line;
			while( ( line = rd.readLine() ) != null )
				resp.getWriter().println( line );
			wr.close();
			rd.close();
        } catch( Exception e ) {
        	resp.getWriter().println( "<html><body>There was an error.</body></html>" );
        	resp.getWriter().println( e.getMessage() );
        	e.printStackTrace( resp.getWriter() );
        }

	}
	
}
