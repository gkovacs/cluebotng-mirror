<?PHP
	require_once 'includes/header.php';
	
	if( !isset( $_REQUEST[ 'page' ] ) )
		$_REQUEST[ 'page' ] = 'Main';
	
	$page = Page::findByName( $_REQUEST[ 'page' ] );
	
	require_once 'includes/footer.php';
?>