<?PHP
	include 'editdbMasterCredentials.php';
	include '../bot/cbng.php';

	function error( $str ) {
		file_put_contents( 'php://stderr', $str . "\n" );
		die();
	}

	function getMasterMySQL() {
		global $mysqlmasteruser, $mysqlmasterpass, $mysqlmasterhost;
		
		$mysql = mysql_connect( $mysqlmasterhost, $mysqlmasteruser, $mysqlmasterpass );
		if( !$mysql )
			error( 'MySQL Connection Error: ' . mysql_error() );
		
		if( !mysql_select_db( 'cbng_editdb_master', $mysql ) )
			error( 'MySQL DB Error: ' . mysql_error() );
		
		return $mysql;
	}

	function insertEdit( $id, $isVand, $isActive, $reviewers, $reviewers_agreeing, $source ) {
		$mysql = getMasterMySQL();
		
		$data = oldData( $id );
		
		$query = 'INSERT INTO `editset` (
				`edittype`,
				`editid`,
				`comment`,
				`user`,
				`user_edit_count`,
				`user_distinct_pages`,
				`user_warns`,
				`prev_user`,
				`user_reg_time`,
				`common_page_made_time`,
				`common_title`,
				`common_namespace`,
				`common_creator`,
				`common_num_recent_edits`,
				`common_num_recent_reversions`,
				`current_minor`,
				`current_timestamp`,
				`current_text`,
				`previous_timestamp`,
				`previous_text`,
				`isvandalism`,
				`isactive`,
				`source`,
				`reviewers`,
				`reviewers_agreeing`
			) VALUES (';
		
		$escaped = Array(
			$data[ 'EditType' ],
			$data[ 'EditID' ],
			$data[ 'comment' ],
			$data[ 'user' ],
			$data[ 'user_edit_count' ],
			$data[ 'user_distinct_pages' ],
			$data[ 'user_warns' ],
			$data[ 'prev_user' ],
			$data[ 'user_reg_time' ],
			$data[ 'common' ][ 'page_made_time' ],
			$data[ 'common' ][ 'title' ],
			$data[ 'common' ][ 'namespace' ],
			$data[ 'common' ][ 'creator' ],
			$data[ 'common' ][ 'num_recent_edits' ],
			$data[ 'common' ][ 'num_recent_reversions' ],
			$data[ 'current' ][ 'minor' ] == 'true' ? 1 : 0,
			$data[ 'current' ][ 'timestamp' ],
			$data[ 'current' ][ 'text' ],
			$data[ 'previous' ][ 'timestamp' ],
			$data[ 'previous' ][ 'text' ],
			$isVandal ? 1 : 0,
			$isActive ? 1 : 0,
			$source,
			$reviewers,
			$reviewers_agreeing
		);
		
		foreach( $escaped as $key => &$value )
			if( $key == 8 or $key == 9 or $key == 16 or $key == 18 )
				$value = 'FROM_UNIXTIME( \'' . mysql_real_escape_string( $value ) . '\' )';
			else
				$value = '\'' . mysql_real_escape_string( $value ) . '\'';
		
		$query .= implode( ',', $escaped );
		$query .= ' )';
		
		return mysql_query( $query );
	}
?>