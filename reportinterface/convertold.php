<?PHP
	include 'includes/header.php';
	$data = file_get_contents( 'oldstuff.txt' );
	preg_match_all( '/\'\'\'ID:\'\'\' (?-U)(\d+)(?U).*\'\'\'Comment:\'\'\' (.*)\n\=\=\=\=\= Discussion \=\=\=\=\=\n(.*)\n\=\=\=\=/iUs', $data, $matches, PREG_SET_ORDER );
	foreach( $matches as $match ) {
		$id = $match[ 1 ];
		$comment = trim( $match[ 2 ] );
		
		createReport( $id, 'Import Script' );
		if( trim( $comment ) != '' )
			createComment( $id, 'Anonymous', $comment );
		
		$discussion = trim( $match[ 3 ] );
		preg_match_all( '/(?-U)[\n:*#]*(?U)(.*)(\-+|\&.dash\;)? ?\[\[User:(.*)(\||\]\])(.*\(UTC\))/iUs', $discussion, $dmatches, PREG_SET_ORDER );
		foreach( $dmatches as $dmatch ) {
			$comment = str_replace( "\n:", "\n\n", $dmatch[ 1 ] );
			$user = $dmatch[ 3 ];
			createComment( $id, $user, $comment );
		}
	}
?>