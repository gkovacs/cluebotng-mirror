#ifndef _FRAMEWORK_HPP
#define _FRAMEWORK_HPP

#include <map>
#include <vector>
#include <string>
#include <stdexcept>
#include <iostream>
#include <boost/any.hpp>
#include <typeinfo>
#include <libconfig.h++>
#include <boost/shared_ptr.hpp>
#include <typeinfo>
#include <sstream>
#include <stdio.h>

namespace WPCluebot {

class PropertySet {
	public:
		class NoSuchProperty : public std::runtime_error {
			public: NoSuchProperty(const std::string & name) : std::runtime_error(std::string("No such property: ") + name) {}
		};

		bool hasProp(const std::string & name) {
			return properties.count(name);
		}

		template <class T>
		T getProp(const std::string & name) {
			if(properties.count(name) == 0) throw NoSuchProperty(name);
			return boost::any_cast<T>(properties[name]);
		}
		
		template <class T>
		T * getPropPtr(const std::string & name) {
			if(properties.count(name) == 0) return NULL;
			return boost::any_cast<T>(&(properties[name]));
		}

		template <class T>
		void setProp(const std::string & name, const T & value) {
			properties[name] = value;
		}

		std::string xmlEscapeString(const std::string & str) {
			std::stringstream sstrm;
			for(std::string::const_iterator it = str.begin(); it != str.end(); ++it) {
				if(*it == '&') sstrm << "&amp;"; else
				if(*it == '<') sstrm << "&lt;"; else
				if(*it == '>') sstrm << "&gt;"; else
				if((*it & 0x80) == 0) sstrm << *it;
			}
			return sstrm.str();
		}

		std::string propToString(boost::any & a) {
			const std::type_info & ti = a.type();
			std::stringstream sstrm;
			if(ti == typeid(int)) {
				sstrm << boost::any_cast<int>(a);
				return sstrm.str();
			}
			if(ti == typeid(unsigned long long int)) {
				sstrm << boost::any_cast<unsigned long long int>(a);
				return sstrm.str();
			}
			if(ti == typeid(float)) {
				sstrm << boost::any_cast<float>(a);
				return sstrm.str();
			}
			if(ti == typeid(std::string)) {
				return boost::any_cast<std::string>(a);
			}
			if(ti == typeid(bool)) {
				if(boost::any_cast<bool>(a)) return "true"; else return "false";
			}
			if(ti == typeid(std::map<std::string,int>)) {
				sstrm << "WordSet:\n";
				std::map<std::string,int> wset = boost::any_cast<std::map<std::string,int> >(a);
				for(std::map<std::string,int>::iterator it = wset.begin(); it != wset.end(); ++it) {
					sstrm << it->first << ": " << it->second << "\n";
				}
				return sstrm.str();
			}
			if(ti == typeid(std::vector<float>)) {
				sstrm << "FloatSet:\n";
				std::vector<float> fset = boost::any_cast<std::vector<float> >(a);
				for(std::vector<float>::iterator fit = fset.begin(); fit != fset.end(); ++fit) sstrm << *fit << "\n";
				return sstrm.str();
			}
			return "(unknown type)";
		}

		void dump(std::ostream & strm, int maxlen = -1) {
			for(std::map<std::string,boost::any>::iterator it = properties.begin(); it != properties.end(); ++it) {
				std::string str = propToString(it->second);
				if(maxlen >= 0) {
					if(str.size() > maxlen) {
						str = str.substr(0, maxlen - 4) + " ...";
					}
				}
				strm << "<" << it->first << ">" << xmlEscapeString(str) << "</" << it->first << ">\n";
			}
		}
		
		std::map<std::string,double> getDoubleMap() {
			std::map<std::string, double> dvarmap;
			for(std::map<std::string,boost::any>::iterator it = properties.begin(); it != properties.end(); ++it) {
				const std::type_info & ti = it->second.type();
				if(ti == typeid(int)) {
					dvarmap[it->first] = (double)boost::any_cast<int>(it->second);
				} else if(ti == typeid(float)) {
					dvarmap[it->first] = (double)boost::any_cast<float>(it->second);
				} else if(ti == typeid(unsigned long long int)) {
					dvarmap[it->first] = (double)boost::any_cast<unsigned long long int>(it->second);
				} else if(ti == typeid(std::string)) {
					dvarmap[it->first + "_size"] = (double)(boost::any_cast<std::string>(it->second)).size();
				} else if(ti == typeid(std::map<std::string,int>)) {
					dvarmap[it->first + "_size"] = (double)(boost::any_cast<std::map<std::string,int> >(it->second)).size();
				} else if(ti == typeid(bool)) {
					bool b = boost::any_cast<bool>(it->second);
					dvarmap[it->first] = b ? 1.0 : 0.0;
				} else if(ti == typeid(std::vector<float>)) {
					std::vector<float> fvec = boost::any_cast<std::vector<float> >(it->second);
					if(fvec.size() < 512) {
						char numbuf[16];
						int it2_i = 0;
						for(std::vector<float>::iterator it2 = fvec.begin(); it2 != fvec.end(); ++it2) {
							sprintf(numbuf, "%d", it2_i);
							it2_i++;
							dvarmap[it->first + "_" + numbuf] = *it2;
						}
					}
				}
			}
			return dvarmap;
		}

		std::map<std::string,boost::any> properties;
};

typedef PropertySet Edit;

class EditProcessor {
	public:
		EditProcessor(libconfig::Setting & cfgs) : configuration(cfgs) {}
		virtual void process(Edit & ed) = 0;
		virtual ~EditProcessor() {}
		virtual void finished() {}
	protected:
		libconfig::Setting & configuration;
};

class TextProcessor : public EditProcessor {
	public:
		TextProcessor(libconfig::Setting & cfg) : EditProcessor(cfg) {}
		~TextProcessor() {}
		
		void process(Edit & ed) {
			libconfig::Setting & io = configuration["inputs"];
			for(int i = 0; i < io.getLength(); ++i) {
				std::string propname = io[i].getName();
				std::string outpfx = io[i];
				if(ed.hasProp(propname)) processText(ed, ed.getProp<std::string>(propname), outpfx);
			}
		}
		
		virtual void processText(Edit & ed, const std::string & text, const std::string & proppfx) = 0;
};

class WordSetProcessor : public EditProcessor {
	public:
		WordSetProcessor(libconfig::Setting & cfg) : EditProcessor(cfg) {}
		~WordSetProcessor() {}
		
		void process(Edit & ed) {
			libconfig::Setting & io = configuration["inputs"];
			for(int i = 0; i < io.getLength(); ++i) {
				std::string propname = io[i].getName();
				std::string outpfx = io[i];
				processWordSet(ed, ed.getProp<std::map<std::string, int> >(propname), outpfx);
			}
		}
		
		virtual void processWordSet(Edit & ed, const std::map<std::string,int> & wordset, const std::string & proppfx) = 0;
};

class EditProcessChain {
	public:
		void appendProcessor(boost::shared_ptr<EditProcessor> p) {
			processors.push_back(p);
		}
		void process(Edit & ed) {
			for(std::vector<boost::shared_ptr<EditProcessor> >::iterator it = processors.begin(); it != processors.end(); ++it) {
				(*it)->process(ed);
			}
		}
		void finished() {
			for(std::vector<boost::shared_ptr<EditProcessor> >::iterator it = processors.begin(); it != processors.end(); ++it) {
				(*it)->finished();
			}
		}
	private:
		std::vector<boost::shared_ptr<EditProcessor> > processors;
};

}

#endif

