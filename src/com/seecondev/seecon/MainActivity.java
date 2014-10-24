package com.seecondev.seecon;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements android.location.LocationListener{

	static final int DIALOG_ABOUT_ID = 1;
	static final int DIALOG_HELP_ID = 2;
	private static final String TAG = "SEECON_DEBUG";
	private String mAddress;
	private double mLatitude;
	private double mLongitude;
	private float mAccuracy;
	private LocationManager mLocationManager;
	private String mProvider;

	public final static String ADDRESS = "com.seecondev.seecon.ADDRESS";
	public final static String LAT = "com.seecondev.seecon.LAT";
	public final static String LONG = "com.seecondev.seecon.LONG";
	private final static long MIN_TIME = 1000;
	private final static float MIN_DIST = 1;


	// Google Map
	private GoogleMap mGoogleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "in onCreate");
		/* Load Google Maps */
		try {
			// Loading map
			initializeMap();
			mGoogleMap.setMyLocationEnabled(true); // false to disable
			mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Get the user's locations from map data */
		getCoordinates();
		mLocationManager.requestLocationUpdates(mProvider, MIN_TIME, MIN_DIST, this);
	}



	/**
	 * Function to load map. If map is not created it will create it for you
	 * */
	private void initializeMap() {
		if (mGoogleMap == null) {
			mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (mGoogleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	/**
	 * Function to obtain the longitude and latitude coordinates of the user's location
	 * */
	/* This set of code obtains the address, longitude and latitude of a given location */
	/* Source documentation: http://stackoverflow.com/questions/6922312/get-location-name-from-fetched-coordinates */
	/* Source documentation: http://wptrafficanalyzer.in/blog/showing-current-location-in-google-maps-with-gps-and-locationmanager-in-android/ */
	private void getCoordinates() {
		// Getting LocationManager object from System Service LOCATION_SERVICE
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		// Creating a criteria object to retrieve provider
		Criteria criteria = new Criteria();
		// Getting the name of the best provider
		mProvider = mLocationManager.getBestProvider(criteria, true);
		Log.d(TAG, "Best provider: " + mProvider);
		// Getting Current Location
		Location location = mLocationManager.getLastKnownLocation(mProvider);
		if (location != null) {
			Log.d(TAG, "location: " + location);  
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			mAccuracy = location.getAccuracy();
			Log.d(TAG, "Longitude: " + mLongitude);
			Log.d(TAG, "Latitude: " + mLatitude);
			Log.d(TAG, "Accuracy: " + mAccuracy);
			geocodeAndMarkAddress();
		} else {
			Toast.makeText(getApplicationContext(),
					"Sorry! unable to get location", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void geocodeAndMarkAddress() {
		LatLng latLng = new LatLng(mLatitude, mLongitude);
		Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());   
		try {
			List<Address> listAddresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
			if(listAddresses != null && listAddresses.size() > 0){
				mAddress = listAddresses.get(0).getAddressLine(0);
				Log.d(TAG, "Current address: " + mAddress);
				// Create a location marker of the user's position
				MarkerOptions marker = new MarkerOptions().position(latLng).title("Current Location").snippet(mAddress);

				// Drop a location marker of the user's position
				mGoogleMap.addMarker(marker);
				mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		/* Set/Display the TextView on the Main Menu */
		TextView textViewMain = (TextView)findViewById(R.id.text_view_title);
		textViewMain.setText("Current Street Address: " + mAddress + "\nCoordinates: " + mLatitude + ", " + mLongitude + "\nAccuracy: " + mAccuracy + " meters");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "in onResume");
		initializeMap();
		getCoordinates();
		geocodeAndMarkAddress();
	}

	@Override
	public void onPause() {
		if (mGoogleMap != null) {
			mGoogleMap.setMyLocationEnabled(false);
		}
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/* ShareMyLocation Button */
	public void shareMyLocation(View view) {
		Intent intent = new Intent(this, ShareMyLocation.class);
		intent.putExtra(ADDRESS, mAddress);
		intent.putExtra(LONG, Double.valueOf(mLongitude).toString());
		intent.putExtra(LAT, Double.valueOf(mLatitude).toString());
		startActivity(intent);
	}

	/* Emergency Button */
	public void getEmergency(View view) {
		Intent intent = new Intent(this, Emergency.class);
		intent.putExtra(ADDRESS, mAddress);
		intent.putExtra(LONG, Double.valueOf(mLongitude).toString());
		intent.putExtra(LAT, Double.valueOf(mLatitude).toString());
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		switch (item.getItemId()){
		case R.id.action_settings:
			startActivityForResult(new Intent(this, Settings.class),0);  
			return true;
		case R.id.menu_about:
			showDialog(DIALOG_ABOUT_ID);
			return true;
		case R.id.menu_help:
			showDialog(DIALOG_HELP_ID);
			return true;
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

		//		if(dialog == null)
		//			Log.d(TAG, "Uh oh! Dialog is null");
		//		else
		//			Log.d(TAG, "Dialog created: " + id + ", dialog: " + dialog);
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



	@Override
	public void onLocationChanged(Location loc) {
		Log.d(TAG, "in onLocationChanged");
		mLatitude = loc.getLatitude();
		mLongitude = loc.getLongitude();
		mAccuracy = loc.getAccuracy();
		geocodeAndMarkAddress();
	}
	public void onProviderDisabled(String provider) {

	}
	public void onProviderEnabled(String provider) {

	}
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}
}

