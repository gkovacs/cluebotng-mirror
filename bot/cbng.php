<?PHP
	/* cbng.php, GPL'd, by Jacobi Carter. */
	
	$convert = Array
		(
			'main'		=> 0,	'talk'		=> 1,
			'user'		=> 2,	'user talk'	=> 3,
			'wikipedia'	=> 4,	'wikipedia talk'=> 5,
			'file'		=> 6,	'file talk'	=> 7,
			'mediawiki'	=> 8,	'mediawiki talk'=> 9,
			'template'	=> 10,	'template talk'	=> 11,
			'help'		=> 12,	'help talk'	=> 13,
			'category'	=> 14,	'category talk'	=> 15,
			'portal'	=> 100,	'portal talk'	=> 101
		);
	
	function namespace2id( $ns ) {
		global $convert;
		return $convert[ strtolower( str_replace( '_', ' ', $ns ) ) ];
	}
	
	function namespace2name( $nsid ) {
		global $convert;
		$convertFlipped = array_flip( $convert );
		return ucfirst( $convertFlipped[ $nsid ] );
	}
	
	function parseFeed( $feed ) {
		if(
			preg_match(
				'/^\[\[((Talk|User|Wikipedia|File|MediaWiki|Template|Help|Category|Portal|Special)(( |_)talk)?:)?([^\x5d]*)\]\] (\S*) (http:\/\/en\.wikipedia\.org\/w\/index\.php\?diff=(\d*)&oldid=(\d*).*|http:\/\/en\.wikipedia\.org\/wiki\/\S+)? \* ([^*]*) \* (\(([^)]*)\))? (.*)$/S',
				$feed,
				$m
			)
		) {
			$change = Array(
				'namespace' => $m[ 1 ] ? $m[ 1 ] : 'Main:' ,
				'namespaceid' => namespace2id( $m[ 1 ] ? substr( $m[ 1 ], 0, -1 ) : 'Main' ),
				'title' => $m[ 5 ],
				'flags' => $m[ 6 ],
				'url' => $m[ 7 ],
				'revid' => $m[ 8 ],
				'old_revid' => $m[ 9 ],
				'user' => $m[ 10 ],
				'length' => $m[ 12 ],
				'comment' => $m[ 13 ],
				'timestamp' => time()
			);
			
			return $change;
		}
		return false;
	}
	
	function setupUrlFetch( $url ) {
		$ch = curl_init();
		curl_setopt( $ch, CURLOPT_USERAGENT, 'ClueBot/2.0' );
		if( isset( $proxyhost ) and isset( $proxyport ) and $proxyport != null and $proxyhost != null ) {
			curl_setopt( $ch, CURLOPT_PROXYTYPE, isset( $proxytype ) ? $proxytype : CURLPROXY_HTTP );
			curl_setopt( $ch, CURLOPT_PROXY, $proxyhost );
			curl_setopt( $ch, CURLOPT_PROXYPORT, $proxyport );
		}
		curl_setopt( $ch, CURLOPT_URL, $url );
		curl_setopt( $ch, CURLOPT_FOLLOWLOCATION, 1 );
		curl_setopt( $ch, CURLOPT_MAXREDIRS, 10 );
		curl_setopt( $ch, CURLOPT_HEADER, 0 );
		curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1 );
		curl_setopt( $ch, CURLOPT_TIMEOUT, 120 );
		curl_setopt( $ch, CURLOPT_CONNECTTIMEOUT, 10 );
		curl_setopt( $ch, CURLOPT_HTTPGET, 1 );
		return $ch;
	}
	
	function getUrlsInParallel( $urls ) {
		$mh = curl_multi_init();
		$chs = Array();
		foreach( $urls as $url ) {
			$ch = setupUrlFetch( $url );
			curl_multi_add_handle( $mh, $ch );
			$chs[] = $ch;
		}
		
		$running = null;
		curl_multi_exec( $mh, $running );
		while( $running > 0 ) {
			curl_multi_select( $mh );
			curl_multi_exec( $mh, $running );
		}
		
		$ret = Array();
		foreach( $chs as $ch ) {
			$ret[] = unserialize( curl_multi_getcontent( $ch ) );
			curl_multi_remove_handle( $mh, $ch );
		}
		
		curl_multi_close( $mh );
		
		return $ret;
	}
	
	function xmlizePart( $doc, $key, $data ) {
		$element = $doc->createElement( $key );
		if( is_array( $data ) )
			foreach( $data as $arrKey => $value )
				$element->appendChild( xmlizePart( $doc, $arrKey, $value ) );
		else
			$element->appendChild( $doc->createTextNode( $data ) );
		return $element;
	}
	
	function xmlize( $data ) {
		$doc = new DOMDocument( '1.0' );
		
		$root = $doc->createElement( 'WPEditSet' );
		$doc->appendChild( $root );
		
		if( isset( $data[ 0 ] ) )
			foreach( $data as $entry )
				$root->appendChild( xmlizePart( $doc, 'WPEdit', $entry ) );
		else
			$root->appendChild( xmlizePart( $doc, 'WPEdit', $data ) );
		return $doc->saveXML();
	}
	
	function parseAllFeed( $feed ) {
		$feedData = parseFeed( $feed );
		return parseFeedData( $feedData );
	}
	
	function genOldFeedData( $id ) {
		/* namespace, namespaceid, title, flags, url, revid, old_revid, user, length, comment, timestamp */
		ini_set( 'user_agent', 'ClueBot/2.0 (Training EditDB Scraper)' );
		$data = unserialize( file_get_contents( 'http://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=timestamp|user|comment&format=php&revids=' . urlencode( $id ) ) );
		$data = current( $data[ 'query' ][ 'pages' ] );
		$change = Array(
			'namespace' => namespace2name( $data[ 'ns' ] ),
			'namespaceid' => $data[ 'ns' ],
			'title' => str_replace( namespace2name( $data[ 'ns' ] ) . ':', '', $data[ 'title' ] ),
			'flags' => '',
			'url' => '',
			'revid' => $id,
			'old_revid' => '',
			'user' => $data[ 'revisions' ][ 0 ][ 'user' ],
			'length' => '',
			'comment' => $data[ 'revisions' ][ 0 ][ 'comment' ],
			'timestamp' => strtotime( $data[ 'revisions' ][ 0 ][ 'timestamp' ] )
		);
		return $change;
	}
	
	function parseFeedData( $feedData, $useOld = false ) {
		$startTime = microtime( true );
		
		$urls = Array(
			'http://toolserver.org/~cobi/cb' . ( $useOld ? 'old' : '' ) . '.php?user=' . urlencode( $feedData[ 'user' ] ) . '&ns=' . $feedData[ 'namespaceid' ] . '&title=' . urlencode( $feedData[ 'title' ] ) . '&timestamp=' . urlencode( $feedData[ 'timestamp' ] ),
			'http://en.wikipedia.org/w/api.php?action=query&prop=revisions&titles=' . urlencode( ( $feedData[ 'namespaceid' ] == 0 ? '' : $feedData[ 'namespace' ] . ':' ) . $feedData[ 'title' ] ) . '&rvstartid=' . $feedData[ 'revid' ] . '&rvlimit=2&rvprop=timestamp|user|content&format=php'
		);
		
		list( $cb, $api ) = getUrlsInParallel( $urls );
		
		$api = current( $api[ 'query' ][ 'pages' ] );
		
		if(
			!(
				isset( $cb[ 'user_edit_count' ] )
				and isset( $cb[ 'user_distinct_pages' ] )
				and isset( $cb[ 'user_warns' ] )
				and isset( $api[ 'revisions' ][ 1 ][ 'user' ] )
				and isset( $cb[ 'user_reg_time' ] )
				and isset( $cb[ 'common' ][ 'page_made_time' ] )
				and isset( $cb[ 'common' ][ 'creator' ] )
				and isset( $cb[ 'common' ][ 'num_recent_edits' ] )
				and isset( $cb[ 'common' ][ 'num_recent_reversions' ] )
				and isset( $api[ 'revisions' ][ 0 ][ 'timestamp' ] )
				and isset( $api[ 'revisions' ][ 0 ][ '*' ] )
				and isset( $api[ 'revisions' ][ 1 ][ 'timestamp' ] )
				and isset( $api[ 'revisions' ][ 1 ][ '*' ] )
			)
		) {
			print_r( $feedData );
			print_r( $cb );
			print_r( $api );
			die( 'API error.' );
		}
		
		$data = Array(
			'EditType' => 'change',
			'EditID' => $feedData[ 'revid' ],
			'comment' => $feedData[ 'comment' ],
			'user' => $feedData[ 'user' ],
			'user_edit_count' => $cb[ 'user_edit_count' ],
			'user_distinct_pages' => $cb[ 'user_distinct_pages' ],
			'user_warns' => $cb[ 'user_warns' ],
			'prev_user' => $api[ 'revisions' ][ 1 ][ 'user' ],
			'user_reg_time' => $cb[ 'user_reg_time' ],
			'common' => Array(
				'page_made_time' => $cb[ 'common' ][ 'page_made_time' ],
				'title' => $feedData[ 'title' ],
				'namespace' => $feedData[ 'namespace' ],
				'creator' => $cb[ 'common' ][ 'creator' ],
				'num_recent_edits' => $cb[ 'common' ][ 'num_recent_edits' ],
				'num_recent_reversions' => $cb[ 'common' ][ 'num_recent_reversions' ]
			),
			'current' => Array(
				'minor' => ( stripos( $feedData[ 'flags' ], 'm' ) === false ) ? 'false' : 'true',
				'timestamp' => strtotime( $api[ 'revisions' ][ 0 ][ 'timestamp' ] ),
				'text' => $api[ 'revisions' ][ 0 ][ '*' ]
			),
			'previous' => Array(
				'timestamp' => strtotime( $api[ 'revisions' ][ 1 ][ 'timestamp' ] ),
				'text' => $api[ 'revisions' ][ 1 ][ '*' ]
			)
		);
		
		$feedData[ 'startTime' ] = $startTime;
		$feedData[ 'all' ] = $data;
		return $feedData;
	}
	
	function toXML( $data ) {
		$xml = xmlize( $data );
		$totalTime = microtime( true ) - $startTime;
		$xml .= '<!-- Processing time = ' . $totalTime . 's -->';
		return $xml;
	}
	
	function isVandalism( $data, &$score ) {
		$fp = fsockopen( Config::$coreip, Config::$coreport, $errno, $errstr, 15 );
		if( !$fp )
			return false;
		fwrite( $fp, str_replace( '</WPEditSet>', '', toXML( $data ) ) );
		fflush( $fp );
		$returnXML = '';
		$endeditset = false;
		while( !feof( $fp ) ) {
			$returnXML .= fgets( $fp, 4096 );
			if( strpos( $returnXML, '</WPEdit>' ) === false and !$endeditset ) {
				fwrite( $fp, '</WPEditSet>' );
				fflush( $fp );
				$endeditset = true;
			}
		}
		
		fclose( $fp );
		$data = simplexml_load_string( $returnXML );
		$score = (string) $data->WPEdit->score;
		$isVand = ( (string) $data->WPEdit->think_vandalism ) == 'true';
		return $isVand;
	}
	
	function oldData( $id ) {
		$feedData = genOldFeedData( $id );
		$feedData = parseFeedData( $feedData, true );
		$feedData = $feedData[ 'all' ];
		return $feedData;
	}
	
	function oldXML( $ids ) {
		if( !is_array( $ids ) )
			$ids = Array( $ids );
		$feedData = Array();
		foreach( $ids as $id ) 
			$feedData[] = oldData( $id );
		return toXML( $feedData );
	}
?>