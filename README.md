# Bus2Go
<img src="./icon/icon_black_version.svg"/>

An android application allowing users to access data such as bus arrivals with minimal internet connectivity, 
with a big focus on user privacy.
For the moment, only data from the [STM](https://stm.info/en) and [Exo](https://exo.quebec/en) is used.

>The data provided by the transport agencies have their own respective licences, available
at their websites.

# Features
- Check bus schedules
- Add them to your favourites to access them on the home screen
- Manage transit agencies data stored locally on your device
> Note: The first time you open the app, you may encounter some latency when you search for a bus.
This is normal, give the app about 10 seconds so that it has time to cache the data.

The application works in sync with the [Bus2Go-Backend](https://github.com/afg360/bus2go-backend),
which you can self-host. More information is available on the projects GitHub page or at
[https://bus2go.app](https://bus2go.app).
This is the server used to generate agency data files suitable for Bus2Go, and is also the application
that will be used for the realtime data implementation.

# Coming Soon
- Real time data syncing → if the user chooses to, the app will gather more accurate information about bus schedules and locations
- Map → implement a map to show a visual of the location of searched buses
- Alarms → implement alarms that the user could set to notify him to get ready to get to the bus

# Building from source
First clone the repo:
```bash
git clone "https://github.com/afg360/bus2go"
cd bus2go
```
Create a keystore file using the keytool utility used for signing the app:
```bash
keytool -genkeypair -v \
  -keystore bus2go.pkcs12 \
  -alias my-alias \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storetype PKCS12
```

Create the `local.properties` file containing the keystore information:
```bash
echo -ne "STORE_PASSWORD=<your-keystore-password>\nKEY_RELEASE=release\nKEY_PASSWORD=<your-key-password>" > local.properties
```

`<your-keystore-password>` and `<your-key-password>` should be the same.

You can finally generate the apk by running:
```bash
./gradlew assembleFdroidRelease
```
which will be located at `./app/build/outputs/apk/fdroid/release`.

# Disclaimer
The application is still in alpha version. It is not fully completed and may contain bugs. 
We are trying to fix these and implement the features ASAP.
