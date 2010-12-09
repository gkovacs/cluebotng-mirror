<?PHP
	class SignInPage extends Page {
		public function __construct() {
			if( isset( $_POST[ 'submit' ] ) ) {
				$query = 'SELECT `userid`, `username`, `admin`, `superadmin` FROM `users` WHERE `username` = ';
				$query.= '\'' . mysql_real_escape_string( $_POST[ 'username' ] ) . '\' AND `password` = ';
				$query.= 'PASSWORD(\'' . mysql_real_escape_string( $_POST[ 'password' ] ) . '\')';
				
				$row = mysql_fetch_assoc( mysql_query( $query ) );
				if( $row ) {
					$_SESSION[ 'userid' ] = $row[ 'userid' ];
					$_SESSION[ 'username' ] = $row[ 'username' ];
					$_SESSION[ 'admin' ] = $row[ 'admin' ] ? true : false;
					$_SESSION[ 'sadmin' ] = $row[ 'superadmin' ] ? true : false;
					header( 'Location: ?page=List' );
					die();
				} else {
					header( 'Location: ?page=Sign+In' );
					die();
				}
			}
		}
		
		public function writeHeader() {
			echo 'Sign In';
		}
		
		public function writeContent() {
			echo '<form method="post">';
			echo '<table>';
			echo '<tr><th>Username:</th><td><input type="text" name="username" /></td></tr>';
			echo '<tr><th>Password:</th><td><input type="password" name="password" /></td></tr>';
			echo '<tr><td colspan=2><input type="submit" name="submit" value="Sign In" /></td></tr>';
			echo '</table>';
			echo '</form>';
		}
	}
	if( !isset( $_SESSION[ 'username' ] ) )
		Page::registerPage( 'Sign In', 'SignInPage', 3 );
?>