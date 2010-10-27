#!/bin/bash
DIR="`dirname $0`"

. "${DIR}/toolconfig"

echo "Creating ANN training data:"
${BINARY_DIR}/cluebotng -f "$TRAINFILE" -m ann_train
echo "Training ANN:"
${BINARY_DIR}/create_ann ${DATA_DIR}/main_ann.fann ${DATA_DIR}/main_ann_train.dat ${ANN_MAX_EPOCHS} ${ANN_TARGET_ERROR} ${ANN_SIZE}

