#include <iostream>
#include <string>
#include <deque>
#include <boost/thread.hpp>
#include <boost/shared_ptr.hpp>
#include <libconfig.h++>
#include "framework.hpp"
#include "standardprocessors.hpp"
#include "bayesprocessors.hpp"
#include "xmleditloader.hpp"
#include "neuralnet.hpp"

using namespace WPCluebot;
using namespace std;
using namespace libconfig;

void printUsage(const char * name) {
	cout << "Usage: " << name << " -f <EditFile> [-m <ChainName>] [-c <ConfigDirectory>]\n";
	exit(1);
}

void addChainLink(EditProcessChain & procchain, const string & modulename, Setting & moduleconfig) {
	if(modulename == "character_counts") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new CharacterCounter(moduleconfig)));
	} else if(modulename == "edit_dump") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new EditDump(moduleconfig)));
	} else if(modulename == "print_progress") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new ProgressPrinter(moduleconfig)));
	} else if(modulename == "fast_string_search") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new FastStringSearch(moduleconfig)));
	} else if(modulename == "posix_regex_search") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new PosixRegexSearch(moduleconfig)));
	} else if(modulename == "posix_regex_replace") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new PosixRegexReplace(moduleconfig)));
	} else if(modulename == "misc_text_metrics") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new MiscTextMetrics(moduleconfig)));
	} else if(modulename == "character_replace") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new CharacterReplace(moduleconfig)));
	} else if(modulename == "word_separator") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new WordSeparator(moduleconfig)));
	} else if(modulename == "multi_word_separator") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new MultiWordSeparator(moduleconfig)));
	} else if(modulename == "wordset_diff") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new WordSetDiff(moduleconfig)));
	} else if(modulename == "wordset_compare") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new WordSetCompare(moduleconfig)));
	} else if(modulename == "misc_raw_word_metrics") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new MiscRawWordMetrics(moduleconfig)));
	} else if(modulename == "word_character_replace") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new WordCharacterReplace(moduleconfig)));
	} else if(modulename == "word_finder") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new WordFinder(moduleconfig)));
	} else if(modulename == "expression_eval") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new ExpressionEval(moduleconfig)));
	} else if(modulename == "float_set_creator") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new FloatSetCreator(moduleconfig)));
	} else if(modulename == "bayesian_training_data_creator") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new BayesTrainDataCreator(moduleconfig)));
	} else if(modulename == "bayesian_scorer") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new BayesScorer(moduleconfig)));
	} else if(modulename == "write_properties") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new WriteProperties(moduleconfig)));
	} else if(modulename == "write_ann_training_data") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new WriteAnnTrainingData(moduleconfig)));
	} else if(modulename == "run_ann") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new RunAnn(moduleconfig)));
	} else if(modulename == "trial_run_report") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new TrialRunReport(moduleconfig)));
	} else if(modulename == "charset_conv") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new CharsetConverter(moduleconfig)));
	} else if(modulename == "all_prop_charset_conv") {
		procchain.appendProcessor(boost::shared_ptr<EditProcessor>(new AllPropCharsetConverter(moduleconfig)));
	} else {
		throw std::runtime_error("Unknown module/chain link");
	}
}

class EditThreadPool {
	public:
		EditThreadPool(EditProcessChain & chainr, int nthreads = 3, int queue_size = 16) : chain(chainr) {
			max_queue_size = queue_size;
			stopflag = false;
			for(int i = 0; i < nthreads; ++i) {
				boost::shared_ptr<boost::thread> tptr(new boost::thread(boost::ref(*this)));
				threads.push_back(tptr);
			}
		}
		~EditThreadPool() {
			stopThreads();
		}
		
		void stopThreads() {
			if(threads.size()) {
				{
					boost::lock_guard<boost::mutex> lock(mut);
					stopflag = true;
				}
				thread_wait_cond.notify_all();
				for(std::vector<boost::shared_ptr<boost::thread> >::iterator it = threads.begin(); it != threads.end(); ++it) {
					(*it)->join();
				}
				threads.clear();
			}
		}
		
		void waitForAllDataProcessed() {
			boost::unique_lock<boost::mutex> lock(mut);
			while(editqueue.size() != 0) {
				main_wait_cond.wait(lock);
			}
		}
		
		void threadMain() {
			for(;;) {
				Edit ed;
				{
					boost::unique_lock<boost::mutex> lock(mut);
					while(editqueue.size() == 0 && !stopflag) {
						thread_wait_cond.wait(lock);
					}
					if(stopflag) return;
					ed = editqueue.back();
					editqueue.pop_back();
				}
				main_wait_cond.notify_one();
				chain.process(ed);
			}
		}
		void operator()() {
			threadMain();
		}
		
