<?PHP
	class ViewPage extends Page {
		private $row;
		private $id;
		private $data;
		
		public function __construct() {
			$this->id = $_REQUEST[ 'id' ];
			$result = mysql_query( 'SELECT * FROM `vandalism` WHERE `id` = \'' . mysql_real_escape_string( $this->id ) . '\'' );
			$this->row = mysql_fetch_assoc( $result );
			$this->data = getReport( $this->id );
			if( $this->data === null ) {
				header( 'Location: ?page=Report&id=' . $this->id );
				die();
			}
			
			if( isset( $_POST[ 'submit' ] ) )
				if( trim( $_POST[ 'comment' ] ) != '' ) {
					createComment( $this->id, $_POST[ 'user' ], $_POST[ 'comment' ] );
					header( 'Location: ?page=View&id=' . $this->id );
					die();
				}
				
			if( isset( $_REQUEST[ 'status' ] ) and isAdmin() ) {
				updateStatus( $this->id, $_REQUEST[ 'status' ], $_SESSION[ 'username' ] );
				header( 'Location: ?page=View&id=' . $this->id );
				die();
			}
			if( isset( $_REQUEST[ 'deletecomment' ] ) and isSAdmin() ) {
				mysql_query( 'DELETE FROM `comments` WHERE `commentid` = \'' . mysql_real_escape_string( $_REQUEST[ 'deletecomment' ] ) . '\'' );
				header( 'Location: ?page=View&id=' . $this->id );
				die();
			}
		}
		
		public function writeHeader() {
			echo 'Viewing ' . $this->id;
		}
		
		public function writeContent() {
			require 'pages/View.tpl.php';
		}
	}
	Page::registerPage( 'View', 'ViewPage', 0, false );
?>