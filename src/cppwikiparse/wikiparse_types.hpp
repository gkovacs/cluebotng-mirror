#ifndef _WIKIPARSE_TYPES
#define _WIKIPARSE_TYPES

#include <iostream>
#include <string>
#include <list>
#include <utility>
#include <sstream>
#include <typeinfo>
#include <boost/shared_ptr.hpp>


namespace WikiParse {


/* Base class for all parse tree nodes, including the root node */
class Node {
	public:
		typedef boost::shared_ptr<Node> Ptr;

		~Node() {}

		virtual std::string getText(int flags = 0) {
			return "";
		}

		virtual void print() {}
};

typedef Node::Ptr ParseTree;

/* Class for a series of nodes */
class NodeSeries : public Node {
	public:
		typedef std::list<Node::Ptr> NodeList;
		NodeList nodes;

		std::string getText(int flags = 0) {
			std::stringstream sstrm;
			for(NodeList::iterator it = nodes.begin(); it != nodes.end(); ++it) sstrm << (**it).getText(flags);
			return sstrm.str();
		}

		void print() {
			std::cout << "NodeSeries {\n";
			for(NodeList::iterator it = nodes.begin(); it != nodes.end(); ++it) (**it).print();
			std::cout << "}\n";
		}
};

/* Text that references an external buffer and is not freed itself */
class TextRef : public Node {
	public:
		const char *text;
		int textsize;

		TextRef(const char * t = NULL, int s = 0) : text(t), textsize(s) {}
		TextRef(const unsigned char * t, int s) : text((const char *)t), textsize(s) {}

		std::string getText(int flags = 0) {
			return std::string(text, textsize);
		}

		void print() {
			std::cout << "Text: { \"" << getText() << "\" }\n";
		}
};

/* Independent text */
class Text : public Node {
	public:
		std::string text;

		Text(const std::string & s) : text(s) {}
		Text(const char * t, int s) : text(t, s) {}
		Text(const unsigned char * t, int s) : text((const char *)t, s) {}
		Text() {}

		std::string getText(int flags = 0) {
			return text;
		}

		void print() {
			std::cout << "Text: { \"" << text << "\" }\n";
		}
};

/* An element that contains no information but other nodes */
class NodeWrapper : public Node {
	public:
		Node::Ptr wrapped;
		enum WrapType { UNKNOWN, NOWIKI, COMMENT };
		WrapType wraptype;

		NodeWrapper(WrapType t = UNKNOWN) : wraptype(t) {}
		NodeWrapper(Node::Ptr w, WrapType t) : wrapped(w), wraptype(t) {}

		std::string wrapTypeStr(WrapType t) {
			switch(t) {
				case UNKNOWN: return "Unknown";
				case NOWIKI: return "NoWiki";
				case COMMENT: return "Comment";
			}
			std::stringstream sstrm;
			sstrm << "Wrapper_" << (int)t;
			return sstrm.str();
		}
		std::string wrapTypeStr() { return wrapTypeStr(wraptype); }

		std::string getText(int flags = 0) {
			return wrapped->getText(flags);
		}

		void print() {
			std::cout << wrapTypeStr() << " { ";
			wrapped->print();
			std::cout << " }\n";
		}
};

class Template : public Node {
	public:
		Node::Ptr name;
		typedef std::list<std::pair<Node::Ptr,Node::Ptr> > ArgList;
		ArgList args;

		std::string getText(int flags = 0) {
			std::stringstream sstrm;
			for(ArgList::iterator it = args.begin(); it != args.end(); ++it) sstrm << it->second->getText(flags) << "\n";
			return sstrm.str();
		}

		void print() {
			std::cout << "Template { Name { ";
			name->print();
			std::cout << " } Arguments";
			for(ArgList::iterator it = args.begin(); it != args.end(); ++it) {
				std::cout << " { Name { ";
				it->first->print();
				std::cout << " } Value { ";
				it->second->print();
				std::cout << " } }";
			}
			std::cout << " }\n";
		}
};


}

#endif