		void submitEdit(Edit & ed) {
			boost::unique_lock<boost::mutex> lock(mut);
			while(editqueue.size() >= max_queue_size) {
				main_wait_cond.wait(lock);
			}
			editqueue.push_front(ed);
			thread_wait_cond.notify_one();
		}
		
	private:
		boost::mutex mut;
		boost::condition_variable thread_wait_cond;
		boost::condition_variable main_wait_cond;
		std::deque<Edit> editqueue;
		bool stopflag;
		EditProcessChain & chain;
		int max_queue_size;
		std::vector<boost::shared_ptr<boost::thread> > threads;
};

void addConfigChain(EditProcessChain & procchain, Setting & configchain, Setting & linkcfgs, Setting & chaincfgs) {
	for(int i = 0; i < configchain.getLength(); ++i) {
		string chainelname = configchain[i];
		// Check if there's a config block for this chain element
		if(linkcfgs.exists(chainelname)) {
			Setting & linkcfg = linkcfgs[chainelname];
			string modulename = chainelname;
			if(linkcfg.exists("module")) modulename = (const char *)linkcfg["module"];
			addChainLink(procchain, modulename, linkcfg);
		} else if(chaincfgs.exists(chainelname)) {	// Check if it's the name of another chain
			addConfigChain(procchain, chaincfgs[chainelname], linkcfgs, chaincfgs);
		} else {	// Assume it's the name of a module without a configuration block
			Setting & blanksetting = linkcfgs.add(chainelname, Setting::TypeGroup);
			addChainLink(procchain, chainelname, blanksetting);
		}
	}
}

int main(int argc, char **argv) {
	string editfile;
	bool editsfromfile = false;
	string chainname = "default";
	string configdir = "./conf";
	
	if(argc < 2) printUsage(argv[0]);
	int opt;
	while((opt = getopt(argc, argv, "f:m:c:")) != -1) {
		switch(opt) {
			case 'f':
				editsfromfile = true;
				editfile.assign(optarg);
				break;
			case 'm':
				chainname.assign(optarg);
				break;
			case 'c':
				configdir.assign(optarg);
				break;
			default:
				printUsage(argv[0]);
		}
	}
	
	Config config;
	try {
		config.readFile((configdir + "/cluebotng.conf").c_str());
	} catch (const ParseException & e) {
		cerr << "Error parsing configuration file " << e.getFile() << " on line " << e.getLine() << ": " << e.getError() << "\n";
		return 1;
	}
	Setting & rootconfig = config.getRoot();
	
	EditProcessChain chain;
	
	if(!rootconfig.exists("chains")) throw std::runtime_error("Config file has no chains group.");
	Setting & configchains = rootconfig["chains"];
	if(!configchains.exists(chainname)) throw std::runtime_error("No such chain.");
	Setting & rootchaincfg = configchains[chainname];
	
	addConfigChain(chain, rootchaincfg, rootconfig, configchains);
	
	int num_edits = 0;
	if(editsfromfile) {
		if(!rootconfig.exists("xml_edit_parser")) throw std::runtime_error("No xml_edit_parser section of config.");
		XMLEditParser editparser(rootconfig["xml_edit_parser"]);
		editparser.parseFile_start(editfile);
	
#ifndef SINGLETHREAD
		int nthreads = 3;
		if(rootconfig.exists("threads")) nthreads = rootconfig["threads"];
		EditThreadPool tpool(chain, nthreads);
#endif
		
		while(editparser.parseFile_more()) {
			while(editparser.availableEdits()) {
				Edit ed = editparser.nextEdit();
				if(rootconfig.exists("require_properties")) {
					bool skipp = false;
					for(int p = 0; p < rootconfig["require_properties"].getLength(); ++p) {
						string pname = (const char *)rootconfig["require_properties"][p];
						if(!ed.hasProp(pname)) {
							skipp = true;
							break;
						}
					}
					if(skipp) continue;
				}
				if(editparser.parseFile_size()) {
					ed.setProp<unsigned long long int>("input_xml_file_size", editparser.parseFile_size());
					ed.setProp<unsigned long long int>("input_xml_file_pos", editparser.parseFile_pos());
				}
#ifdef SINGLETHREAD
				chain.process(ed);
#else
				tpool.submitEdit(ed);
#endif
				++num_edits;
			}
		}
#ifndef SINGLETHREAD
		tpool.waitForAllDataProcessed();
		tpool.stopThreads();
#endif
		chain.finished();
		cout << "Processed " << num_edits << " edits.\n";
	}
}
