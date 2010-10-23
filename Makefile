CFLAGS=-g -O2

all: cluebotng create_ann create_bayes_db print_bayes_db

cluebotng: bayesdb.hpp framework.hpp xmleditloader.hpp bayesprocessors.hpp faststringops.hpp neuralnet.hpp standardprocessors.hpp main.cpp
	g++ $(CFLAGS) main.cpp -lexpat -lmatheval -ldb_cxx -liconv -lfann -lconfig++ -lm -ocluebotng

create_ann: create_ann.cpp
	g++ ${CFLAGS} create_ann.cpp -lfann -ocreate_ann

create_bayes_db: create_bayes_db.cpp bayesdb.hpp
	g++ ${CFLAGS} create_bayes_db.cpp -ldb_cxx -ocreate_bayes_db

print_bayes_db: print_bayes_db.cpp bayesdb.hpp
	g++ ${CFLAGS} print_bayes_db.cpp -ldb_cxx -oprint_bayes_db

clean:
	rm -f cluebotng create_bayes_db print_bayes_db create_ann




TRAINING_SET=./editsets/C/train.xml
TRIAL_SET=./editsets/C/trial.xml

bayes_db:
	@echo Creating bayesian training set
	./cluebotng -f $(TRAINING_SET) -m create_bayes_train
	@echo Creating bayesian database
	./create_bayes_db ./data/bayes.db ./data/main_bayes_train.dat

ann_train:
	@echo Creating ANN training set
	./cluebotng -f $(TRAINING_SET) -m create_ann_train
	@echo Training ANN
	# ANNFile TrainFile MaxEpochs DesiredError HiddenLayerSize
	./create_ann ./data/main_ann.fann ./data/main_ann_train.dat 90 0.05 100

trial:
	@echo Performing trial
	./cluebotng -f $(TRIAL_SET) -m trial_run

train: bayes_db ann_train

trainandtrial: train trial

anntrainandtrial: ann_train trial

