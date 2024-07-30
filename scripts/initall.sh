#!/bin/bash

#The below should point to the folder where you want to place
#the database inside your project
#e.g. project=$PROJECT_FOLDER/src/main/assets/database
cat .env 2> /dev/null && project="$(sed 's/project=//' .env)" || project="/absolute/path/to/your/project/database"

execute_stm(){
  if [[ $1 = "--help" ]]; then
    echo -e "\nDownload and initialise the database only for data from the STM."
    echo -e "\nUse the --no-download flag to initialise the database if you already have the data from the STM.\n"
  else
    #to replace below if needed
    path=.

    cd $path/stm
    rm stm_info.db
    if [[ $1 = "--no-download" ]]; then
      python3 setup_stm_db.py "no-download"
    else
      python3 setup_stm_db.py
    fi
    cp -i 'stm_info.db' $project
    cd ..
  fi
}

execute_exo(){
  #to replace below if needed
  #path=/project/folder
  if [[ $1 = "--help" ]]; then
    echo -e "\nDownload and initialise the database only for data from Exo."
    echo -e "\nUse the --no-download flag to initialise the database if you already have the data for Exo.\n"
  else
    path=.
    cd $path/exo
    rm exo_info.db
    if [[ $1 = "--no-download" ]]; then
      python3 setup_exo_db.py "no-download"
    else
      python3 setup_exo_db.py
    fi
    cp -i 'exo_info.db' $project
    cd ..
  fi
}

clean() {
	# delete all subdirs inside exo dir
	[ rm -r $(find ./exo -mindepth 1 -maxdepth 1 -type d) 2> /dev/null ] || echo "Exo files are already deleted"
	#remove all text files in stm dir
	[ rm ./stm/*.txt 2> /dev/null ] || echo "Stm files already deleted"
}

if [[ $1 = "--help" ]]; then
  echo "Script to initialise databases containing static data from transit agencies.
       Usage: ./initall.ps1 [--help] <agency-name | command> [<args>]

               Giving no agency names or command will set up all the databases for all the available agencies
               Available agency names:
                   exo
                   stm
                   Args:
                       --no-download       Setup the database without redownloading the required files

				Available commands:
					clean -> deletes all the txt files in the subdir of this script
           "

elif [[ $1 = "clean" && $# -lt 2 ]]; then
	clean

elif [[ $1 = "stm" && $# -lt 3 ]]; then
  execute_stm $2

elif [[ $1 = "exo" && $# -lt 3 ]]; then
  execute_exo $2

elif [[ $# -le 1 && ! $1 = "--help" ]]; then
  execute_stm $1
  execute_exo $1

else
  echo "Syntax Error!"
  exit 1
fi
