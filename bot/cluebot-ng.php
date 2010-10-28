<?PHP
	declare( ticks = 1 );

	require_once 'includes.php';

	function sig_handler( $signo ) {
		switch( $signo ) {
			case SIGCHLD:
				while( ( $x = pcntl_waitpid( 0, $status, WNOHANG ) ) != -1 ) {
					if( $x == 0 )
						break;
					$status = pcntl_wexitstatus( $status );
				}
				break;
		}
	}

	pcntl_signal( SIGCHLD, 'sig_handler' );

	doInit();
	IRC::init();
	for(;;)
		Feed::connectLoop();
?>