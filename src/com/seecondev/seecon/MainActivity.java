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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity{
	
	static final int DIALOG_ABOUT_ID = 1;
	static final int DIALOG_HELP_ID = 2;
	private static final String TAG = "Tag";
	private String address;
	private double latitude;
	private double longitude;
	private List<Address> addresses;
	
	Location mLocation;
	LatLng mLatLong;
	int tryCount = 0;
	
	public final static String ADDRESS = "com.seecondev.seecon.ADDRESS";
	public final static String LAT = "com.seecondev.seecon.LAT";
	public final static String LONG = "com.seecondev.seecon.LONG";
	
	Double lat;
	Double lon;
	
	// Google Map
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /* Load Google Maps */
        try {
            // Loading map
            initilizeMap();
            googleMap.setMyLocationEnabled(true); // false to disable
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        /* Get the user's locations from map data */
        getCoordinates();
                
        /* Set/Display the TextView on the Main Menu */
        TextView textViewMain = (TextView)findViewById(R.id.text_view_title);
        textViewMain.setText("Current Street Address: \n" + address + "\nExact Coordinates: \n" + latitude + ", " + longitude);
        
        tryGeoCoder();
    }
    
    // complete on a separate thread as this is a blocking operation on
    // the network
    private void tryGeoCoder() {
        AsyncTask<Geocoder, Void, List<Address>> addressFetcher = new AddFetch();
        Geocoder gc = new Geocoder(this, Locale.US);
        addressFetcher.execute(gc);
    }

    public void tryAgain() {
        tryCount++;
        if(tryCount < 10) {
            AsyncTask<Geocoder, Void, List<Address>> addressFetcher = new AddFetch();
            Geocoder gc = new Geocoder(this, Locale.US);
            addressFetcher.execute(gc);
        }
    }
    
    private  class AddFetch extends AsyncTask<Geocoder, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(Geocoder... arg0) {
            Geocoder gc = arg0[0];
            Log.d(TAG, "Geocode is present: " + Geocoder.isPresent());
            addresses = null;
            // "forward geocoding": get lat and long from name or address
            try {
                addresses = gc.getFromLocationName(
                        "713 North Duchesne, St. Charles, MO", 5);
            } catch (IOException e) {}
            if(addresses != null && addresses.size() > 0) {
                double lat = addresses.get(0).getLatitude();
                double lng = addresses.get(0). getLongitude ();
                String zip = addresses.get(0).getPostalCode();
                Log.d(TAG, "FORWARD GEO CODING: lat: " + lat + ", long: " + lng + ", zip: " + zip);
            }
            Log.d(TAG, "forward geocoding address list: " + addresses);
            
            // also try reverse geocoding, location from lat and long
            tryReverseGeocoding(gc);
            return addresses;
        }

        private void tryReverseGeocoding(Geocoder gc) {
            LocationManager locMgr = ((LocationManager) getSystemService(LOCATION_SERVICE));
            Location location = locMgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            double lat = location.getLatitude() ;
            double lng = location.getLongitude();
            Log.d(TAG, "REVERSE GEO CODE TEST lat: " + lat);
            Log.d(TAG, "REVERSE GEO CODE TEST long: " + lng);
            List<Address> addresses = null;
            try {
              addresses = gc.getFromLocation(lat, lng, 20); // maxResults
            } catch (IOException e) {}
            if(addresses != null)
                Log.d(TAG, "reverse geocoding, addresses from lat and long: " + addresses + " " + addresses.size());
        }

        protected void onPostExecute(List<Address> result) {
            if(result == null)
                tryAgain();
        }

    }
    
    
    /**
     * Function to load map. If map is not created it will create it for you
     * */
    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
 
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    
    /**
     * Function to obtain the longitude and latitude coordinates of the user's location
     * */
    private void getCoordinates() {
    	// Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Getting Current Location
        mLocation = locationManager.getLastKnownLocation(provider);
        // Create variable for LatLng
        mLatLong = null;  
        
        /* This set of code obtains the address, longitude and latitude of a given location */
        /* Source documentation: http://stackoverflow.com/questions/6922312/get-location-name-from-fetched-coordinates */
        /* Source documentation: http://wptrafficanalyzer.in/blog/showing-current-location-in-google-maps-with-gps-and-locationmanager-in-android/ */
        
        List<String> providerList = locationManager.getAllProviders();
        if (mLocation != null && provider!=null && providerList.size()>0){
        	longitude = mLocation.getLongitude();
        	latitude = mLocation.getLatitude();
        	mLatLong = new LatLng(mLocation.getLatitude(), 
        			mLocation.getLongitude());
        	Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());   
        	try {
        	    List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
        	    if(null!=listAddresses&&listAddresses.size()>0){
        	        address = listAddresses.get(0).getAddressLine(0);
        	        Log.d(TAG, "Current address: " + address);
        	        Log.d(TAG, "Longitude: " + longitude);
        	        Log.d(TAG, "Latitude: " + latitude);
        	        
        	        // Create a location marker of the user's position
        	        //MarkerOptions marker = new MarkerOptions().position(mLatLong).title("Current Location").snippet(address);
        	     
        	        // Drop a location marker of the user's position
        	        //googleMap.addMarker(marker);
        	    }
        	} catch (IOException e) {
        	    e.printStackTrace();
        	}
        }
        
        if (mLocation != null) {
        	Log.d(TAG, "Location not null");
        	mLatLong = new LatLng(mLocation.getLatitude(), 
        			mLocation.getLongitude());
        	googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLong, 16));
        } else {
        	Log.d(TAG, "Location is null");
        }   
        
        lat = latitude;
        lon = longitude;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        initilizeMap();
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
        intent.putExtra(ADDRESS, address);
        intent.putExtra(LONG, lon.toString());
        intent.putExtra(LAT, lat.toString());
	    startActivity(intent);
	}
    
    /* Emergency Button */
    public void getEmergency(View view) {
    	Intent intent = new Intent(this, Emergency.class);
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
		// TODO Auto-generated method stub
		Context context = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.help_dialog, null); 		
		builder.setView(layout);
		builder.setPositiveButton("OK", null);	
		return builder.create();
	}


	private Dialog createAboutDialog(Builder builder) {
		// TODO Auto-generated method stub
		Context context = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.about_dialog, null); 		
		builder.setView(layout);
		builder.setPositiveButton("OK", null);
		return builder.create();
	}
}
