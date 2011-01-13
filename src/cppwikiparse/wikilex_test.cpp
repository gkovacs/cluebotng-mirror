#include <iostream>
#include <string>
/* Flex internal structures, necessary for wikilex.ll.hpp */
#include "wikilex.yy.h"
/* WikiLex */
#include "wikilex.ll.hpp"

using namespace std;

int main() {
	string strbuf;
	{
		char cbuf[1024];
		while(!cin.eof()) {
			cin.read(cbuf, 1024);
			strbuf.append(cbuf, cin.gcount());
		}
	}
	WikiLex wikilex;
	wikilex.setFullBuffer(reinterpret_cast<const unsigned char *>(strbuf.c_str()), strbuf.size());
	WikiLex::State st_init;
	wikilex.getState(st_init);
	int tokcnt = 0;
	for(;;) {
		const unsigned char *bufpos;
		int len;
		int ttyp = wikilex.nextToken(&bufpos, &len);
		if(tokcnt == 0) wikilex.getState(st_init);
		tokcnt++;
		cout << "Token type=" << ttyp << ": ";
		string tstr((const char *)bufpos, (size_t)len);
		cout << tstr << "\n";
		if(ttyp == WikiLex::END) break;
	}
	cout << "FIRSTEND\n";
	wikilex.restoreState(st_init);
	for(;;) {
                const unsigned char *bufpos;
                int len;
                int ttyp = wikilex.nextToken(&bufpos, &len);
                cout << "Token type=" << ttyp << ": ";
                string tstr((const char *)bufpos, (size_t)len);
                cout << tstr << "\n";
                if(ttyp == WikiLex::END) break;
        }
}

