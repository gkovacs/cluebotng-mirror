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
				
				$type = '';
				if( ( (String) $edit->Constructive ) == ( (String) $edit->Required ) )
					$type = 'C';
				else if( ( (String) $edit->Skipped ) == ( (String) $edit->Required ) )
					$type = 'S';
				else if( ( (String) $edit->Vandalism ) == ( (String) $edit->Required ) )
					$type = 'V';
				else
					$type = 'U';
				
				if( $type == 'V' and ( (String) $edit->Constructive ) > 0 )
					$type .= 'S';
				else if( $type == 'C' and ( (String) $edit->Vandalism ) > 0 )
					$type .= 'S';
				
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
		$doneKeys = Array();
		$editGroupAPI = callReviewAPI( Array( 'get' => Array( 'editgroup' => $egKey ) ) )->GetEditGroup->EditGroup;
		foreach( $editGroupAPI->Done->EditKey as $eKey )
			$doneKeys[] = $eKey;
		
		$xml->startElement( 'EditGroup' );
		$xml->writeElement( 'Key', (String) $editGroupAPI->Key );
		$xml->writeElement( 'Name', (String) $editGroupAPI->Name );
		$xml->writeElement( 'Weight', (String) $editGroupAPI->Weight );
		$xml->startElement( 'Done' );
		processEdits( $xml, $doneKeys );
		$xml->endElement();
		$xml->endElement();
	}
	
	function processEditGroups() {
		$xml = new XMLWriter();
		$xml->openURI( 'php://output' );
		$xml->setIndent( true );
		$xml->startDocument( '1.0', 'UTF-8' );
		$xml->startElement( 'EditGroups' );
		foreach( callReviewAPI( Array( 'list' => 'editgroups' ) )->ListEditGroups->EditGroupKey as $egKey )
			processEditGroup( $xml, $egKey );
		$xml->endDocument();
		$xml->flush();
	}
	
	processEditGroups();
?>