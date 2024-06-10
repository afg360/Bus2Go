#!/bin/bash

#The below should point to the folder where you want to place
#the database inside your project
#e.g. project=$PROJECT_FOLDER/src/main/assets/database
project=/absolute/path/to/your/project/database/folder
#The below could look like path=$PROJECT_FOLDER/scripts/sql
path=/absolute/path/to/scripts/folder/scripts/sql

execute_stm(){
  list=$(find . -type f -name "*.txt" | grep -v 'note[s]\?.txt')
    #download them if the user chooses to. Curl and unzip must be installed to proceed
    if [ -z "$list" ]; then
        echo "No matching txt data files found in the subdirectories."
            echo -n "Do you wish to download them now? [y/n] (no will abort the script) -> "
            read -r answer
            if [[ $answer = "y" || $answer = "yes" ]]; then
                if ! command unzip &> /dev/null; then
                    echo "unzip is not installed on this system. You must install it before running this script. Aborting."
                    exit 2
                fi
                curl -L https://www.stm.info/sites/default/files/gtfs/gtfs_stm.zip -o data.zip
                unzip data.zip
                if [[ $? -eq 0 ]]; then
                    rm agency.txt calendar_dates.txt feed_info.txt data.zip
                else
                    echo "An error occured trying to unzip the data file. Aborting the script"
                    exit $?
                fi
                #move the unzip files to the correct directory, named similarly
                for file in *.txt; do
                    if [[ -f $file ]]; then
                        if [[ $file = "stop_times.txt" ]]; then
                            mv "$file" "./stm/stop-times"
                        else
                                base_name="./stm/${file%.txt}"
                            if [[ -d $base_name ]]; then
                                mv "$file" "$base_name/"
                            fi
                        fi
                    fi
                done
                path=$(pwd)/stm
            else
                echo "Aborting script"
                exit 1
            fi
    fi

    #initialisation of the database starts here

    cd $path
    for dir in $(ls)
    do
      if [[ ! -d $dir ]]; then
        continue
      fi
    	cd $dir
    	echo "Working on $dir"
    	echo "Starting script.py"
    	python3 script.py
    	#check if python script executed properly
    	if [[ $? -ne 0 ]]; then
    		echo -e "Skipping $dir. No python script to run in this folder\n\n"
    	else
    		echo -e "Done\n\n"
    	fi
    	cd ..
    done

    cd $path/..
    #run the python script to create the new table containing better info than stoptimes
    python3 ./script.py
    echo "Completed the initialisation of the database"

    #copy to the $project path if the user chooses to
    echo -n "Would you like to copy the database for the android schedules project? [y/n] -> "
    read choice
    if [[ $choice = 'y' || 'yes' ]]; then
        echo "Copying database to the project"
    	cp -i 'stm/stm_info.db' $project
    else
    	echo "No copying. Don't forget to do the process manually"
    fi
    echo "Completed task"
}

execute_exo{
  #to replace below if needed
  #path=/project/folder
  path=.

  cd $path/exo
  if [[ $1 = "--no-download" ]]; then
    python3 setup_exo_db.py "no-download"
  elif [[ $# -gt 2]]; then
    echo "Too many given arguments!"
  else
    python3 setup_exo_db.py
  fi
  cp -i 'exo_info.db' $project
  exit $?
}

if [[ $1 = "--help" ]]; then
  echo "Script to initialise databases containing static data from transit agencies.
      Usage: $ ./initall.sh <agency-name> -> download the data and initialise the database
                                              only related to the given agency-name.
             $ ./initall.sh -> download the data from all the agencies and init all the databases.
             $ ./initall.sh --help -> print this help message
      E.g.:  $ ./initall.sh stm
      "

#stm download
elif [[ $1 = "stm" ]]; then
  #check if the txt files exist (assuming they are the correct ones)
  execute_stm
  exit $?

elif [[ $1 = "exo" ]]; then
  execute_exo $2
  exit $?

#all downloads
elif [[ $# -eq 0 ]]; then
  execute_exo
  execute_stm
  exit $?

else
  echo "Syntax Error!"
  exit 1
fi