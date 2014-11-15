package com.seecondev.seecon;

import java.io.IOException;
import java.security.Provider;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity{

	static final int DIALOG_ABOUT_ID = 1;
	static final int DIALOG_HELP_ID = 2;
	private static final String TAG = "SEECON_DEBUG";
	private Location mLocation;
	private String mAddress;
	private double mLatitude;
	private double mLongitude;
	private float mAccuracy;
	private LocationManager mLocationManager;
	private List<String> mProviders;

	public final static String ADDRESS = "com.seecondev.seecon.ADDRESS";
	public final static String LAT = "com.seecondev.seecon.LAT";
	public final static String LONG = "com.seecondev.seecon.LONG";
	private final static long MIN_TIME = 1000;
	private final static float MIN_DIST = 3;
	private static boolean mLocationEnabled;
	private static boolean mShowCoordinates;

	private SharedPreferences mPrefs;

	// Google Map
	private GoogleMap mGoogleMap;
	private Marker mMarker;

	// Sound
	private SoundPool mSounds;	
	private boolean mSoundOn;
	private int mClickSoundID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "in onCreate");

		mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
		mSoundOn = mPrefs.getBoolean("sound", true);
		mShowCoordinates = mPrefs.getBoolean("showCoordinates", false);
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());		
		if (resultCode == ConnectionResult.SUCCESS) {
			mLocationEnabled = true;
			/* Load Google Maps */
			try {
				// Loading map
				initializeMap();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			mLocationEnabled = false;
			/* AlertDialog if the user needs to update Google Play Services */
			generateAlert("Please update Google Play Services.", true);
		}
	}

	public void generateAlert(String message, final boolean isFatal) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage(message);
		builder.setCancelable(true);
		builder.setNegativeButton("Ok",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				if (isFatal)
					finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}


	/**
	 * Function to load map. If map is not created it will create it for you
	 * */
	private void initializeMap() {
		Log.d(TAG, "in initializeMap");
		if (mGoogleMap == null) {
			mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
		}
		if (mLocationManager == null) {
			mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		} 
		// Start with better of two last known locations
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Log.d(TAG, "gps last known location is " + mLocation);
		}
		Location netLocation = null;
		if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			netLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			Log.d(TAG, "network last known location is " + mLocation);
		}
		if (netLocation != null && isBetterLocation(netLocation)) {
			mLocation = netLocation;
		}
		Log.d(TAG, "we chose the following best location: " + mLocation);
		if (mLocation == null) {
			generateAlert("Error: location information unavailable.", true);
		}
		updateLocation(mLocation);

		mGoogleMap.setMyLocationEnabled(true); 
		mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
		mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude, mLongitude), 16));
		new GetAddressTask().execute();
		Log.d(TAG, "done with initialize map");

	}

	public void updateLocation(Location newLocation) {
		if (newLocation == null){
			Toast.makeText(getBaseContext(), 
					"Please turn on WIFI and/or Location Services.", 
					Toast.LENGTH_SHORT).show();
		} else {
			mLocation = newLocation;
			mLatitude = mLocation.getLatitude();
			mLongitude = mLocation.getLongitude();
			mAccuracy = mLocation.getAccuracy();
		}
	}

	private void createSoundPool() {
		mSounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		// 2 = maximum sounds to play at the same time,
		// AudioManager.STREAM_MUSIC is the stream type typically used for games
		// 0 is the "the sample-rate converter quality. Currently has no effect. Use 0 for the default."
		mClickSoundID = mSounds.load(this, R.raw.click, 1);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "in onResume");
		if (mLocationEnabled) {
			createSoundPool();

			if (mLocationManager == null) {
				mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			}
			if (mLocationManager != null) {
				Criteria criteria = new Criteria();
				mProviders = mLocationManager.getProviders(true);

				for (String provider: mProviders) {
					Log.d(TAG, "requesting location updates from " + provider);
					mLocationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DIST, seeconLocationListener);
				}
				initializeMap();
			}
		}
	}


	private void playSound(int soundID) {
		if (mSoundOn)
			mSounds.play(soundID, 1, 1, 1, 0, 1);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "in onPause");
		if(mSounds != null) {
			mSounds.release();
			mSounds = null;
		}	
		if (mGoogleMap != null) {
			Log.d(TAG, "removing location updates");
			mGoogleMap.setMyLocationEnabled(false);
			mLocationManager.removeUpdates(seeconLocationListener);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_CANCELED) {
			// Apply potentially new settings
			mSoundOn = mPrefs.getBoolean("sound", true);
			mShowCoordinates = mPrefs.getBoolean("showCoordinates", false);
		}
	}

	/* ShareMyLocation Button */
	public void shareMyLocation(View view) {
		playSound(mClickSoundID);
		Intent intent = new Intent(this, ShareMyLocation.class);
		intent.putExtra(ADDRESS, mAddress);
		intent.putExtra(LONG, Double.valueOf(mLongitude).toString());
		intent.putExtra(LAT, Double.valueOf(mLatitude).toString());
		startActivity(intent);
	}

	/* Emergency Button */
	public void getEmergency(View view) {
		playSound(mClickSoundID);
		Intent intent = new Intent(this, Emergency.class);
		intent.putExtra(ADDRESS, mAddress);
		intent.putExtra(LONG, Double.valueOf(mLongitude).toString());
		intent.putExtra(LAT, Double.valueOf(mLatitude).toString());
		startActivity(intent);
	}
	
	public void getEmergencyContacts(View view){
		Intent intent = new Intent(this, EmergencyContacts.class);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		switch (item.getItemId()){
		case R.id.action_settings:
			startActivityForResult(new Intent(this, Preferences.class),0);  
			return true;
		case R.id.menu_about:
			showDialog(DIALOG_ABOUT_ID);
			return true;
		case R.id.menu_help:
			showDialog(DIALOG_HELP_ID);
			return true;
//		case R.id.menu_emergency_contacts:
//			Intent intent = new Intent(this, EmergencyContacts.class);
//			this.startActivity(intent);
//			break;
//		case R.id.menu_gesture_confirmation:
//			Intent intent2 = new Intent(this, GestureConfirmation.class);
//			this.startActivity(intent2);
//			break;
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch(id) {
		case DIALOG_ABOUT_ID:
			dialog = createAboutDialog(builder);
			break;
		case DIALOG_HELP_ID:
			dialog = createHelpDialog(builder);
			break;
		}
		return dialog;        
	}

	private Dialog createHelpDialog(Builder builder) {
		Context context = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.help_dialog, null); 		
		builder.setView(layout);
		builder.setPositiveButton("OK", null);	
		return builder.create();
	}


	private Dialog createAboutDialog(Builder builder) {
		Context context = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.about_dialog, null); 		
		builder.setView(layout);
		builder.setPositiveButton("OK", null);
		return builder.create();
	}


	/** Code adapted from http://developer.android.com/guide/topics/location/strategies.html
	 * Determines whether one Location reading is better than the current Location fix
	 * @param location  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location) {
		if (mLocation == null) {
			Log.d(TAG, "mLocation was null, so we will use the new one");
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - mLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > 1000;
		boolean isSignificantlyOlder = timeDelta < -1000;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - mLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				mLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	/* Code adapted from http://stackoverflow.com/questions/10198614/asynctask-geocoder-sometimes-crashes */
	private class GetAddressTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Log.d(TAG, "in GeoCoder doInBackground");

			Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());   
			try {
				List<Address> listAddresses = gc.getFromLocation(mLatitude, mLongitude, 1);
				if (listAddresses != null && listAddresses.size() > 0) {
					int maxAddressLine = listAddresses.get(0).getMaxAddressLineIndex();
					if (maxAddressLine < 2) {
						mAddress = "Address unavailable";
					} else {
						mAddress = "";
						// we are making assumptions here that the last line is always country which we don't want. hopefully that's okay
						for (int i = 0; i < maxAddressLine; i++) {
							String tmp = listAddresses.get(0).getAddressLine(i);
							if (tmp != null) {
								if (i != 0)
									mAddress += "\n";
								mAddress += tmp;
							}
						}
					}
				} else {
					mAddress = "Address unavailable";
				}
				Log.d(TAG, "mAddress is " + mAddress);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.d(TAG, "in GeoCoder onPostExecute");
			if (mAddress == null) {
				generateAlert("Error: unable to retrieve address", false);
			} else {
				/* Set/Display the TextView on the Main Menu */
				TextView textViewMain = (TextView)findViewById(R.id.text_view_title);
				textViewMain.setMovementMethod(new ScrollingMovementMethod());
				String text = mAddress + "\n";
				if (mShowCoordinates)
					text += "(" + mLatitude + ", " + mLongitude + ")\n";
				text += "Accuracy: +/-" + mAccuracy + " meters";
				textViewMain.setText(text);
			}
		}

	}

	private final LocationListener seeconLocationListener =
			new LocationListener(){

		@Override
		public void onLocationChanged(Location loc) {
			Log.d(TAG, "in onLocationChanged");

			if (isBetterLocation(loc)) {
				updateLocation(loc);
				new GetAddressTask().execute();
			}
		}
		public void onProviderDisabled(String provider) {

		}
		public void onProviderEnabled(String provider) {

		}
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	};
	@Override
	public void onBackPressed() {
		//Quick fix, disable the back button
	}
}

