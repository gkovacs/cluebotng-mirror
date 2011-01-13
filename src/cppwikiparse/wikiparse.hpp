#ifndef _WIKIPARSE_HPP
#define _WIKIPARSE_HPP

#include "wikiparse_types.hpp"

/* Declaration needed for pointer */
struct WikiLex;

namespace WikiParse {


/* A new parser needs to be instantiated for each article to be parsed. */

class Parser {
	public:
		ParseTree parse(const char * buf, int size);
		ParseTree parse(const std::string & str) {
			return parse(str.c_str(), str.size());
		}

	private:
		
		WikiLex * wikilex;

		Node::Ptr parse_main();
		bool parse_structural_segment(Node::Ptr & retnode, int flags = 0);
		void condenseAdjacentTextNodes(NodeSeries::NodeList & nodelist);
		void condenseAdjacentSeries(NodeSeries::NodeList & nodelist);
		void trimWhitespace(NodeSeries::NodeList & nodelist);
		
		bool parse_curly_braces(Node::Ptr & retnode, int num_curlies, int flags = 0);
		bool parse_template(Node::Ptr & retnode, int flags = 0);
};


}

#endif

