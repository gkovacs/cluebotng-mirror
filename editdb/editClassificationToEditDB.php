<?PHP
	include 'editdbMasterFunctions.php';
	
	if( $argc == 1 ) {
		echo 'You must include a source name for these edits.' . "\n";
		exit( 1 );
	}
	
	$source = $argv[ 1 ];
	
	$in = fopen( 'php://input', 'r' );
	while( !feof( $in ) ) {
		$line = trim( fgets( $in, 512 ) );
		
		list( $id, $type ) = explode( ' ', $line, 2 );
		
		switch( $type[ 0 ] ) {
			case 'V': list( $isVand, $isActive ) = Array( 1, 1 ); break;
			case 'S':
			case 'U': list( $isVand, $isActive ) = Array( 0, 0 ); break;
			case 'C': list( $isVand, $isActive ) = Array( 0, 1 ); break;
		}
		
		echo 'Inserting ' . $id . ' ...';
		$ret = insertEdit( $id, $isVand, $isActive, 0, 0, $source );
		if( $ret )
			echo ' Done.' . "\n";
		else
			echo ' Failed.' . "\n";
	}
	fclose( $in );
?>