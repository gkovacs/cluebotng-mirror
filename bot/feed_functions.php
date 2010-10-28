<?PHP
	class Feed {
		public static $host = 'irc.wikimedia.org';
		public static $port = 6667;
		public static $channel = '#en.wikipedia';
		private static $fd;
		public static function connectLoop() {
			self::$fd = fsockopen( self::$host, self::$port, $feederrno, $feederrstr, 30 );

			if( !self::$fd )
				return;
			
			$nick = str_replace( ' ', '_', Config::$user );

			self::send( 'USER ' . $nick . ' "1" "1" :ClueBot Wikipedia Bot 2.0.' );
			self::send( 'NICK ' . $nick );

			while( !feof( self::$fd ) ) {
				$rawline = fgets( self::$fd, 1024 );
				$line = str_replace( array( "\n", "\r", "\002" ), '', $rawline );
				$line = preg_replace( '/\003(\d\d?(,\d\d?)?)?/', '', $line );
				if( !$line ) {
					fclose( self::$fd );
					break;
				}
				self::loop( $line );
			}
		}
		
		private static function loop( $line ) {
			$d = IRC::split( $line );
			
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
						self::send( 'JOIN ' . self::$channel );
						break;
					case 'privmsg':
						if( strtolower( $d[ 'target' ] ) == self::$channel ) {
							$message = $d[ 'pieces' ][ 0 ];
							
							$data = parseFeed( $message );
							
							if( $data === false )
								return;
							
							if( stripos( 'N', $data[ 'flags' ] ) !== false )
								return;
							
							if(
								( $data[ 'namespace' ] != 'Main:' )
								and ( ( !preg_match( '/\* \[\[(' . preg_quote( $data[ 'namespace' ] . $data[ 'title' ], '/' ) . ')\]\] \- .*/i', Globals::$optin ) ) )
								and ( $change[ 'flags' ] != 'move' )
								and ( $change[ 'namespace' ] != 'Template:')
							)
								return;
							
							echo 'Processing: ' . $message . "\n";
							Process::processEdit( $data );
						}
						break;
				}
		}
		
		public static function send( $line ) {
			fwrite( self::$fd, $line . "\n" );
		}
	}
?>