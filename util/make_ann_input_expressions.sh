#!/bin/bash

ALLNAMES=""

function addallnames {
	ALLNAMES="${ALLNAMES}\"${1}\","
	ALLNAMES="${ALLNAMES} `echo`"
}

function word_prop {
	echo "ann_${1} = \"added_${1} / added_word_count / (previous_${1} / previous_word_count + 1.0)\";"
	echo "ann_removed_${1} = \"removed_${1} / removed_word_count / (previous_${1} / previous_word_count + 1.0)\";"
	addallnames "ann_${1}"
	addallnames "ann_removed_${1}"
}

function basic_added_word_prop {
	echo "ann_${1} = \"${1} / added_word_count\";"
	addallnames "ann_${1}"
}

function linear_scale {
	HIGH=$3
	LOW=$2
	M="`echo "scale=8;1/(${HIGH}-${LOW})" | bc`"
	B="`echo "scale=8;0-${M}*${LOW}" | bc`"
	echo "ann_${1} = \"${M} * ${1} + ${B}\";"
	addallnames "ann_${1}"
}

function diff_linear_scale {
	HIGH=$3
	LOW=$2
	M="`echo "scale=8;1/(${HIGH}-${LOW})" | bc`"
	B="`echo "scale=8;0-${M}*${LOW}" | bc`"
	echo "ann_${1} = \"${M} * (current_${1} - previous_${1}) + ${B}\";"
	addallnames "ann_${1}"
}

function log_scale {
	HIGH=$3
	LOW=$2
	J="`echo "scale=8;(0.1 * (${HIGH} - ${LOW}) - ${LOW} * 0.9) / (0.1 * (${HIGH} - ${LOW}))" | bc`"
	K="`echo "scale=8;0.9 / (0.1 * (${HIGH} - ${LOW}))" | bc`"
	echo "ann_${1} = \"1 - 1 / (${K} * ${1} + ${J})\";"
	addallnames "ann_${1}"
}

function spec_log_scale {
	HIGH=$4
	LOW=$3
	J="`echo "scale=8;(0.1 * (${HIGH} - ${LOW}) - ${LOW} * 0.9) / (0.1 * (${HIGH} - ${LOW}))" | bc`"
	K="`echo "scale=8;0.9 / (0.1 * (${HIGH} - ${LOW}))" | bc`"
	echo "${2} = \"1 - 1 / (${K} * ${1} + ${J})\";"
	addallnames "${2}"
}

function age_scale {
	spec_log_scale "(current_timestamp - ${1})" "ann_${1}" 0 31536000
}

function diff_charcount {
	echo "ann_${1}_add = \"(current_${1} - previous_${1}) / current_text_size\";"
	echo "ann_${1}_rem = \"(previous_${1} - current_${1}) / previous_text_size\";"
	echo "ann_${1}_cnt = \"(current_${1} - previous_${1}) / 10\";"
	addallnames "ann_${1}_add"
	addallnames "ann_${1}_rem"
	addallnames "ann_${1}_cnt"
}

function boolean {
	echo "ann_${1} = \"${1}\";"
	addallnames "ann_${1}"
}

function exact {
	echo "ann_${1} = \"${1}\";"
	addallnames "ann_${1}"
}


word_prop all_lcase_word_count
word_prop all_ucase_word_count
word_prop common_words
word_prop distinct_word_count
word_prop first_ucase_word_count
word_prop max_word_repeats
word_prop middle_ucase_word_count
word_prop novowels_word_count
word_prop numeric_word_count
word_prop part_numeric_word_count
word_prop sex_words
word_prop swear_words
word_prop acceptable_allcaps_words
# basic_added_word_prop added_reused_words

diff_charcount alpha_surrounded_digit_count
diff_charcount alpha_surrounded_punctuation_count
diff_charcount charcount_at
diff_charcount charcount_bracket
diff_charcount charcount_comma
diff_charcount charcount_exclamationpoint
diff_charcount charcount_period
diff_charcount charcount_qmark
diff_charcount charcount_rawcapitals
diff_charcount charcount_rawdigit
diff_charcount charcount_rawlowercase
diff_charcount charcount_space
diff_charcount charcount_wikichar

diff_linear_scale extlink_count 0 6
diff_linear_scale html_count 0 16
diff_linear_scale punctuation_series_count 0 32
diff_linear_scale uncapitalized_sentence_count 0 16
diff_linear_scale unterminated_sentence_count 0 16
diff_linear_scale wikilink_count 0 32
diff_linear_scale wikimarkup_formatting_count 0 64


linear_scale added_longest_char_run 0 6
linear_scale added_max_word_len 0 30
linear_scale comment_size 0 100

log_scale added_word_count 0 1000
log_scale current_num_recent_edits 0 100
log_scale current_num_recent_reversions 0 20
log_scale current_word_count 0 1000
log_scale user_distinct_pages 0 1024
log_scale user_edit_count 0 1024

age_scale current_page_made_time
age_scale user_reg_time

boolean current_minor

exact main_bayes_score

echo "ann_user_warns = \"user_warns * 2 / user_edit_count\";"
addallnames "ann_user_warns"
echo "ann_added_reused_words = \"(added_reused_words - added_common_words) / (added_word_count - added_common_words)\";"
addallnames "ann_added_reused_words"


echo
echo
echo
echo "$ALLNAMES"
