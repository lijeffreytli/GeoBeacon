1. Team members’ names and uteids:
Jeffrey Li: jl47232
Katie Ensign: krh524 
Jared Hettinger: jah3735

2. Brief instructions on how to use the app. After working hours and hours on the app, it will be obvious to you how it works, but imagine a new user, who has no idea. Copy any information from the high level read me.

•Prior to app use:
In order to use the app, location services must be enabled on the device. This app can be used outdoors as long as there is an available WIFI or network connection. 

•Opening the app:
On the main page, a map of the user’s location will be displayed along with the user’s exact coordinates (longitude, latitude), the nearest street location, as well as the ‘Accuracy’ of the user’s location, an estimation based on GPS or network connectivity (the lower the number, the more precise). 

•Hit ‘Share My Location’:
This activity will display the user’s current location. The user can hit the ‘Contacts’ button which will display all the contacts in the user’s Contacts app. After selecting a contact, the ‘Select a Contact’ field will be auto-filled with the contact’s name. The user has the option of adding an additional message that will be sent along with the user’s location. A confirmation box will appear, confirming whether or not to send the message/location to the requested contact.

•Hit ‘!’ (also referred to as the emergency button):
This aspect of the application will send the user’s exact coordinates as well as the nearest street location to emergency personnel. A drop-down menu allows the user to quickly select a generic emergency message. If the appropriate message is not listed in the drop-down, the user has the option of inputting an additional message. A checkbox preference allows the user to send all of this information to emergency contacts which can be selected in the ‘Settings’ menu. (This feature, however, will not be released until the beta) 

Note: for the alpha release, all ‘Emergency’ messages will be sent to developers for testing purposes.

•Selecting from the ActionBar menu:
Help: This is a generic page that describes the basic functionality of the app and an abridged version of the information provided above.
About: This is a generic page that lists developer names, the application icon, and a simplified description of the overall purpose of the app.
Settings: This is currently a beta feature. However, a default Settings page is present but does not interact with the rest of the app.

3. a list of features/use cases you have completed
•Main menu: the main menu of the app shows the user’s location in Google Maps along with buttons that implement the other app’s features. The main menu obtains the user’s location and displays it in an easy-to-read font. Everything that is displayed on the main menu (the Google Map, coordinates, and street address) are constantly being refreshed and updated for real-time use.
•Button 1 (Share My Location): This feature allows the user to text a specific contact their location via SMS services. 
•Button 2 (‘!’ Emergency): This feature allows the user to send a distress text to emergency contacts as well as emergency personnel.
•The user has the ability to select from a list of pre-existing, generic emergency messages that correlate with a particular situation. If the message is not specific enough, the user has the option to add additional information about the situation.
•Menu options: About, Help

4. a list of features/use cases from your application prototype that you have not completed.
•In the prototype, we discussed how we wanted to implement a feature that allowed the user to text location address/coordinates through user input. However, we decided that this feature seemed impractical and would not likely be used in the case of emergency situations, therefore we have no included this “feature” in our alpha release.

5. a list of features/use cases you added that were not part of the application prototype
•Menu options: Settings (this is currently incomplete and as mentioned above, will not interact or affect other parts of the app)

6. a list of the classes and major chunks you obtained from other sources/include a reference. (URL and title okay)
MainActivity: onCreate()
	•Get location name from fetched coordinates
	http://stackoverflow.com/questions/6922312/get-location-name-from-fetched-coordinates
	•Showing current location in Google Maps with GPS and LocationManager in Android
	http://wptrafficanalyzer.in/blog/showing-current-location-in-google-maps-with-gps-and-locationmanager-in-android/
MainActivity: initializeMap(), onCreate()
	•Android working with Google Maps V2
	http://www.androidhive.info/2013/08/android-working-with-google-maps-v2/
MainActivity/Emergency/ShareMyLocation: createHelpDialog(), createAboutDialog()
	•Tutorial 6 - Settings
ShareMyLocation/Emergency: onClickListener(), OnClick()
	•how to display Alert Dialog in android?
	http://stackoverflow.com/questions/2115758/how-to-display-alert-dialog-in-android
ShareMyLocation/GetContacts: onActivityResult()
	•How to get contacts' phone number in Android
	-http://stackoverflow.com/questions/11218845/how-to-get-contacts-phone-number-in-android
	•Pick a Number and Name From Contacts List in android app
	-http://stackoverflow.com/questions/9496350/pick-a-number-and-name-from-contacts-list-in-android-app
	•Android Get Random Contact
	http://stackoverflow.com/questions/5759385/android-get-random-contact
ShareMyLocation/Emergency: checkSMSLength()
	•How to add Character counter to SMS APP? [closed]
	http://stackoverflow.com/questions/20736874/how-to-add-character-counter-to-sms-app


7. a list of the classes and major chunks of code you completed yourself
MainActivity/Emergency/ShareMyLocation: onCreateOptionsMenu(), onOptionsItemSelected(), onCreateDialog() (everything else, except what's mentioned above)
GetSettings (self-implemented, generic Settings template obtained from Android SDK)
ShareMyLocation: onRestoreInstanceState(), onSavedInstanceState() (everything else, except what's mentioned above)
Emergency (everything else, except what's mentioned above)   
