<?PHP
	class SignOutPage extends Page {
		public function __construct() {
			session_destroy();
			header( 'Location: ?page=Sign+In' );
			die();
		}
		
		public function writeHeader() {
			echo 'Sign Out';
		}
		
		public function writeContent() {
			// No content.
		}
	}
	if( isset( $_SESSION[ 'username' ] ) )
		Page::registerPage( 'Sign Out', 'SignOutPage', 3 );
?>