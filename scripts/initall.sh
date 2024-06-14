#!/bin/bash

#The below should point to the folder where you want to place
#the database inside your project
#e.g. project=$PROJECT_FOLDER/src/main/assets/database
project=/absolute/path/to/your/project/database/folder
#The below could look like path=$PROJECT_FOLDER/scripts/
#path=/absolute/path/to/scripts/folder/scripts

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

if [[ $1 = "--help" ]]; then
  echo "Script to initialise databases containing static data from transit agencies.
       Usage: ./initall.ps1 [-help] <agency-name> [<args>]

               Giving no agency names will set up all the databases for all the available agencies
               Available agency names:
                   exo
                   stm
                   Args:
                       --no-download       Setup the database without redownloading the required files
           "

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