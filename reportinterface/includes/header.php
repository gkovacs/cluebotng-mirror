<?PHP
	session_start();
	
	require_once 'includes/Page.php';
	require_once 'includes/settings.php';
	require_once 'includes/dbFunctions.php';
	
	foreach( glob( 'pages/*.page.php' ) as $page )
		require_once $page;
	
	$mysql = mysql_connect( $dbHost, $dbUser, $dbPass );
	if( !$mysql )
		die( 'Error.  Could not connect to database.' );
	
	if( !mysql_select_db( $dbSchema ) )
		die( 'Error.  Database has insufficient permissions.' );
	
	date_default_timezone_set( 'America/New_York' );
	
	ini_set( 'user_agent', 'ClueBot/2.0 (ClueBot NG Report Interface)' );
?>