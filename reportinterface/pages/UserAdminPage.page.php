<?PHP
	class UserAdminPage extends Page {
		private $users;
		
		public function __construct() {
			if( !isSAdmin() )
				die( '*sigh*' );
			
			if( isset( $_REQUEST[ 'action' ] ) ) {
				$set = null;
				switch( $_REQUEST[ 'action' ] ) {
					case 'delete':
						mysql_query( 'UPDATE `comments` SET `userid` = -1 WHERE `userid` = \'' . mysql_real_escape_string( $_REQUEST[ 'uid' ] ) . '\'' );
						mysql_query( 'UPDATE `reports` SET `reporterid` = -1 WHERE `reporterid` = \'' . mysql_real_escape_string( $_REQUEST[ 'uid' ] ) . '\'' );
						mysql_query( 'DELETE FROM `users` WHERE `userid` = \'' . mysql_real_escape_string( $_REQUEST[ 'uid' ] ) . '\'' );
						break;
					case 'superadmin':
						$set = '`superadmin` = 1';
						break;
					case 'admin':
						$set = '`admin` = 1';
						break;
					case 'deadmin':
						$set = '`admin` = 0';
						break;
				}
				
				if( $set !== null ) {
					mysql_query( 'UPDATE `users` SET ' . $set . ' WHERE `userid` = \'' . mysql_real_escape_string( $_REQUEST[ 'uid' ] ) . '\'' );
				}
				
				rc( '[[report:Special:UserAdmin]] ' . $_REQUEST[ 'action' ] . ' http://' . $_SERVER[ 'HTTP_HOST' ] . $_SERVER[ 'PHP_SELF' ] . '?page=User+Admin * ' . $_SESSION[ 'username' ] . ' * ' . $_REQUEST[ 'action' ] . ' ' . $_REQUEST[ 'user' ] );
				
				header( 'Location: ?page=User+Admin' );
				die();
			}
			
			$result = mysql_query( 'SELECT `userid`, `username`, `email`, `admin`, `superadmin` FROM `users`' );
			$this->users = Array();
			while( $row = mysql_fetch_assoc( $result ) )
				$this->users[] = Array(
					'id' => $row[ 'userid' ],
					'user' => $row[ 'username' ],
					'email' => $row[ 'email' ],
					'admin' => $row[ 'superadmin' ] ? 'super' : ( $row[ 'admin' ] ? 'yes' : 'no' )
				);
		}
		
		public function writeHeader() {
			echo 'User Admin';
		}
		
		public function writeContent() {
			echo '<table border=1>';
			echo '<tr><th>Actions</th><th>User ID</th><th>Username</th><th>Email</th><th>Admin</th></tr>';
			foreach( $this->users as $user ) {
				echo '<tr>';
				echo '<td>';
				if( $user[ 'admin' ] != 'super' ) {
					echo '<a href="?page=User+Admin&action=delete&uid=' . $user[ 'id' ] . '&user=' . urlencode( $user[ 'user' ] ) . '">X</a> &middot; ';
					echo '<a href="?page=User+Admin&action=superadmin&uid=' . $user[ 'id' ] . '&user=' . urlencode( $user[ 'user' ] ) . '">++</a> &middot; ';
					echo '<a href="?page=User+Admin&action=admin&uid=' . $user[ 'id' ] . '&user=' . urlencode( $user[ 'user' ] ) . '">+</a> &middot; ';
					echo '<a href="?page=User+Admin&action=deadmin&uid=' . $user[ 'id' ] . '&user=' . urlencode( $user[ 'user' ] ) . '">-</a> ';
				} else
					echo 'None';
				echo '</td>';
				echo '<td>' . $user[ 'id' ] . '</td>';
				echo '<td>' . htmlentities( $user[ 'user' ] ) . '</td>';
				echo '<td>' . htmlentities( $user[ 'email' ] ) . '</td>';
				echo '<td>' . htmlentities( $user[ 'admin' ] ) . '</td>';
				echo '</tr>';
			}
			echo '</table>';
		}
	}
	Page::registerPage( 'User Admin', 'UserAdminPage', 5, true, true );
?>