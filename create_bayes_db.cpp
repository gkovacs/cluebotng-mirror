#include <iostream>
#include <fstream>
#include <string>
#include "bayesdb.hpp"

using namespace std;
using namespace WPCluebot;

int main(int argc, char **argv) {
	if(argc != 3) {
		cout << "Usage: " << argv[0] << " <BayesianDatabaseFile> <TrainingDataFile>\n";
		return 1;
	}
	string trainfilename(argv[2]);
	string dbfilename(argv[1]);
	ifstream trainfile;
	trainfile.open(trainfilename.c_str());
	BayesDB baydb;
	baydb.createNew(dbfilename);
	unsigned int i = 0;
	cout << "Processing words ...\n";
	while(!trainfile.eof() && !trainfile.bad() && !trainfile.fail()) {
		int isvand;
		string word;
		trainfile >> isvand;
		trainfile >> word;
		baydb.addWord(word, isvand);
		if(word == "_EDIT_TOTALS") {
			++i;
			if(i % 100 == 0) cout << i << "\n";
		}
	}
	cout << "Pruning ...\n";
	baydb.pruneDB(3, 0.25);
}

