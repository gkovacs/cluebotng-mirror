<?PHP
	require_once 'includes/header.php';
	
	$result = mysql_query( 'SELECT `new_id` FROM `reports` JOIN `vandalism` ON `revertid` = `id` WHERE `status` = 2' );
	
	$ids = Array();
	
	while( $row = mysql_fetch_assoc( $result ) )
		$ids[] = $row[ 'new_id' ];
	
	echo implode( "\n", $ids );
?>