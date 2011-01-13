#ifndef _PARSEGEN_DEFINES_HPP
#define _PARSEGEN_DEFINES_HPP


#define PGEN_FUNC_DECL(funcname) int funcname(Node::Ptr & retnode, int flags)
#define PGEN_FUNC_DEFN(funcname) int Parser::##funcname(Node::Ptr & retnode, int flags)

#define PARSE_RETSTATUS_END -3
#define PARSE_RETSTATUS_NODE -2
#define PARSE_RETSTATUS_ERROR -1

#define PARSE_RETURN_END return PARSE_RETSTATUS_END
#define PARSE_RETURN_TEXT  { retnode = boost::shared_ptr<Node>(dynamic_cast<Node *>(new TextRef(tok_text, tok_len))); wikilex->nextToken(); return PARSE_RETSTATUS_NODE; }


#endif

