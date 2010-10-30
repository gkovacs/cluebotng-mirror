<?PHP
	class Action {
		public static function getWarningLevel( $user, &$content = null ) {
			$warning = 0;
			$content = API::$q->getpage( 'User talk:' . $user );
			if( preg_match_all( '/<!-- Template:(uw-[a-z]*(\d)(im)?|Blatantvandal \(serious warning\)) -->.*(\d{2}):(\d{2}), (\d+) ([a-zA-Z]+) (\d{4}) \(UTC\)/iU',
				$content,
				$match,
				PREG_SET_ORDER
			) )
				foreach( $match as $m ) {
					$month = Array(
						'January' => 1, 'February' => 2, 'March' => 3, 
						'April' => 4, 'May' => 5, 'June' => 6, 'July' => 7, 
						'August' => 8, 'September' => 9, 'October' => 10, 
						'November' => 11, 'December' => 12
					);
					if( $m[ 1 ] == 'Blatantvandal (serious warning)' )
						$m[ 2 ] = 4;
					if( ( time() - gmmktime( $m[ 4 ], $m[ 5 ], 0, $month[ $m[ 7 ] ], $m[ 6 ], $m[ 8 ] ) ) <= ( 2 * 24 * 60 * 60 ) )
						if( $m[ 2 ] > $warning )
							$warning = $m[2];
				}
			return $warning;
		}
		
		private static function aiv( $change, $report ) {
			$aivdata = API::$q->getpage( 'Wikipedia:Administrator_intervention_against_vandalism/TB2' );
			if( !preg_match( '/' . preg_quote( $change[ 'user' ], '/' ) . '/i', $aivdata ) ) {
				IRC::say( 'aivchannel', '!admin Reporting [[User:' . $change[ 'user' ] . ']] to [[WP:AIV]]. Contributions: [[Special:Contributions/' . $change[ 'user' ] . ']] Block: [[Special:Blockip/' . $change[ 'user' ] . ']]' );
				
				API::$a->edit(
					'Wikipedia:Administrator_intervention_against_vandalism/TB2',
					$aivdata . "\n\n"
					. '* {{' . ( ( long2ip( ip2long( $change[ 'user' ] ) ) == $change[ 'user' ] ) ? 'IPvandal' : 'Vandal' ) . '|' . $change[ 'user' ] . '}}'
					. ' - ' . $report . ' (Automated) ~~~~' . "\n",
					'Automatically reporting [[Special:Contributions/' . $change[ 'user' ] . ']]. (bot)',
					false,
					false
				);
			}
		}
		
		private static function warn( $change, $report, $content, $warning ) {
			API::$a->edit(
				'User talk:' . $change[ 'user' ],
				$content . "\n\n"
				. '{{subst:User:' . $user . '/Warnings/Warning'
				. '|1=' . $warning
				. '|2=' . str_replace( 'File:', ':File:', $change[ 'title' ] )
				. '|3=' . $report
				. ' <!{{subst:ns:0}}-- MySQL ID: ' . $change[ 'mysqlid' ] . ' --{{subst:ns:0}}>}} ~~~~'
				. "\n",
				'Warning [[Special:Contributions/' . $change[ 'user' ] . '|' . $change[ 'user' ] . ']] - #' . $warning,
				false,
				false
			); /* Warn the user */
		}
		
		public static function doWarn( $change, $report ) {
			$warning = self::getWarningLevel( $change[ 'user' ], $tpcontent ) + 1;
			if( !Config::$dry ) {
				if( $warning == 5 ) /* Report them if they have been warned 4 times. */
					self::aiv( $change, $report );
				else if( $warning < 5 ) /* Warn them if they haven't been warned 4 times. */
					self::warn( $change, $report, $tpcontent, $warning );
			}
			IRC::say( 'vandalismchannel', 'rcbot bl add ' . $change[ 'user' ] . ' x=' . ( 24 * $warning ) . ' r=Vandalism to [[' . $change[ 'title' ] . ']] (#' . $warning . ').' );
		}
		
		public static function doRevert( $change ) {
			$rev = API::$a->revisions( $change[ 'title' ], 5, 'older', false, null, true, true );
			$revid = 0;
			$rbtok = $rev[ 0 ][ 'rollbacktoken' ];
			foreach( $rev as $revdata )
				if( $revdata[ 'user' ] != $change[ 'user' ] ) {
					$revid = $revdata[ 'revid' ];
					break;
				}
			if( ( $revdata[ 'user' ] == $user ) or ( in_array( $revdata[ 'user' ], explode( ',', Config::$friends ) ) ) )
				return false;
			IRC::say( 'debugchannel', 'Reverting ...' );
			if( Config::$dry )
				return true;
			$rbret = API::$a->rollback(
				$change[ 'title' ],
				$change[ 'user' ],
				'Reverting possible vandalism by [[Special:Contributions/' . $change[ 'user' ] . '|' . $change[ 'user' ] . ']] ' .
				'to ' . ( ( $revid == 0 ) ? 'older version' : 'version by ' . $revdata[ 'user' ] ) . '. ' .
				'False positive? [[User:' . Config::$user . '/FalsePositives|Report it]]. '.
				'Thanks, [[User:' . Config::$user . '|' . Config::$user . ']]. (' . $change[ 'mysqlid' ] . ') (Bot)',
				$rbtok
			);
			return $rbret;
		}
		
		public static function shouldRevert( $change ) {
			if( preg_match( '/(assisted|manual)/iS', Config::$status ) ) {
				echo 'Revert [y/N]? ';
				if( strtolower( substr( fgets( Globals::$stdin, 3 ), 0, 1 ) ) != 'y' )
					return false;
			}
			if( Config::$angry )
				return true;
			if( ( time() - Globals::$tfas ) >= 1800 ) {
				if( preg_match( '/\(\'\'\'\[\[([^|]*)\|more...\]\]\'\'\'\)/iU', API::$q->getpage( 'Wikipedia:Today\'s featured article/' . date( 'F j, Y' ) ), $tfam ) ) {
					Globals::$tfas = time();
					Globals::$tfa = $tfam[ 1 ];
				}
			}
			if( Globals::$tfa == $change[ 'title' ] )
				return true;
			if( preg_match( '/\* \[\[('. preg_quote( $change[ 'title' ], '/' ) . ')\]\] \- .*/i', Globals::$aoptin ) ) {
				IRC::say( 'debugchannel', 'Angry-reverting [[' . $change[ 'title' ] . ']].' );
				return true;
			}
			$titles = unserialize( file_get_contents( 'titles.txt' ) );
			if(
				!isset( $titles[ $change[ 'title' ] . $change[ 'user' ] ] )
				or ( ( time() - $titles[ $change[ 'title' ] . $change[ 'user' ] ] ) > ( 24 * 60 * 60 ) )
			) {
				$titles[ $change[ 'title' ] . $change[ 'user' ] ] = time();
				file_put_contents( 'titles.txt', serialize( $titles ) );
				return true;
			}
			return false;
		}
		
		public static function isWhitelisted( $user ) {
//			if (
//				(
//					( /* IP users with 250 contributions are fine .. */
//						(long2ip(ip2long($change['user'])) == $change['user'])
//						/* and ($uc = $wpapi->usercontribs($change['user'],250))
//						and (!isset($uc[249])) */
//					)
//					or ( /* Users with 50 contributions are fine .. */
//						(long2ip(ip2long($change['user'])) != $change['user'])
//						and ($wpq->contribcount($change['user']) < 50)
//					)
//				)
//				and ( /* Whitelisted users are ok. */
//					/* ($wl = $wpq->getpage('User:'.$user.'/Whitelist'))
//					and */ (!preg_match('/^\* \[\[User:('.preg_quote($change['user'],'/').')|\1\]\] \- .*/',$wl))
//				)
//			) {
			return false;
		}
	}
?>