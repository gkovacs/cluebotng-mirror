<?PHP
	include '../reviewinterface/reviewApi.php';
	
	function processEdits( $xml, $keys ) {
		foreach( array_chunk( $keys, 50 ) as $run ) {
			$api = callReviewAPI( Array( 'get' => Array( 'edit' => $run ) ) );
			foreach( $api->GetEdit->Edit as $edit ) {
				$xml->startElement( 'Edit' );
				
				$xml->writeElement( 'Key', (String) $edit->Key );
				$xml->writeElement( 'ID', (String) $edit->ID );
				$xml->writeElement( 'Weight', (String) $edit->Weight );
				$xml->writeElement( 'Required', (String) $edit->Required );
				$xml->writeElement( 'Constructive', (String) $edit->Constructive );
				$xml->writeElement( 'Skipped', (String) $edit->Skipped );
				$xml->writeElement( 'Vandalism', (String) $edit->Vandalism );
				$xml->writeElement( 'OriginalClassification', (String) $edit->Classification );

				$required = (String) $edit->Required;
				$constructive = (String) $edit->Constructive;
				$skipped = (String) $edit->Skipped;
				$vandalism = (String) $edit->Vandalism;
				$max = max( $constructive, $skipped, $vandalism );
				$sum = $constructive + $skipped + $vandalism;

				if( $max < $required )
					$type = 'U';
				else if( 2 * $skipped > $sum )
					$type = 'S';
				else if( $constructive >= 3 * $vandalism )
					$type = 'C';
				else if( $vandalism >= 3 * $constructive )
					$type = 'V';
				else
					$type = 'U';
				
				$xml->writeElement( 'RealClassification', $type );
				
				$xml->startElement( 'Comments' );
				foreach( $edit->Comments->Comment as $comment )
					$xml->writeElement( 'Comment', (String) $comment );
				$xml->endElement();
				
				$xml->startElement( 'Users' );
				foreach( $edit->Users->UserKey as $userKey )
					$xml->writeElement( 'UserKey', (String) $userKey );
				$xml->endElement();
				
				$xml->endElement();
			}
		}
	}
	
	function processEditGroup( $xml, $egKey ) {
		$editKeys = Array();
		$reviewKeys = Array();
		$doneKeys = Array();
		$editGroupAPI = callReviewAPI( Array( 'get' => Array( 'editgroup' => $egKey ) ) )->GetEditGroup->EditGroup;
		
		foreach( $editGroupAPI->Edits->EditKey as $eKey )
			$editKeys[] = $eKey;
		foreach( $editGroupAPI->Reviewed->EditKey as $eKey )
			$ReviewKeys[] = $eKey;
		foreach( $editGroupAPI->Done->EditKey as $eKey )
			$doneKeys[] = $eKey;
		
		$xml->startElement( 'EditGroup' );
		$xml->writeElement( 'Key', (String) $editGroupAPI->Key );
		$xml->writeElement( 'Name', (String) $editGroupAPI->Name );
		$xml->writeElement( 'Weight', (String) $editGroupAPI->Weight );
		$xml->startElement( 'Edits' );
		processEdits( $xml, $editKeys );
		$xml->endElement();
		$xml->startElement( 'Reviewed' );
		processEdits( $xml, $reviewKeys );
		$xml->endElement();
		$xml->startElement( 'Done' );
		processEdits( $xml, $doneKeys );
		$xml->endElement();
		$xml->endElement();
	}
	
	function processUser( $xml, $uKey ) {
		$userAPI = callReviewAPI( Array( 'get' => Array( 'user' => $uKey ) ) )->GetUser->User;
		
		$xml->startElement( 'User' );
		$xml->writeElement( 'Key', (String) $userAPI->Key );
		$xml->writeElement( 'Nick', (String) $userAPI->Nick );
		$xml->writeElement( 'Classifications', (String) $userAPI->Classifications );
		$xml->endElement();
	}
	
	function processData() {
		$xml = new XMLWriter();
		$xml->openURI( 'php://output' );
		$xml->setIndent( true );
		$xml->startDocument( '1.0', 'UTF-8' );
		$xml->startElement( 'Data' );
		$xml->startElement( 'EditGroups' );
		foreach( callReviewAPI( Array( 'list' => 'editgroups' ) )->ListEditGroups->EditGroupKey as $egKey )
			processEditGroup( $xml, $egKey );
		$xml->endElement();
		$xml->startElement( 'Users' );
		foreach( callReviewAPI( Array( 'list' => 'users' ) )->ListUsers->UserKey as $uKey )
			processUser( $xml, $uKey );
		$xml->endElement();
		$xml->endElement();
		$xml->endDocument();
		$xml->flush();
	}
	
	processData();
?>
