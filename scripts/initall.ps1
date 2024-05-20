# Check if the txt files exist (assuming they are the correct ones)
$list = Get-ChildItem -Path . -Recurse -Filter *.txt | Where-Object { $_.Name -notmatch 'note[s]?.txt' }
# Download them if the user chooses to. Curl and unzip must be installed to proceed
if (-not $list) {
    Write-Output "No matching txt data files found in the subdirectories."
    $answer = Read-Host -Prompt "Do you wish to download them now? [y/n] (no will abort the script)"
    if ($answer -eq "y" -or $answer -eq "yes") {
        if (-not (Get-Command expand -ErrorAction SilentlyContinue)) {
            Write-Output "unzip is not installed on this system. You must install it before running this script. Aborting."
            exit 2
        }
        Invoke-WebRequest -Uri "https://www.stm.info/sites/default/files/gtfs/gtfs_stm.zip" -OutFile "data.zip"
        Expand-Archive -Path "data.zip" -DestinationPath .
        if ($LastExitCode -eq 0) {
            Remove-Item "agency.txt", "calendar_dates.txt", "feed_info.txt", "data.zip"
        }
        else {
            Write-Output "An error occurred trying to unzip the data file. Aborting the script"
            exit 3
        }
        # Move the unzip files to the correct directory, named similarly
        Get-ChildItem -Path . -Filter *.txt | ForEach-Object {
            if ($_.Name -eq "stop_times.txt") {
                Move-Item $_.FullName -Destination "./sql/stop-times"
            }
            else {
                $base_name = "./sql/$($_.BaseName)"
                if (Test-Path $base_name -PathType Container) {
                    Move-Item $_.FullName -Destination $base_name
                }
            }
        }
        $path = (Get-Location).Path + "/sql"
    }
    else {
        Write-Output "Aborting script"
        exit 1
    }
}
else {
    #$path='PROJECT_FOLDER/scripts/sql'
    $path = ".\sql"
}


#$project='/absolute/location/of/database/directory/inside/assets'
$project='~/Desktop/Programming/Projects/android-apps/bus2go/android_app/app/src/main/assets/database'
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