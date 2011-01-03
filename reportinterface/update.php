<?PHP
	require_once 'includes/header.php';
	
	function getEditIdsData( $ids ) {
		$data = Array();
		foreach( array_chunk( $ids, 10 ) as $list ) {
			$xml = file_get_contents( 'http://review.cluebot.cluenet.org/api?getEdit&geIds=' . urlencode( implode( ':', $list ) ) );
			$parsed = simplexml_load_string( $xml );
			foreach( $parsed->GetEdit->Edit as $edit )
				if( isset( $edit->ID ) )
					$data[ (int) $edit->ID ] = Array(
						'status' => (string) $edit->Status,
						'class' => (string) $edit->NewClassification
					);
			foreach( $list as $id )
				if( !isset( $data[ $id ] ) )
					$data[ $id ] = Array(
						'status' => 'NOTFOUND',
						'class' => 'UNKNOWN'
					);
		}
		return $data;
	}
	
	function reviewStatusToReportStatus( $status ) {
		switch( $status[ 'status' ] ) {
			case 'NOTFOUND':
				return 2;
			case 'NOTDONE':
				return 5;
			case 'PARTIAL':
				return 6;
			case 'DONE':
				switch( $status[ 'class' ] ) {
					case 'CONSTRUCTIVE':
						return 7;
					case 'VANDALISM':
						return 8;
					case 'SKIPPED':
						return 9;
				}
		}
		return 3;
	}
	
	function updateStatuses( $ids ) {
		$reportIds = array_flip( $ids );
		foreach( getEditIdsData( $ids ) as $id => $status )
			updateStatusIfIncorrect( $reportIds[ $id ], reviewStatusToReportStatus( $status ), 'Review Interface' );
	}
	
	$result = mysql_query( 'SELECT `revertid`, `new_id` FROM `reports` JOIN `vandalism` ON `revertid` = `id` WHERE `status` = 2 OR `status` = 5 OR `status` = 6' );
	if( !$result )
		die( mysql_error() );
	$ids = Array();
	while( $row = mysql_fetch_assoc( $result ) )
		$ids[ $row[ 'revertid' ] ] = $row[ 'new_id' ];
	updateStatuses( $ids );
?>
