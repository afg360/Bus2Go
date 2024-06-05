This section stores a few simple scripts to initialise the database
used by the Bus2Go application, from where the basic data for transit
is stored. For the moment, the data is from the [stm](https://www.stm.info/en/about/developers).
Make sure to set the appropriate variables before running the scripts, depending on your platform.

# Usage
## Windows
If you are using Windows, enable permission to run powershell scripts. You can then run the script by running `./initall.ps1` inside powershell
You may need to change the $path and $project folder names to something else (on lines 42 and 47 respectively)
### Syntax
To initialise all the databases, run this command:
```powershell
./initall.ps1
```
If you only need it for a specific agency, run:
```powershell
./initall.ps1 <agency-name>
```
You can also check the help output by running:
```powershell
./initall.ps1 -help
```
## Linux
The script for linux should be ran under bash. Be sure to give the right permissions to the file so that it can be run:
```bash
chmod u+x initall.sh
./initall.sh
```
If you use the script without needing to download the txt files, make sure to set the right paths by line 44 for the $path variable.
Moreover, do the same regardless for the download, to the $project folder, which would point to folder in the android project where the
assets will be stored (in this case the database).

If you want to use the exo command, you would need to replace the $path variable to your proper path, at line 103.
### Dependencies
The script uses curl to download the necessary files, as well as unzip in linux for unziping the zip file. You must have both installed before
proceeding.
