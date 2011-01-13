
DEFINITIONS_TMP_FILE="./pgen_definitions.tmp"
RULES_TMP_FILE="./pgen_rules.tmp"
FUNCTIONS_TMP_FILE="./pgen_funcs.tmp"


export DEFINITIONS_TMP_FILE
export RULES_TMP_FILE
export FUNCTIONS_TMP_FILE


echo -n "" > $DEFINITIONS_TMP_FILE
echo -n "" > $RULES_TMP_FILE
echo -n "" > $FUNCTIONS_TMP_FILE


export TOKLIST=""
export CONDLIST=""


function add_token_def {
	TOKLIST="${TOKLIST} ${1}"
	echo "${1} ${2}" >> $DEFINITIONS_TMP_FILE
}


function add_condition {
	CONDLIST="${CONDLIST} ${1}"
	for i in $2; do
		echo "${i} ${1}" >> $RULES_TMP_FILE
	done
}



