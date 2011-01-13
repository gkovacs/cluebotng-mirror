#include <iostream>
#include <string>
/* This needs to be defined to get access to the start condition defines */
#define YY_HEADER_EXPORT_START_CONDITIONS
/* Flex internal structures, necessary for wikilex.ll.hpp */
#include "wikilex.yy.h"
/* WikiLex */
#include "wikilex.ll.hpp"
/* WikiParse header */
#include "wikiparse.hpp"

using namespace std;

namespace WikiParse {


ParseTree Parser::parse(const char * buf, int size) {
	/* Set up the lexer.  A pointer to it is set in the parser class, but it is only accessed when it is within local scope. */
	WikiLex wlex((const unsigned char *)buf, size);
	wikilex = &wlex;
	/* Parse */
	return parse_main();
}

Node::Ptr Parser::parse_main() {
	/* Declare stuff to return */
	NodeSeries * retseries = new NodeSeries;
	Node::Ptr retptr(dynamic_cast<Node *>(retseries));
	/* Set initial condition */
	wikilex->setCondition(COND_ARTICLESTART);
	/* Parse first node.  This is the only node that can be a redirect. */
	{ Node::Ptr nodep; if(parse_structural_segment(nodep)) retseries->nodes.push_back(nodep); }
	/* Set condition to main - only difference is it doesn't parse redirects */
	wikilex->setCondition(COND_MAIN);
	/* Keep parsing stuff from the lexer until EOF is returned, then we're done */
	for(Node::Ptr nodep; parse_structural_segment(nodep); retseries->nodes.push_back(nodep));
	condenseAdjacentTextNodes(retseries->nodes);
	condenseAdjacentSeries(retseries->nodes);
	/* Return */
	return retptr;
}

/* Converts all TextRef nodes to Text nodes, and also condenses adjacent text nodes to a single node */
void Parser::condenseAdjacentTextNodes(NodeSeries::NodeList & nodelist) {
	bool lastwastext = false;
	Text * lasttext;
	NodeSeries::NodeList::iterator next;
	for(NodeSeries::NodeList::iterator node = nodelist.begin(); node != nodelist.end(); node = next) {
		next = node; ++next;
		if(typeid(**node) == typeid(Text)) {
			Text & ctext = dynamic_cast<Text &>(**node);
			if(lastwastext) {
				lasttext->text += ctext.text;
				nodelist.erase(node);
			} else {
				lasttext = &ctext;
				lastwastext = true;
			}
		} else if(typeid(**node) == typeid(TextRef)) {
			TextRef & ctext = dynamic_cast<TextRef &>(**node);
			if(lastwastext) {
				lasttext->text.append(ctext.text, ctext.textsize);
				nodelist.erase(node);
			} else {
				*node = boost::shared_ptr<Node>(dynamic_cast<Node *>(lasttext = new Text(ctext.text, ctext.textsize)));
				lastwastext = true;
			}
		} else lastwastext = false;
	}
}

/* If there are wrapper or series that are adjacent with the same type,
   condenses them into one wrapper/series. */
void Parser::condenseAdjacentSeries(NodeSeries::NodeList & nodelist) {
	// TODO: Make Wrappers work
	NodeSeries * lastseries = NULL;
	NodeSeries::NodeList::iterator next;
	/* Find instances of NodeSeries inside this NodeList and splice the sub-series nodes into this list */
	for(NodeSeries::NodeList::iterator node = nodelist.begin(); node != nodelist.end(); node = next) {
		next = node; ++next;
		if(typeid(**node) == typeid(NodeSeries)) {
			NodeSeries & cseries = dynamic_cast<NodeSeries &>(**node);
			nodelist.splice(node, cseries.nodes);
			nodelist.erase(node);
		}
	}
	/* Find instances of one NodeSeries after another, and append the second one to the first */
	for(NodeSeries::NodeList::iterator node = nodelist.begin(); node != nodelist.end(); node = next) {
		next = node; ++next;
		if(typeid(**node) == typeid(NodeSeries)) {
			NodeSeries & cseries = dynamic_cast<NodeSeries &>(**node);
			if(lastseries) {
				lastseries->nodes.splice(lastseries->nodes.end(), cseries.nodes);
				nodelist.erase(node);
			} else {
				lastseries = &cseries;
			}
		} else {
			lastseries = NULL;
		}
	}
}

/* Parse the structural segment starting at the scanner's current position.
   If the parser is at a token with no special significance, like plain text, just return it as text.
   Returns false if parser is positioned on an EOF (and no node), and true otherwise */
bool Parser::parse_structural_segment(Node::Ptr & retnode, int flags) {
	// Peek at the next token without discarding it
	const unsigned char * tok_text;
	int tok_len;
	int tok_type;
	tok_type = wikilex->peekToken(&tok_text, &tok_len);
	// Check for EOF
	if(tok_type == WikiLex::END) return false;
	// Hand off token to subparser
	switch(tok_type) {
		case WikiLex::CURLY_OPEN_SERIES: if(parse_curly_braces(retnode, tok_len)) return true; else break;
	}
	// If haven't returned by now, a sub-parser didn't handle it, or errored.
	// Treat token as text.
	// It is assumed that, if a sub-parser failed, the sub-parser positions the lexer at its original position
	retnode = boost::shared_ptr<Node>(dynamic_cast<Node *>(new TextRef(tok_text, tok_len)));
	// We only peek'd the token before, now advance to the next one
	wikilex->nextToken();
	// EOF was checked for earlier, so return true
	return true;
}

/* Parses element(s) starting at a series of open curly braces */
bool Parser::parse_curly_braces(Node::Ptr & retnode, int num_curlies, int flags) {
	// Save current state in case parsing fails and we have to bomb (in which case original state must be restored)
	WikiLex::State orig_state(wikilex);
	// 1 curly is a literal curly.  2 is potentially a template.  3 is potentially a template argument,
	// or a literal curly and a template.  4 is a literal curly and then 3 others.  5 is potentially a template
	// (with a name that's a template arg), potentially 2 literal curlies and a template arg, or potentially
	// 3 literal curlies and a template, or potentially 5 literal curlies.  More than 5 is (x-5) literal curlies
	// and then 5 others.
	// Switch on number of curlies
	switch(num_curlies) {
		case 1:
			// Single literal curly, just plain text.  Return false because it's nothing special.
			// No need to reset state, no state has been modified
			return false;
		case 2:
			// 2 curly braces is either a template or a parse error.  First try parsing as template.
			if(parse_template(retnode)) return true;
			// If template parsing failed, that function should have restored state, so no need to do it again
			return false;
	}
}

bool Parser::parse_template(Node::Ptr & retnode, int flags) {
	// Save current state in case parsing fails and we have to bomb (in which case original state must be restored)
	WikiLex::State orig_state(wikilex);
	// Switch to 2-curly-find mode to skip past the two curlies
	wikilex->setCondition(COND_FIND2OPENCURLY);
	if(wikilex->nextToken() != WikiLex::CURLY_OPEN_SERIES) { wikilex->restoreState(orig_state); return false; }
	// Set up the template to return
	Template * tmpl = new Template;
	retnode = Node::Ptr(dynamic_cast<Node *>(tmpl));
	// Parse the template name
	{
		// Template names are text and/or template arg substitutions
		Node::Ptr rnode;
		wikilex->setCondition(COND_MODE_TEMPLATENAME);
		bool tmpl_close = false;
		for(bool go = true; go;) switch(wikilex->peekToken()) {
			case WikiLex::TEMPLATE_CLOSE:
			tmpl_close = true;
			case WikiLex::PIPE:
			go = false;
			break;
			default:
			if(!parse_structural_segment(
		}
	}
	
}



}

using namespace WikiParse;

int main() {
	string strbuf;
        {
                char cbuf[1024];
                while(!cin.eof()) {
                        cin.read(cbuf, 1024);
                        strbuf.append(cbuf, cin.gcount());
                }
        }
	cout << "READ INPUT.\nPARSING.\n\n";
	Parser parser;
	ParseTree parsetree = parser.parse(strbuf);
	parsetree->print();
	return 0;
}



