#ifndef _BAYESPROCESSORS_HPP
#define _BAYESPROCESSORS_HPP

#include "standardprocessors.hpp"
#include "bayesdb.hpp"

namespace WPCluebot {


class BayesTrainDataCreator : public EditProcessor {
	public:
		BayesTrainDataCreator(libconfig::Setting & cfg) : EditProcessor(cfg) {
			std::string filename = (const char *)configuration["filename"];
			filestream.exceptions(std::ofstream::badbit | std::ofstream::failbit);
			filestream.open(filename.c_str());
		}
		
		void process(Edit & ed) {
			std::string wsprop = (const char *)configuration["words"];
			std::string isvandprop("isvandalism");
			if(configuration.exists("is_vandalism")) isvandprop = (const char *)configuration["is_vandalism"];
			WordSet wset = ed.getProp<WordSet>(wsprop);
			if(wset.size() < 1) return;
			int isvand_i = ed.getProp<bool>(isvandprop) ? 1 : 0;
			filestream << isvand_i << " _EDIT_TOTALS\n";
			for(WordSet::iterator it = wset.begin(); it != wset.end(); ++it) {
				filestream << isvand_i << " " << it->first << "\n";
			}
		}
	private:
		std::ofstream filestream;
};

class BayesScorer : public EditProcessor {
	public:
		BayesScorer(libconfig::Setting & cfg) : EditProcessor(cfg) {
			std::string dbfilename = (const char *)configuration["dbfile"];
			inprop = (const char *)configuration["input"];
			outprop = (const char *)configuration["output"];
			min_edits = 4;
			if(configuration.exists("min_edits")) min_edits = configuration["min_edits"];
			num_words = 20;
			if(configuration.exists("num_words")) num_words = configuration["num_words"];
			default_score = 0.0;
			if(configuration.exists("default_score")) default_score = configuration["default_score"];
			baydb.openDBForReading(dbfilename);
		}
		
		void process(Edit & ed) {
			/* First, calculate probability for each word in the list.
			 * Then, only use the top 20 most weighted words.  Calculated the magnitude of the "weight"
			 * by subtracting from 0.5 and taking the asbolute value. */
			// Pair is Magnitude of Weight, Probability
			WordSet words = ed.getProp<WordSet>(inprop);
			std::vector<std::pair<float, float> > probs;
			for(WordSet::const_iterator it = words.begin(); it != words.end(); ++it) {
				unsigned int good_cnt, bad_cnt;
				baydb.getWord(it->first, good_cnt, bad_cnt);
				if((good_cnt + bad_cnt) < min_edits) continue;
				float p = baydb.getWordVandalProb(good_cnt, bad_cnt, true);
				if(p < 0.0) continue;
				float m = 0.5 - p;
				if(m < 0.0) m = 0.0 - m;
				probs.push_back(std::pair<float,float>(m, p));
			}
			if(probs.size() < 1) {
				ed.setProp<float>(outprop, default_score);
				return;
			}
			// Sort
			std::sort(probs.begin(), probs.end());
			// Remove all but the top num_words
			if(probs.size() > num_words) {
				probs.erase(probs.begin(), probs.end() - num_words);
			}
			// Calculate total score
			double v_prob = 1.0;
			double l_prob = 1.0;
			for(std::vector<std::pair<float, float> >::iterator it = probs.begin(); it != probs.end(); ++it) {
				double d = (double)(*it).second;
				v_prob *= d;
				l_prob *= (1.0 - d);
			}
			double set_prob = v_prob / (v_prob + l_prob);
			ed.setProp<float>(outprop, (float)set_prob);
		}
		
	private:
		BayesDB baydb;
		std::string inprop;
		std::string outprop;
		int min_edits;
		int num_words;
		float default_score;
};


}

#endif

