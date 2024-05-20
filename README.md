# Bus2Go
This android application allows users to access data such as bus arrivals with minimal internet connectivity, 
with a big focus on user privacy.
For the moment, we are only using the STM data.

# Features
- Check bus schedules
- Add them to favourite to access them on the home screen
The above needs absolutely no internet connection to work

## Coming Soon
- Real time data syncing -> if the user chooses to, the app will gather more accurate information about bus schedules and locations
- Map -> implement a map to show a visual of the location of searched buses
- Alarms -> implement alarms that the user could set to notify him to get ready to get to the bus
- AutoUpdates -> If the user chooses to, allow auto updates

## Ideas
- !! Calculate best paths to take to get to a destination at a certain time

# Building from source
To build from source, you must initialise the local database used by the application. It
can be initialised by running powershell/bash scripts. More info at [./scripts](./scripts).
You must make sure that the database is stored under [./app/src/main/assets/database](./app/src/main/assets/database)
(The database folder is not created in the git repo so you may need to create it yourself).
You can then run `./gradlew build` to build it.

# Disclaimer
The application is still in alpha version. It is not fully completed and may contain bugs. 
We are trying to fix these and implement the features ASAP.

# Acknowledgements
This application uses the GOLI font. It's licence is available under [./app/src/main/assets/fonts](./app/src/main/assets/database)
