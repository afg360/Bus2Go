# Database
This section stores a few simple scripts to initialise the database
used by the Bus2Go application, from where the basic data for transit
is stored. For the moment, the data is from the [STM](https://www.stm.info/en/about/developers)
and [Exo](https://exo.quebec/en/about/open-data).

### Make sure to set the appropriate variables before running the scripts, depending on your platform.

# Usage
Before using the script, be sure to initialise the right **$path** and **$project** variables either inside the initall.ps1 or the initall.sh script so that the script can be ran properly

## Windows
If you are using Windows, enable permission to run powershell scripts.

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
You can use the **--no-download** flag with the **exo** agency to skip the download process if the files are already there.

You can also check the help output by running:
```powershell
./initall.ps1 -help
```
## Linux
The script for linux should be ran under bash. Be sure to give the right permissions to the file so that it can be run:
```bash
chmod u+x initall.sh
```
### Syntax
To initialise all the databases, run
```bash
./initall.sh
```
This will download the necessary data as well as create the database files, and prompt you to copy the files at the right directory

To initialise the database for a certain agency only, run
```bash
./initall.sh <agency-name>
```
You may use the **--no-download** flag with the **exo** agency to skip the download process if the files are already there.

To get the help output, run
```bash
./initall.sh --help
```
### Dependencies
The script uses curl to download the necessary files, as well as unzip in linux for unziping the zip file. You must have both installed before
proceeding.
