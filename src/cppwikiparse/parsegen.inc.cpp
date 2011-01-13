#ifdef parse_structural_segment_FUNC_DEFN
// Function definition for parse_structural_segment
parse_structural_segment_FUNC_DEFN
#else
PGEN_FUNC_DEFN(parse_structural_segment)
#endif
{
#ifdef parse_structural_segment_SAVE_STATE
	// Save state in case of error
	WikiLex::State orig_state(wikilex);
#endif
	// Set start condition
	wikilex->setCondition(COND1);
	// Token variables
	const unsigned char * tok_text;
	int tok_len;
	int tok_type;
	// Get the next token
#ifdef parse_structural_segment_CONSUME_TOKEN
	tok_type = wikilex->nextToken(&tok_text, &tok_len);
#else
	tok_type = wikilex->peekToken(&tok_text, &tok_len);
#endif
	// Function-specific setup
#ifdef parse_structural_segment_SETUP
	parse_structural_segment_SETUP
#endif
	// Take action depending on token type
	switch(tok_type) {
		case WikiLex::CURLY_OPEN_SERIES:
#ifdef parse_structural_segment_PARSE_CURLY_OPEN_SERIES
parse_structural_segment_PARSE_CURLY_OPEN_SERIES
#else
PARSE_CURLY_OPEN_SERIES
#endif
		break;
		case WikiLex::END:
#ifdef parse_structural_segment_PARSE_RETURN_END
parse_structural_segment_PARSE_RETURN_END
#else
PARSE_RETURN_END
#endif
		break;
		case WikiLex::TEXT:
		{ break; }
		break;
	}
#ifdef parse_structural_segment_MAIN
parse_structural_segment_MAIN
#endif
}
#ifdef find_template_open_FUNC_DEFN
// Function definition for find_template_open
find_template_open_FUNC_DEFN
#else
PGEN_FUNC_DEFN(find_template_open)
#endif
{
#ifdef find_template_open_SAVE_STATE
	// Save state in case of error
	WikiLex::State orig_state(wikilex);
#endif
	// Set start condition
	wikilex->setCondition(COND2);
	// Token variables
	const unsigned char * tok_text;
	int tok_len;
	int tok_type;
	// Get the next token
#ifdef find_template_open_CONSUME_TOKEN
	tok_type = wikilex->nextToken(&tok_text, &tok_len);
#else
	tok_type = wikilex->peekToken(&tok_text, &tok_len);
#endif
	// Function-specific setup
#ifdef find_template_open_SETUP
	find_template_open_SETUP
#endif
	// Take action depending on token type
	switch(tok_type) {
		case WikiLex::CURLY_OPEN_2:
#ifdef find_template_open_RETURN_TOKEN
find_template_open_RETURN_TOKEN
#else
RETURN_TOKEN
#endif
		break;
		case WikiLex::END:
#ifdef find_template_open_PARSE_RETURN_ERROR
find_template_open_PARSE_RETURN_ERROR
#else
PARSE_RETURN_ERROR
#endif
		break;
		case WikiLex::TEXT:
#ifdef find_template_open_PARSE_RETURN_ERROR
find_template_open_PARSE_RETURN_ERROR
#else
PARSE_RETURN_ERROR
#endif
		break;
	}
#ifdef find_template_open_MAIN
find_template_open_MAIN
#endif
}
