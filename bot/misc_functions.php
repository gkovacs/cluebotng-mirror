<?PHP
	function myfnmatch ($pattern,$string) {
		if (strlen($string) < 4000) {
			return fnmatch($pattern,$string);
		} else {
			$pattern = strtr(preg_quote($pattern, '#'), array('\*' => '.*', '\?' => '.', '\[' => '[', '\]' => ']'));
			if (preg_match('#^'.$pattern.'$#',$string)) return true;
			return false;
		}
	}
	
	function doInit() {
		API::init();
		API::$a->login( Config::$user, Config::$pass );

		Globals::$mysql = false;
		checkMySQL();

		Globals::$tfas = 0;
		Globals::$stdin = fopen( 'php://stdin','r' );
		Globals::$run = API::$q->getpage( 'User:' . Config::$user . '/Run' );
		Globals::$wl = API::$q->getpage( 'User:' . Config::$user . '/Whitelist' );
		Globals::$optin = API::$q->getpage( 'User:' . Config::$user . '/Optin' );
		Globals::$aoptin = API::$q->getpage( 'User:' . Config::$user . '/AngryOptin' );

		$tmp = explode( "\n", API::$q->getpage( 'User:' . Config::$owner . '/CBAutostalk.js' ) );
		foreach( $tmp as $tmp2 )
			if( substr( $tmp2, 0, 1 ) != '#' ) {
				$tmp3 = explode( '|', $tmp2, 2 );
				$stalk[ $tmp3[ 0 ] ] = trim( $tmp3[ 1 ] );
			}
			
		$tmp = explode( "\n", API::$q->getpage( 'User:' . Config::$owner . '/CBAutoedit.js' ) );
		foreach( $tmp as $tmp2 )
			if( substr( $tmp2, 0, 1 ) != '#' ) {
				$tmp3 = explode( '|', $tmp2, 2 );
				$edit[ $tmp3[ 0 ] ] = trim( $tmp3[ 1 ] );
			}
	}
?>