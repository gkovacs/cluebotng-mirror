all: wikilex.yy.cpp
	# g++ -g wikilex.yy.cpp wikilex_test.cpp -o wikilex_test
	# g++ -g wikilex.yy.cpp wikiparse.cpp -o wikiparse

wikilex.yy.cpp: wikilex.ll
	flex wikilex.ll

wikilex.ll: wikilex.ll.template parsergen.php
	php parsergen.php >/dev/null

clean:
	rm -f *.o wikilex.yy.h wikilex.yy.cpp wikilex_test wikiparse parsegen*.inc.cpp parsegen*.inc.hpp wikilex.ll
