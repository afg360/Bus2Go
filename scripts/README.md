# Database Scripts
This section stores a few simple scripts to initialise the database
used by the Bus2Go application, from where the basic data for transit
is stored. For the moment, the data is from the [STM](https://www.stm.info/en/about/developers)
and [Exo](https://exo.quebec/en/about/open-data).

# Usage
If you are using Windows, enable permission to run powershell scripts.

For linux users, ensure the script is executable by running:
```bash
chmod u+x initall.sh
```
** The script for linux should be ran under bash. Before using the script for linux, be sure to initialise the right _$path_
and _$project_ variables either inside the initall.sh.**

# Syntax
To initialise all the databases, run this command (for windows):
```powershell
./initall.ps1 [<agency-name>] [--no-download | -help]
```
or this command (for linux):
```bash
./initall.sh [<agency-name>] [--no-download | --help]
```

The **--no-download** flag is used to only initialise the databases, without downloading
the required data (assuming that they have already been downloaded).