<?PHP
	class Process {
		public static function processEditThread( $change ) {
			if( !isVandalism( $change[ 'all' ], $s ) )
				return;
			
			echo 'Is ' . $change[ 'user' ] . ' whitelisted ?' . "\n";
			if( Action::isWhitelisted( $change[ 'user' ] ) )
				return;
			echo 'No.' . "\n";
			
			$reason = 'ANN scored at ' . $s;
			
			$heuristic = '';
			$log = null;
			
			$diff = 'http://en.wikipedia.org/w/index.php' .
				'?title=' . urlencode( $change[ 'title' ] ) .
				'&diff=' . urlencode( $change[ 'revid' ] ) .
				'&oldid=' . urlencode( $change[ 'old_revid' ] );

 			$report = '[[' . str_replace( 'File:', ':File:', $change[ 'title' ] ) . ']] was '
				. '[' . $diff . ' changed] by '
				. '[[Special:Contributions/' . $change[ 'user' ] . '|' . $change[ 'user' ] . ']] '
				. '[[User:' . $change[ 'user' ] . '|(u)]] '
				. '[[User talk:' . $change[ 'user' ] . '|(t)]] '
				. $reason . ' on ' . gmdate( 'c' );

			$oftVand = unserialize( file_get_contents( 'oftenvandalized.txt' ) );
			if( rand( 1, 50 ) == 2 )
				foreach( $oftVand as $art => $artVands )
					foreach( $artVands as $key => $time )
						if( ( time() - $time ) > 2 * 24 * 60 * 60 )
							unset( $oftVand[ $art ][ $key ] );
			$oftVand[ $change[ 'title' ] ][] = time();
			if( count( $oftVand[ $change[ 'title' ] ] ) >= 30 )
				IRC::say( 'reportchannel', '!admin [['.$change['title'].']] has been vandalized '.(count($tmp[$change['title']])).' times in the last 2 days.' );
			file_put_contents( 'oftenvandalized.txt', serialize( $oftVand ) );

			IRC::say( 'debugchannel', 'Possible vandalism: ' . $change[ 'title' ] . ' changed by ' . $change[ 'user' ] . ' ' . $reason . '(' . $s . ')' );
			IRC::say( 'debugchannel', '( http://en.wikipedia.org/w/index.php?title=' . urlencode( $change[ 'title' ] ) . '&action=history | ' . $change[ 'url' ] . ' )' );

			$query = 'INSERT INTO `vandalism` ' .
				'(`id`,`user`,`article`,`heuristic`' . ( ( is_array( $log ) ) ? ',`regex`' : '' ) . ',`reason`,`diff`,`old_id`,`new_id`,`reverted`) ' .
				'VALUES ' .
				'(NULL,\'' . mysql_real_escape_string( $change[ 'user' ] ) . '\',' .
				'\'' . mysql_real_escape_string( $change[ 'title' ] ) . '\',' .
				'\'' . mysql_real_escape_string( $heuristic ) . '\',' .
				( ( is_array( $log ) ) ? '\'' . mysql_real_escape_string( $logt ) . '\',' : '' ) .
				'\'' . mysql_real_escape_string( $reason ) . '\',' .
				'\'' . mysql_real_escape_string( $change[ 'url' ] ) . '\',' .
				'\'' . mysql_real_escape_string( $change[ 'old_revid' ] ) . '\',' .
				'\'' . mysql_real_escape_string( $change[ 'revid' ] ) . '\',0)';
			checkMySQL();
			mysql_query( $query );
			$change[ 'mysqlid' ] = mysql_insert_id();
			
			echo 'Should revert?' . "\n";

			if( Action::shouldRevert( $change ) ) {
				echo 'Yes.' . "\n";
				$rbret = Action::doRevert( $change );
				if ($rbret !== false) {
					IRC::say( 'debugchannel', 'Reverted. (' . ( microtime( true ) - $change[ 'startTime' ] ) . ' s)' );
					Action::doWarn( $change, $report );
					checkMySQL();
					mysql_query( 'UPDATE `vandalism` SET `reverted` = 1 WHERE `id` = \'' . mysql_real_escape_string( $change[ 'mysqlid' ] ) . '\'' );
				} else {
					$rv2 = API::$a->revisions( $change[ 'title' ], 1 );
					if( $change[ 'user' ] != $rv2[ 0 ][ 'user' ] ) {
						IRC::say( 'debugchannel', 'Grr! Beaten by ' . $rv2[ 0 ][ 'user' ] );
						checkMySQL();
						mysql_query( 'INSERT INTO `beaten` (`id`,`article`,`diff`,`user`) VALUES (NULL,\'' . mysql_real_escape_string( $change['title'] ) . '\',\'' . mysql_real_escape_string( $change[ 'url' ] ) . '\',\'' . mysql_real_escape_string( $rv2[ 0 ][ 'user' ] ) . '\')' );
					}
				}
			}
		}
		
		public static function processEdit( $change ) {
			if (
				( time() - Globals::$tfas ) >= 1800
				and ( preg_match( '/\(\'\'\'\[\[([^|]*)\|more...\]\]\'\'\'\)/iU', API::$q->getpage( 'Wikipedia:Today\'s featured article/' . date( 'F j, Y' ) ), $tfam ) )
			) {
				Globals::$tfas = time();
				Globals::$tfa = $tfam[ 1 ];
			}
			if( Config::$fork ) {
				$pid = pcntl_fork();
				if( $pid != 0 ) {
					echo 'Forked - ' . $pid . "\n";
					return;
				}
				
			}
			$change = parseFeedData( $change );
			$change[ 'justtitle' ] = $change[ 'title' ];
			if( $change[ 'namespace' ] != 'Main:' )
				$change[ 'title' ] = $change[ 'namespace' ] . $change[ 'title' ];
			self::processEditThread( $change );
			if( Config::$fork )
				die();
		}
	}