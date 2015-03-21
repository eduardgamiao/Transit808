# Transit808
The Bus Arrival Time/Trip Planner  
University of Hawaii at Manoa ICS 466 Project

### Background
This is an open source transit app developed to provide real-time arrival information for [TheBus](http://www.thebus.org). The app also provides directions between two locations using the [Google Directions API](https://developers.google.com/maps/documentation/directions/). The app is built in Android  using [Android Studio](http://developer.android.com) and was tested using the [GenyMotion](http://www.genymotion.com) Android emulator.

### Setup
Here are the steps to get the app running:

1. Download a local copy to your machine from this page. You can use either the "Clone to Desktop" or "Download Zip" links on the side of the [GitHub page](https://github.com/eduardgamiao/Transit808/) to download a copy. Unzip the file into your preferred location.
2. Open Android Studio, click `File` -> `Import Project`.
3. Select the folder where you unzipped the file to. This will import the project into Android Studio.
4 An Invalid VCS Error may occur, you may either make the direction a repository or remove the VCS using the `-` under the configure options.
5. Before running, you will need to add a file that will store the API Keys for [TheBus HEA](http://hea.thebus.org/api_info.asp) and [Google Maps/Places](https://console.developers.google.com/project). Follow the directions at those links to receive an API Key. You will need a Google Places Browser Key and a Google Maps API Key. 
6. Locate the `api_key.xml` file and replace the `google_browser_key`, `google_api_key` and `hea_api` with your API keys.
7. Click the run button and the app will build and compile. After it completes, Android Studio will ask you which device to use, select your device and you will be up and running.

### Using GenyMotion
Here are the steps to get the app running with GenyMotion.

1. Download and install [GenyMotion](http://www.genymotion). This will require installing VirtualBox as well. [This](https://wiki.appcelerator.org/display/guides2/Installing+Genymotion) guide may be helpful for this step.
2. Create a device and run it.
3. Setup Google Play Services for your device. Tutorial can be found [here](http://www.techrepublic.com/article/pro-tip-install-google-play-services-on-android-emulator-genymotion/).
4. Run the app in Android Studio as well as the virtual device in GenyMotion which should be listed under the "Running Devices" window when Android Studio asks you which device to use for the app.
5. To get a current location registered onto the virtual device, you need to enable location access in both `Settings` and `Google Settings`. Then register a valid location on Oahu using the GPS widget located on the right-side of the window. Once this is complete, your GenyMotion device will be able to fully use this app.
