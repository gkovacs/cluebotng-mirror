<?PHP
	class IRC {
		private static $fd = null;
		private static $chans = Array();
		public static $pid;
		public static $server = '10.156.12.4';
		public static $port = 6667;
		public static $pass;
		
		public static function split( $message ) {
			if( !$message )
				return null;
			
			$return = Array();
			$i = 0;
			$quotes = false;
			
			if( $message[ $i ] == ':' ) {
				$return[ 'type' ] = 'relayed';
				$i++;
			} else
				$return[ 'type' ] = 'direct';
			
			$return[ 'rawpieces' ] = Array();
			$temp = '';
			for( ; $i < strlen( $message ) ; $i++ ) {
				if( $quotes and $message[ $i ] != '"' )
					$temp .= $message[ $i ];
				else 
					switch( $message[ $i ] ) {
						case ' ':
							$return[ 'rawpieces' ][] = $temp;
							$temp = '';
							break;
						case '"':
							if( $quotes or $temp == '' ) {
								$quotes = !$quotes;
								break;
							}
						case ':':
							if( $temp == '' ) {
								$i++;
								$return[ 'rawpieces' ][] = substr( $message, $i );
								$i = strlen( $message );
								break;
							}
						default:
							$temp .= $message[ $i ];
					}
			}
			if( $temp != '' )
				$return[ 'rawpieces' ][] = $temp;
			
			if( $return[ 'type' ] == 'relayed' ) {
				$return[ 'source' ] = $return[ 'rawpieces' ][ 0 ];
				$return[ 'command' ] = strtolower( $return[ 'rawpieces' ][ 1 ] );
				$return[ 'target' ] = $return[ 'rawpieces' ][ 2 ];
				$return[ 'pieces' ] = array_slice( $return[ 'rawpieces' ], 3 );
			} else {
				$return[ 'source' ] = 'Server';
				$return[ 'command' ] = strtolower( $return[ 'rawpieces' ][ 0 ] );
				$return[ 'target' ] = 'You';
				$return[ 'pieces' ] = array_slice( $return[ 'rawpieces' ], 1 );
			}
			$return[ 'raw' ] = $message;
			return $return;
		}
		
		public static function send( $line ) {
			fwrite( self::$fd, $line . "\n" );
		}
		
		public static function say( $chans, $message ) {
			$chans = 'irc' . $chans;
			echo 'Saying to ' . $chans . ' (' . self::$chans[ $chans ] . '): ' . $message . "\n";
			foreach( explode( ',', self::$chans[ $chans ] ) as $chan )
				self::send( 'PRIVMSG ' . $chan . ' :' . $message );
		}
		
		private static function loop( $data ) {
			$d = self::split( $data );
			if( $d === null )
				return;
			if( $d[ 'type' ] == 'direct' )
				switch( $d[ 'command' ] ) {
					case 'ping':
						self::send( 'PONG :' . $d[ 'pieces' ][ 0 ] );
						break;
				}
			else
				switch( $d[ 'command' ] ) {
					case '376':
					case '422':
						print_r( self::$chans );
						foreach( self::$chans as $chans )
							self::send( 'JOIN ' . $chans );
						break;
				}
		}
		
		private static function thread() {
			global $user;
			$nick = str_replace( ' ', '_', Config::$user );
			self::send( 'PASS ' . self::$pass );
			self::send( 'USER ' . $nick . ' "1" "1" :ClueBot Wikipedia Bot.' );
			self::send( 'NICK ' . $nick );
			while( !feof( self::$fd ) ) {
				$data = str_replace( array( "\n", "\r" ), '', fgets( self::$fd, 1024 ) );
				self::loop( $data );
			}
			die();
		}
		
		public static function init() {
			$ircconfig = explode( "\n", API::$q->getpage( 'User:' . Config::$owner . '/CBChannels.js' ) );
			$tmp = array();
			foreach( $ircconfig as $tmpline )
				if( $tmpline[ 0 ] != '#') {
					$tmpline = explode( '=', $tmpline, 2);
					$tmp[ trim( $tmpline[ 0 ] ) ] = trim( $tmpline[ 1 ] );
				}

			self::$chans = $tmp;

			self::$fd = fsockopen( self::$server, self::$port, $errno, $errstr, 15 );
			self::$pid = pcntl_fork();
			if( self::$pid == 0 )
				self::thread();

		}
	}
?>