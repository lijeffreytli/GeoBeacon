package com.seecondev.seecon;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends ActionBarActivity{
	
	static final int DIALOG_ABOUT_ID = 1;
	static final int DIALOG_HELP_ID = 2;
	private static final String TAG = "Tag";
	
	// Google Map
    private GoogleMap googleMap;
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        try {
            // Loading map
            initilizeMap();
            googleMap.setMyLocationEnabled(true); // false to disable
        } catch (Exception e) {
            e.printStackTrace();
        }
        
     // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        
     // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        
     // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);
 
        LatLng mlocation = null;
        if (location == null){
        	Log.d(TAG, "location is null");
        } else {
        	Log.d(TAG, "Success!");
        }
        
        
//        Location location = googleMap.getMyLocation();
//       
//        Log.d(TAG, "HELLOOOO ");
        
        if (location != null) {
        	Log.d(TAG, "NOT NULL ");
        	mlocation = new LatLng(location.getLatitude(), 
        			location.getLongitude());
        	
        	googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mlocation, 15));
        } else {
        	Log.d(TAG, "ASDFAWEFAW ");
        }
        
        
    }

    /**
     * function to load map. If map is not created it will create it for you
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
    
    public void shareMyLocation(View view) {
		Intent intent = new Intent(this, ShareMyLocation.class);
	    startActivity(intent);
	}
    
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
