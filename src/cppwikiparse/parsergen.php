<?php

/*
This file is a PHP script that generates C++ code and Flex input files.
It was created as part of an effort to abstract Flex into a simple "Find X set of tokens and take different actions depending on the token" usage scenario.
To make flex work in this mode, start conditions, among other things, have to be generated for each set of tokens to find at a time.
This script contains definitions of tokens to find and functions  to find them.
*/



/* Input files */
$ll_template = 'wikilex.ll.template';
/* Output files */
$ll_output = 'wikilex.ll';
$func_output = 'parsegen.inc.cpp';
$func_decl_output = 'parsegen.inc.hpp';
$token_enum_output = 'parsegen_tokenlist.inc.hpp';

/* Specify all possible tokens, except for TEXT and END.
   Associated regular expressions are in flex format.
   All names must be unique.
   Precedence is standard flex rules (ie, order may be significant) */
$tokens = array( 
	'CURLY_OPEN_SERIES'	=> '\{\{+',
	'CURLY_OPEN_2'		=> '\{\{',
	'CURLY_OPEN_3'		=> '\{\{\{',
	'WHITESPACE'		=> '[ \t]+',
	'ALLWHITESPACE'		=> '[ \t\r\n]+',
	'CURLY_CLOSE_2'		=> '\}\}',
	'PIPE'			=> '\|'
);

$text_tok_name = "TEXT";
$eof_tok_name = "END";

$fname_prefix = "pgen_";




/*********** BEGIN PARSEGEN CODE *******************/

// Automatically filled in
$startconds = array();
$startcond_cindex = 1;
$funcspecs = array();

function StartCond_Exists($token_list) {
	global $startconds;
	global $text_tok_name, $eof_tok_name;
	foreach($token_list as $i => $t) if($t == $text_tok_name || $t == $eof_tok_name) unset($token_list[$i]);
	sort($token_list);
	return array_search($token_list, $startconds);
}

function StartCond_Add($token_list, $name = FALSE) {
	global $startconds, $startcond_cindex, $tokens;
	if(($exist_name = StartCond_Exists($token_list)) !== FALSE) return $exist_name;
	if($name === FALSE) {
		$name = "COND" . $startcond_cindex;
		$startcond_cindex++;
	}
	global $text_tok_name, $eof_tok_name;
	foreach($token_list as $i => $t) if($t == $text_tok_name || $t == $eof_tok_name) unset($token_list[$i]);
	sort($token_list);
	foreach($token_list as $tokname) if(!array_key_exists($tokname, $tokens)) die("Token " . $tokname . " does not exist!\n");
	$startconds[$name] = $token_list;
	return $name;
}

function FuncSpec_Add($funcname, $tokspec) {
	global $funcspecs;
	$funcspecs[$funcname] = $tokspec;
}

