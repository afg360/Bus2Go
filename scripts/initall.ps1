#$path='/absolute/location/of/the/sql/directory'
$path='~/Desktop/Programming/Projects/android-apps/schedules/android_app/scripts/sql'
#$project='/absolute/location/of/database/directory/inside/assets'
$project='~/Desktop/Programming/Projects/android-apps/schedules/android_app/app/src/main/assets/database'
cd $path
foreach ($dir in $(ls -Attributes Directory .)){
	cd $dir
	echo "Working on $dir"
	Try {
		echo "Starting script.py"
		python script.py
		echo "Done"
		echo ""
		echo ""
	}
	Catch {
		echo "Skipping $dir. No python script to run in this folder"
		echo ""
	}
	cd ..
}
cd $path/..
#run the python script to create the new table containing better info than stoptimes
python script.py

echo "Completed the initialisation of the database"
$choice = Read-Host "Would you like to copy the database for the android schedules project? [y/n] -> "
if ($choice -match "^(y|yes)$") {
    echo "Copying database to the project"
	Copy-Item 'sql/stm_info.db' $project -Confirm
}
else {
	echo "No copying. Don't forget to do the process manually"
}
echo "Completed task"