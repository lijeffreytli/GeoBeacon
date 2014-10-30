1. Team members' names and uteids:
Jeffrey Li: jl47232
Katie Ensign: krh524 
Jared Hettinger: jah3735

2. Brief instructions on how to use the app. After working hours and hours on 
   the app, it will be obvious to you how it works, but imagine a new user, 
   who has no idea. Copy any information from the high level read me.

   Prior to app use:
   In order to use the app, location services must be enabled on the device. 
   This app can be used anywhere as long as there is an available WIFI or 
   network connection. Also, sms services must be available as well. 

   Opening the app:
   On the main page, a map of the user’s location will be displayed along with 
   the user’s exact coordinates (longitude, latitude), the nearest street 
   location, as well as the ‘Accuracy’ of the user’s location, an estimation 
   based on GPS or network connectivity (the lower the number, the more 
   precise). 

   Hit ‘Share My Location’:
   This activity will display the user's current location. The user can hit 
   the 'Contacts' button which will display all the contacts in the user's 
   Contacts app. After selecting a contact, the 'Select a Contact' field will 
   be auto-filled with the contact's name. The user has the option of adding 
   an additional message that will be sent along with the user's location. A 
   confirmation box will appear, confirming whether or not to send the 
   message/location to the requested contact.

   Hit '!' (also referred to as the emergency button):
   This aspect of the application will send the user’s exact coordinates as 
   well as the nearest street location to emergency personnel. A drop-down 
   menu allows the user to quickly select a generic emergency message. If the 
   appropriate message is not listed in the drop-down, the user has the option 
   of inputting an additional message. A checkbox preference allows the user 
   to send all of this information to emergency contacts which can be 
   selected in the 'Settings' menu. (This feature, however, will not be 
   released until the beta) 

   Note: for the alpha release, all 'Emergency' messages will be sent to 
   developers for testing purposes.

   Selecting from the ActionBar menu:
   Help: This is a generic page that describes the basic functionality of the 
   app and an abridged version of the information provided above.
   About: This is a generic page that lists developer names, the application 
   icon, and a simplified description of the overall purpose of the app.
   Settings: The sound preference is functional now, but the emergency contact
   list will not be implemented until the beta. 

3. A list of features/use cases you have completed

   Main menu: the main menu of the app shows the user’s location in Google 
   Maps along with buttons that implement the other app’s features. The main 
   menu obtains the user’s location and displays it in an easy-to-read font. 
   Everything that is displayed on the main menu (the Google Map, coordinates, 
   and street address) are constantly being refreshed and updated for real-time 
   use.

   Button 1 (Share My Location): This feature allows the user to text a 
   specific contact their location via SMS services. The text includes a
   link to a google map with a pin on the user's exact location. This allows
   anyone with internet access to receive the location, regardless of whether
   or not the have the app.

   Button 2 ('!' Emergency): This feature allows the user to send a distress 
   text to emergency personnel (911). Since
   Travis County has not yet implemented text-to-911 and we are only testing, 
   this text is sent instead to the app developers.

   The user has the ability to select from a list of pre-existing, generic 
   emergency messages that correlate with a particular situation. If the 
   message is not specific enough, the user has the option to add additional 
   information about the situation.
   
   Menu options: About, Help
   Displays a Dialog with text for these two menu options.

   Menu options: Settings
   We plan to be able to edit the emergency contact list here, but for now
   we just have the setting to toggle sound. This preference is persistent.
   We added sound for clicking the Share My Location and Emergency buttons,
   as well as the Send button when sending a text. 

4. A list of features/use cases from your application prototype that you have 
   not completed.

   Alpha features:

   In the prototype, we discussed how we wanted to implement a feature that 
   allowed the user to also enter a number manually instead of selecting a
   contact. However, we decided that this feature seemed impractical and 
   would rarely (if ever) be used, especially in an emergency. Therefore we 
   have not included this feature in our alpha release.

  Beta features:

  Emergency contact list - A user will be able to add contacts to an 
  emergency contacts list. When he or she sends a distress text to 911,
  a text can also be sent to emergency contacts.

  Trace a triangle - In order to prevent accidental dialing of 911, the user
  will have to draw a triangle on the screen to finalize sending a distress
  text to 911. This feature is chosen because it will be very simple to do
  in the case of an emergency, but very hard to accidentally do in a user's
  pocket.

  Message confirmation - The app will confirm to the user that a text message
  has been sent. A Toast will appear, indicating to the user whether the
  message has been sent successfully.

5. A list of features/use cases you added that were not part of the 
   application prototype

   We have not added any features or use cases outside of what was described
   in our prototype.

6. A list of the classes and major chunks you obtained from other 
   sources/include a reference. (URL and title okay)

from others
   General:
       all sound code - CS371M Tutorial 6
       creating help and about dialogs - CS371M Tutorial 3
       create an alert dialog - code adapted from http://stackoverflow.com/questions/2115758/how-to-display-alert-dialog-in-android

   Main Activity:
       isBetterLocation() and isSameProvider() - determines whether or not a
           location update is better than the current location.
           Adapted from android developer guide for location services:
           http://developer.android.com/guide/topics/location/strategies.html
       GetAddressTask class - class to implement async task for geocoding 
           addresses. Adapted from stackoverflow post:
           http://stackoverflow.com/questions/10198614/asynctask-geocoder-sometimes-crashes
       note - we also were influenced by these tutorials: 
           http://www.androidhive.info/2013/08/android-working-with-google-maps-v2/
	   http://wptrafficanalyzer.in/blog/showing-current-location-in-google-maps-with-gps-and-locationmanager-in-android/

   ShareMyLocation/Emergency: 
       using intents - influenced by CS371M Tutorial 1
       saving instance state - influenced by CS371M Tutorial 5
       live updates of text length - code adapted from http://stackoverflow.com/questions/20736874/how-to-add-character-counter-to-sms-app
       get a contact's phone number - code adapted from http://stackoverflow.com/questions/11218845/how-to-get-contacts-phone-number-in-android and http://stackoverflow.com/questions/9496350/pick-a-number-and-name-from-contacts-list-in-android-app
       hide soft keyboard - code adapted from http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext

   Settings:
       code adapted from Tutorial 6

7. A list of the classes and major chunks of code you completed yourself

   All of the UI and design code

   Main Activity:
       location setup - initializeMap() and updateLocation()
       LocationListener
       standard android code for onCreate(), onResume(), onPause(), etc.

   ShareMyLocation/Emergency:
        onCreate(), onCreateOptionsMenu(), onOptionsItemSelected(),
        onCreateDialog()

   Everything else, except what's mentioned in 6.


