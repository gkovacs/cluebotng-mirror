<?PHP
	class MainPage extends Page {
		public function __construct() {
			
		}
		
		public function writeHeader() {
			echo 'Report';
		}
		
		public function writeContent() {
			echo '<form action="?page=Report" method="post">';
			
			echo '<h3>You need to get ClueBot NG\'s revert ID, there are two ways to do this:</h3>';
			
			echo '<b>From the article:</b><br />';
			echo '<div class="image" style="width: 400px">';
			echo '<img alt="" src="http://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/CBFPHowto1.png/400px-CBFPHowto1.png" width="400" height="88" /><br />';
			echo '<small>Click the history tab at the top of the article.</small>';
			echo '</div>';
			echo '<div class="image" style="width: 600px">';
			echo '<img alt="" src="http://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/CBFPHowto2.png/600px-CBFPHowto2.png" width="600" height="139" class="thumbimage" /><br />';
			echo '<small>The revert ID is in parenthesis right after "Thanks, User:ClueBot".</small>';
			echo '</div>';
			
			echo '<b>From your talk page:</b><br />';
			echo '<div class="image" style="width: 400px">';
			echo '<img alt="" src="http://upload.wikimedia.org/wikipedia/commons/thumb/7/72/CBFPHowto3.png/400px-CBFPHowto3.png" width="400" height="99" class="thumbimage" /><br />';
			echo '<small>Click the "edit this page" tab at the top of the page.</small>';
			echo '</div>';
			echo '<div class="image" style="width: 600px">';
			echo '<img alt="" src="http://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/CBFPHowto4.png/600px-CBFPHowto4.png" width="600" height="298" class="thumbimage" /><br />';
			echo '<small>The revert ID is in the text right after "MySQL ID:".</small>';
			echo '</div>';
			
			echo 'Revert ID: ';
			echo '<input type="text" name="id">';
			echo '<input type="submit" value="Submit">';
			
			echo '</form>';
		}
	}
	Page::registerPage( 'Main', 'MainPage', 0 );
?>