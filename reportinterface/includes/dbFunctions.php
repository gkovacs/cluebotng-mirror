<?PHP
	$statuses = Array(
		'Reported',
		'Invalid',
		'Deferred to Review Interface',
		'Bug',
		'Resolved'
	);
	
	function statusIdToName( $status ) {
		global $statuses;
		return $statuses[ $status ];
	}
	
	function statusNameToId( $status ) {
		global $statuses;
		return array_search( $status, $statuses );
	}

	function createReport( $id, $user ) {
		if( isset( $_SESSION[ 'userid' ] ) ) {
			$userid = $_SESSION[ 'userid' ];
			$user = $_SESSION[ 'username' ];
		} else
			$userid = -1;
		
		$query = 'INSERT INTO `reports` (`revertid`,`reporterid`,`reporter`,`status`) VALUES (';
		$query.= '\'' . mysql_real_escape_string( $id ) . '\',';
		$query.= '\'' . mysql_real_escape_string( $userid ) . '\',';
		$query.= '\'' . mysql_real_escape_string( $user ) . '\',';
		$query.= '0';
		$query.= ')';
		
		mysql_query( $query );
		
		rc( '[[report:' . $id . ']] new http://' . $_SERVER[ 'HTTP_HOST' ] . $_SERVER[ 'PHP_SELF' ] . '?page=View&id=' . $id . ' * ' . $user . ' * New Report' );
	}
	
	function createComment( $id, $user, $comment, $forceUser = false ) {
		if( !$forceUser ) {
			if( isset( $_SESSION[ 'userid' ] ) ) {
				$userid = $_SESSION[ 'userid' ];
				$user = $_SESSION[ 'username' ];
			} else
				$userid = -1;
		} else
			$userid = -2;
		
		$query = 'INSERT INTO `comments` (`revertid`,`userid`,`user`,`comment`) VALUES (';
		$query.= '\'' . mysql_real_escape_string( $id ) . '\',';
		$query.= '\'' . mysql_real_escape_string( $userid ) . '\',';
		$query.= '\'' . mysql_real_escape_string( $user ) . '\',';
		$query.= '\'' . mysql_real_escape_string( $comment ) . '\'';
		$query.= ')';
		
		mysql_query( $query );
		
		rc( '[[report:' . $id . ']] comment http://' . $_SERVER[ 'HTTP_HOST' ] . $_SERVER[ 'PHP_SELF' ] . '?page=View&id=' . $id . ' * ' . $user . ' * ' . $comment );
	}
	
	function getReport( $id ) {
		$id = '\'' . mysql_real_escape_string( $id ) . '\'';
		$result = mysql_query(
			'SELECT `revertid`, UNIX_TIMESTAMP(`timestamp`) AS `time`, `reporterid`, `reporter`, `status`
			FROM `reports`
			WHERE `revertid` = ' . $id
		);
		
		if( mysql_num_rows( $result ) == 0 )
			return null;
		
		$reportData = mysql_fetch_assoc( $result );
		
		$data = Array(
			'id' => $reportData[ 'revertid' ],
			'timestamp' => $reportData[ 'time' ],
			'anonymous' => $reportData[ 'reporterid' ] == -1 ? true : false,
			'username' => $reportData[ 'reporter' ],
			'status' => statusIdToName( $reportData[ 'status' ] ),
			'comments' => Array()
		);
		
		$result = mysql_query(
			'SELECT `commentid`, UNIX_TIMESTAMP( `timestamp` ) AS `time`, `userid`, `user`, `comment`
			FROM `comments`
			WHERE `revertid` = ' . $id
		);
		
		while( $row = mysql_fetch_assoc( $result ) )
			$data[ 'comments' ][] = Array(
				'id' => $row[ 'commentid' ],
				'timestamp' => $row[ 'time' ],
				'userid' => $row[ 'userid' ],
				'anonymous' => $row[ 'userid' ] == -1 ? true : false,
				'username' => $row[ 'user' ],
				'comment' => $row[ 'comment' ]
			);
		
		foreach( $data[ 'comments' ] as &$comment )
			if( $comment[ 'userid' ] != -1 ) {
				$row = mysql_fetch_assoc( mysql_query( 'SELECT `admin`, `superadmin` FROM `users` WHERE `userid` = ' . $comment[ 'userid' ] ) );
				if( $row and $row[ 'admin' ] == 1 )
					$comment[ 'admin' ] = true;
				else
					$comment[ 'admin' ] = false;
				if( $row and $row[ 'superadmin' ] == 1 )
					$comment[ 'sadmin' ] = true;
				else
					$comment[ 'sadmin' ] = false;
			} else {
				$comment[ 'admin' ] = false;
				$comment[ 'sadmin' ] = false;
			}
		
		return $data;
	}
	
	function isAdmin() {
		if( isSAdmin() )
			return true;
		if( !isset( $_SESSION[ 'admin' ] ) )
			return false;
		if( $_SESSION[ 'admin' ] === true )
			return true;
		return false;
	}
	
	function isSAdmin() {
		if( !isset( $_SESSION[ 'sadmin' ] ) )
			return false;
		if( $_SESSION[ 'sadmin' ] === true )
			return true;
		return false;
	}
	
	function rc( $line ) {
		global $rchost, $rcport;
		$rc = fsockopen( 'udp://' . $rchost, $rcport );
		$line = str_replace( Array( "\r", "\n" ), Array( '', '/' ), $line );
		if( strlen( $line ) > 400 )
			$line = substr( $line, 0, 394 ) . ' [...]';
		fwrite( $rc, $line );
		fclose( $rc );
	}
?>