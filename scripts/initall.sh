#!/bin/bash
path=/absolute/path/to/scripts/folder/sql
#e.g. path=$PROJECT_FOLDER/bus2go/scripts
project=/absolute/path/to/your/project/database/folder

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
	#check if executed properly
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

echo -n "Would you like to copy the database for the android schedules project? [y/n] -> "
read choice
#this part doesnt work properly for the moment
if [[ $choice = 'y' || 'yes' ]]; then
    echo "Copying database to the project"
	cp -i 'sql/stm_info.db' $project
else
	echo "No copying. Don't forget to do the process manually"
fi
echo "Completed task"
exit 0
