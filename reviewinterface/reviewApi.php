<?PHP
	function normalizeReviewAPIInput( $data ) {
		if( !is_array( $data ) )
			$data = Array( 'list' => $data );
		if( isset( $data[ 'list' ] ) )
			if( !is_array( $data[ 'list' ] ) )
				$data[ 'list' ] = Array( $data[ 'list' ] );
		if( isset( $data[ 'get' ] ) )
			if( !is_array( $data[ 'get' ] ) )
				unset( $data[ 'get' ] );
			else
				foreach( $data[ 'get' ] as &$value )
					if( !is_array( $value ) )
						$value = Array( $value );
		return $data;
	}
	
	function callDataToURL( $data ) {
		$parts = Array();
		
		if( isset( $data[ 'list' ] ) ) {
			if( in_array( 'users', $data[ 'list' ] ) )
				$parts[] = 'listUsers';
			if( in_array( 'editgroups', $data[ 'list' ] ) )
				$parts[] = 'listEditGroups';
		}
		
		if( isset( $data[ 'get' ] ) ) {
			if( isset( $data[ 'get' ][ 'user' ] ) ) {
				$parts[] = 'getUser';
				$parts[] = 'guKeys=' . urlencode( implode( ':', $data[ 'get' ][ 'user' ] ) );
			}
			if( isset( $data[ 'get' ][ 'edit' ] ) ) {
				$parts[] = 'getEdit';
				$parts[] = 'geKeys=' . urlencode( implode( ':', $data[ 'get' ][ 'edit' ] ) );
			}
			if( isset( $data[ 'get' ][ 'editgroup' ] ) ) {
				$parts[] = 'getEditGroup';
				$parts[] = 'gegKeys=' . urlencode( implode( ':', $data[ 'get' ][ 'editgroup' ] ) );
			}
		}
		
		$contextArray = Array(
			'http' => Array(
				'method'  => 'POST',
				'content' => implode( '&', $parts ),
				'header'  => 'Content-Type: application/x-www-form-urlencoded'
			)
		);

		$context = stream_context_create( $contextArray );
		$url = 'http://cluebotreview.g.cluenet.org/api';
		return Array( $url, $context, $contextArray );
	}
	
	function callReviewAPI( $data ) {
		$data = normalizeReviewAPIInput( $data );
		list( $url, $context, $contextData ) = callDataToURL( $data );
		$xml = file_get_contents( $url, false, $context );
		return simplexml_load_string( $xml );
	}
?>
