<?PHP
	class ListPage extends Page {
		private $ids;
		
		public function __construct() {
			if( !isset( $_REQUEST[ 'showall' ] ) )
				$where = ' WHERE `status` = 0 OR `status` = 3';
			else
				$where = '';
			$result = mysql_query( 'SELECT `revertid`, `reporter`, `status` FROM `reports`' . $where );
			$this->ids = Array();
			while( $row = mysql_fetch_assoc( $result ) )
				$this->ids[] = Array(
					'id' => $row[ 'revertid' ],
					'user' => $row[ 'reporter' ],
					'status' => statusIdToName( $row[ 'status' ] )
				);
		}
		
		public function writeHeader() {
			echo 'List';
		}
		
		public function writeContent() {
			echo '<table>';
			echo '<tr><th>ID</th><th>Reporter</th><th>Status</th></tr>';
			foreach( $this->ids as $entry )
				echo '<tr>'
					. '<td><a href="?page=View&id=' . urlencode( $entry[ 'id' ]) . '">' . htmlentities( $entry[ 'id' ] ) . '</a></td>'
					. '<td>' . htmlentities( $entry[ 'user' ] ) . '</td>'
					. '<td>' . htmlentities( $entry[ 'status' ] ) . '</td>'
					. '</tr>';
			echo '</table>';
		}
	}
	Page::registerPage( 'List', 'ListPage', 1 );
?>