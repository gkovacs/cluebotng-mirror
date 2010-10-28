<?PHP
	/* cbng.php, GPL'd, by Jacobi Carter. */
	function namespace2id( $ns ) {
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
		
		return $convert[ strtolower( str_replace( '_', ' ', $ns ) ) ];
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
				'comment' => $m[ 13 ]
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
		curl_setopt( $ch, CURLOPT_TIMEOUT, 30 );
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
		$root->appendChild( xmlizePart( $doc, 'WPEdit', $data ) );
		return $doc->saveXML();
	}
	
	function parseAllFeed( $feed ) {
		$feedData = parseFeed( $feed );
		return parseFeedData( $feedData );
	}
	
	function parseFeedData( $feedData ) {
		$startTime = microtime( true );
		
		$urls = Array(
			'http://toolserver.org/~cobi/cb.php?user=' . urlencode( $feedData[ 'user' ] ) . '&ns=' . $feedData[ 'namespaceid' ] . '&title=' . urlencode( $feedData[ 'title' ] ),
			'http://en.wikipedia.org/w/api.php?action=query&prop=revisions&titles=' . urlencode( ( $feedData[ 'namespaceid' ] == 0 ? '' : $feedData[ 'namespace' ] . ':' ) . $feedData[ 'title' ] ) . '&rvstartid=' . $feedData[ 'revid' ] . '&rvlimit=2&rvprop=timestamp|user|content&format=php'
		);
		
		list( $cb, $api ) = getUrlsInParallel( $urls );
		
		$api = array_values( $api[ 'query' ][ 'pages' ] );
		$api = $api[ 0 ];
		
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
?>