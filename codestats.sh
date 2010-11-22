#!/bin/sh
echo -e "Lang\t  Lines\t  Words\t  Bytes"
for lang in 'PHP/*.php' 'C++/*.?pp' 'Java/*.java' 'Bash/*.sh' 'Python/*.py' 'Make/Makefile'
do
	match=$(basename "${lang}")
	name=$(dirname "${lang}")
	echo -ne "${name}:\t"
	find . -iname "${match}" -print0 | xargs -0 cat | wc
done
