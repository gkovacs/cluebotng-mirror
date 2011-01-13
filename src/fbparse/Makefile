# Taken from CVS:phase3/extensions/wikidiff/Makefile and modified

all: libfbwikiparse.a

libfbwikiparse.a : lex.yy.c wikiparse.tab.c parsetree.c parsetree.h
	gcc -c wikiparse.tab.c
	gcc -c parsetree.c
	gcc -c lex.yy.c
	ar rcs libfbwikiparse.a wikiparse.tab.o parsetree.o lex.yy.o

lex.yy.c : wikilex.l
	flex wikilex.l

wikiparse.tab.c : wikiparse.y
	bison -d wikiparse.y

clean :
	rm -f libfbwikiparse.a
	rm -f wikiparse.tab.c
	rm -f wikiparse.tab.h
	rm -f lex.yy.c
	rm -f *.o