function Generate() {
	global $startconds, $funcspecs, $fname_prefix, $text_tok_name, $eof_tok_name, $tokens, $ll_template, $ll_output, $func_output, $func_decl_output, $token_enum_output;
	
	echo "Generating start conditions ...\n";
	
	$funcstartconds = array();
	foreach($funcspecs as $funcname => $tokspec) {
		$funcstartconds[$funcname] = StartCond_Add(array_keys($tokspec));
	}

	echo "Finding start conditions per token ...\n";

	$tokstartconds = array();
	foreach($tokens as $tokname => $tokregex) $tokstartconds[$tokname] = array();
	foreach($startconds as $condname => $condtoks) foreach($condtoks as $condtok) if(array_search($condname, $tokstartconds[$condtok]) === FALSE) $tokstartconds[$condtok][] = $condname;

	echo "Creating flex start condition block ...\n";

	$startcond_ll_list = '%s';
	foreach($startconds as $condname => $condtoks) $startcond_ll_list .= " " . $condname;
	$startcond_ll_list .= "\n";

	echo "Creating flex token block ...\n";

	$ll_token_block = "";
	foreach($tokstartconds as $tokname => $tokconds) if(count($tokconds) > 0) $ll_token_block .= '<' . implode(',', $tokconds) . '>' . $tokens[$tokname] . "\t\t" . 'RETTOKEN(' . $tokname . ');' . "\n";
	
	echo "Creating Flex input file ...\n";

	$lltemplatetext = file_get_contents($ll_template);
	$lltemplatetext = str_replace('__PARSEGEN_STARTCONDITIONS__', $startcond_ll_list, $lltemplatetext);
	$lltemplatetext = str_replace('__PARSEGEN_RULES__', $ll_token_block, $lltemplatetext);
	file_put_contents($ll_output, $lltemplatetext);

	echo "Creating token enum file ...\n";
	
	$tokenum = 'enum TokenType { ' . $eof_tok_name . ', ' . $text_tok_name . ', ' . implode(', ', array_keys($tokens)) . ' };' . "\n";
	file_put_contents($token_enum_output, $tokenum);
	
	echo "Creating function files ...\n";
	
	$funcdecl = '';
	$funcdefn = '';
	
	foreach($funcspecs as $funcname => $tokspec) {
		// Make function declaration
		$funcdecl .= "#ifdef {$funcname}_FUNC_DECL\n{$funcname}_FUNC_DECL;\n#else\nPGEN_FUNC_DECL({$funcname});\n#endif\n";
		
		// Check to see if any of the possible actions is an error.  If not, we don't have to save state.
		$acthaserr = FALSE;
		foreach($tokspec as $tokname => $act) if(isset($act['error'])) if($act['error'] === TRUE) $acthaserr = TRUE;
		if($acthaserr) $funcdefn .= "#define {$funcname}_SAVE_STATE\n";

		// Function definition start
		$funcdefn .= <<<ENDFUNCDEFN
#ifdef {$funcname}_FUNC_DEFN
// Function definition for {$funcname}
{$funcname}_FUNC_DEFN
#else
PGEN_FUNC_DEFN({$funcname})
#endif
{
#ifdef {$funcname}_SAVE_STATE
	// Save state in case of error
	WikiLex::State orig_state(wikilex);
#endif
	// Set start condition
	wikilex->setCondition({$funcstartconds[$funcname]});
	// Token variables
	const unsigned char * tok_text;
	int tok_len;
	int tok_type;
	// Get the next token
#ifdef {$funcname}_CONSUME_TOKEN
	tok_type = wikilex->nextToken(&tok_text, &tok_len);
#else
	tok_type = wikilex->peekToken(&tok_text, &tok_len);
#endif
	// Function-specific setup
#ifdef {$funcname}_SETUP
	{$funcname}_SETUP
#endif
	// Take action depending on token type
	switch(tok_type) {

ENDFUNCDEFN;
		
		// Token type switch cases
		foreach($tokspec as $tokname => $tokact) {
			if($tokname == 'default') $funcdefn .= "\t\tdefault:"; else $funcdefn .= "\t\tcase WikiLex::{$tokname}:";
			if(isset($tokact['error'])) if($tokact['error'] == TRUE) $funcdefn .= "\n\t\twikilex->restoreState(orig_state);\n\t\t";
			switch($tokact['action']) {
				case 'code':
					$funcdefn .= "\n\t\t{ {$tokact['code']} }\n\t\tbreak;\n";
					break;
				case 'macro':
					$macroname = $tokact['macroname'];
					if($macroname == FALSE) $macroname = 'HANDLE_' . $tokname;
					$funcdefn .= "\n{\n#ifdef {$funcname}_{$macroname}\n{$funcname}_{$macroname}\n#else\n{$macroname}\n#endif\n}\n\t\tbreak;\n";
					break;
			}
		}

		// Rest of function definition
		$funcdefn .= <<<ENDFUNCDEFN
	}
#ifdef {$funcname}_MAIN
{$funcname}_MAIN
#endif
}

ENDFUNCDEFN;
	}

	// Write function files
	file_put_contents($func_output, $funcdefn);
	file_put_contents($func_decl_output, $funcdecl);
}

/* Action that executes code when the token is found */
function act_code($code) {
	return array('action' => 'code', 'code' => $code);
}

/* Meta-action to remove an action from the base set when merging actions */
function act_except() {
	return FALSE;
}

function act_codeerr($code) {
	return array('action' => 'code', 'code' => $code, 'error' => TRUE);
}

function act_macro($macroname = FALSE) {
	return array('action' => 'macro', 'macroname' => $macroname);
}

function mergeact($base, $override) {
	$ret = $base;
	foreach($override as $tokname => $act) {
		if($act === act_except()) {
			if(isset($ret[$tokname])) unset($ret[$tokname]);
		} else {
			$ret[$tokname] = $act;
		}
	}
	return $ret;
}

/********** END PARSEGEN CODE ******************/

$error_action = act_macro('PARSE_RETURN_ERROR');
$text_action = act_macro('PARSE_RETURN_TEXT');

$default_actions = array(
	'CURLY_OPEN_SERIES'		=> act_macro('PARSE_CURLY_OPEN_SERIES'),
	'END'					=> $error_action,
	'TEXT'					=> $text_action
);

FuncSpec_Add('parse_structural_segment', mergeact($default_actions, array(
	'END'					=> act_macro('PARSE_RETURN_END'),
	'TEXT'					=> act_code('break;')
)));

FuncSpec_Add('find_template_open', array(
	'CURLY_OPEN_2'		=> act_macro('RETURN_TOKEN'),
	'END'			=> $default_actions['END'],
	'TEXT'			=> $error_action
));

Generate();

echo "ParserGen Done.\n";

?>
