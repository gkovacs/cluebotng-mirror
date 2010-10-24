CFLAGS=-g -O2 -march=native -msse4 -I/usr/include/db4.8
#CFLAGS = -g

all: cluebotng create_ann create_bayes_db print_bayes_db

cluebotng: bayesdb.hpp framework.hpp xmleditloader.hpp bayesprocessors.hpp faststringops.hpp neuralnet.hpp standardprocessors.hpp main.cpp
	g++ $(CFLAGS) main.cpp -lexpat -lmatheval -ldb_cxx -liconv -lfann -lconfig++ -lboost_thread -lboost_system -lm -ocluebotng

create_ann: create_ann.cpp
	g++ ${CFLAGS} create_ann.cpp -lfann -ocreate_ann

create_bayes_db: create_bayes_db.cpp bayesdb.hpp
	g++ ${CFLAGS} create_bayes_db.cpp -ldb_cxx -ocreate_bayes_db

print_bayes_db: print_bayes_db.cpp bayesdb.hpp
	g++ ${CFLAGS} print_bayes_db.cpp -ldb_cxx -oprint_bayes_db

clean:
	rm -f cluebotng create_bayes_db print_bayes_db create_ann

svn: svnup clean all bayes_db ann_train_data

svnup:
	svn up



TRAINING_SET=./editsets/C3/train.xml
TRIAL_SET=./editsets/C3/trial.xml

bayes_db:
	@echo Creating Bayesian training sets
	./cluebotng -f $(TRAINING_SET) -m create_bayes_train
	@echo Creating main Bayesian database
	./create_bayes_db ./data/bayes.db ./data/main_bayes_train.dat
	@echo Creating 2-Bayesian database
	./create_bayes_db ./data/two_bayes.db ./data/two_bayes_train.dat

ann_train_only:
	@echo Training ANN
	./create_ann ./data/main_ann.fann ./data/main_ann_train.dat 100 0.037 300

ann_train_only___auto:
	@echo Training ANN with ${PARAMS}
	./create_ann ./data/main_ann.fann ./data/main_ann_train.dat ${PARAMS}

ann_train_data:
	@echo Creating ANN training set
	./cluebotng -f $(TRAINING_SET) -m create_ann_train

ann_auto_train:
	@echo AutoTraining ANN
	php util/autotraintrial.php ${PARAMS}

ann_train: ann_train_data ann_train_only

trial:
	@echo Performing trial
	./cluebotng -f $(TRIAL_SET) -m trial_run

train: bayes_db ann_train

trainandtrial: train trial

anntrainandtrial: ann_train trial

