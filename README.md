# Bus2Go
An android application allowing users to access data such as bus arrivals with minimal internet connectivity, 
with a big focus on user privacy.
For the moment, only data from the [STM](https://stm.info/en) and [Exo](https://exo.quebec/en) is used.

>The data provided by the transport agencies have their own respective licences, available
at their websites.

# Features
- Check bus schedules
- Add them to favourite to access them on the home screen
The above needs no internet connection to work
> Note: The first time you open the app, you may encounter some latency when you search for a bus.
This is normal, give the app about 10 seconds so that it has time to cache the data.

# Coming Soon
- Real time data syncing -> if the user chooses to, the app will gather more accurate information about bus schedules and locations
- Map -> implement a map to show a visual of the location of searched buses
- Alarms -> implement alarms that the user could set to notify him to get ready to get to the bus
- AutoUpdates -> If the user chooses to, allow auto updates for the app

## Ideas
- !! Calculate best paths to take to get to a destination at a certain time

# Building from source
To build from source, you must initialise the local database used by the application. It
can be initialised by running powershell/bash scripts. More info at [./scripts](./scripts).
You must make sure that the database is stored under [./app/src/main/assets/database](./app/src/main/assets/database)
You can then run 
```
./gradlew build
```
to build it.

# Disclaimer
The application is still in alpha version. It is not fully completed and may contain bugs. 
We are trying to fix these and implement the features ASAP.