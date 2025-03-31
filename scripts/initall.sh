#!/bin/bash

if ! python3 --version > /dev/null 2> /dev/null; then
	echo "This script needs python to be able to run"
	exit 1
fi

#The below should point to the folder where you want to place
#the database inside your project
#e.g. project=$PROJECT_FOLDER/src/main/assets/database
cat .env > /dev/null 2> /dev/null && project="$(sed 's/project=//' .env | sed 's/\"//g')" || project="/absolute/path/to/your/project/database"

execute_stm(){
	if [[ $1 = "--help" || $1 = "-h" ]]; then
		echo -e "\nDownload and initialise the database only for data from the STM."
		echo -e "\nUse the --no-download flag to initialise the database if you already have the data from the STM.\n"
	else
		#to replace below if needed
		path=./stm
		[ ! -d "$path" ] && mkdir "$path"

		rm "$path/stm_info.db"
		if [[ $1 = "--no-download" ]]; then
			python3 setup_stm_db.py "no-download"
		else
			python3 setup_stm_db.py 
		fi
		cp -i "$path/stm_info.db" "$project"
		cd ..
	fi
}

execute_exo(){
	#to replace below if needed
	#path=/project/folder
	if [[ $1 = "--help" || $1 = "-h" ]]; then
		echo -e "\nDownload and initialise the database only for data from Exo."
		echo -e "\nUse the --no-download flag to initialise the database if you already have the data for Exo.\n"
	else
		path=./exo
		[ ! -d "$path" ] && mkdir "$path"

		rm "$path/exo_info.db"
		if [[ $1 = "--no-download" ]]; then
			python3 setup_exo_db.py "no-download"
		else
			python3 setup_exo_db.py
		fi
		cp -i "$path/exo_info.db" "$project"
		cd ..
	fi
}

clean() {
	# delete all subdirs inside exo dir
	[ rm -r $(find ./exo -mindepth 1 -maxdepth 1 -type d) 2> /dev/null ] || echo "Exo files are already deleted"
	#remove all text files in stm dir
	[ rm ./stm/*.txt 2> /dev/null ] || echo "Stm files already deleted"
}

usage() {
	echo "Script to initialise databases containing static data from transit agencies.
Usage: ./initall.sh [-h/--help] <agency-name | command> [<args>]

Giving no agency names or command will set up all the databases for all the available agencies
Available agency names:
	exo
	stm
Args:
	--no-download       Setup the database without redownloading the required files

Available commands:
	clean -> deletes all the txt files in the subdir of this script
	"
}

if [[ $# -eq 1 && ($1 = "--help" || $1 = "-h") ]]; then
	usage

elif [[ $1 = "clean" && $# -eq 1 ]]; then
	clean

elif [[ $1 = "stm" && $# -lt 3 ]]; then
	execute_stm $2

elif [[ $1 = "exo" && $# -lt 3 ]]; then
	execute_exo $2

elif [[ $# -eq 1 && ($1 = "--no-download") ]]; then
	execute_stm $1
	execute_exo $1

else
	echo "Syntax Error!"
	usage
	exit 1
fi
