package com.seecondev.seecon;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.gsm.SmsManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ShareMyLocation extends ActionBarActivity {
	
	static final int DIALOG_ABOUT_ID = 1;
	static final int DIALOG_HELP_ID = 2;
	
	/* Debugging Purposes */
	private static final String TAG = "Tag";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_share_my_location);
		
		// Get the message from the intent
		Intent intent = getIntent();
		String contactName = intent.getStringExtra(GetContacts.CONTACT_NAME);
		String contactNumber = intent.getStringExtra(GetContacts.CONTACT_NUMBER);
		
		
		Log.d(TAG, "In ShareMyLocation: Contact Name: " + contactName);
		Log.d(TAG, "In ShareMyLocation: Contact Number: " + contactNumber);
		
		EditText text = (EditText) findViewById(R.id.editPhoneNumber);
		
		/*This code doesn't work.*/
		//EditText messageView = (EditText)findViewById(R.id.editMessage);
		//messageView.setSingleLine();
		
		if (contactNumber != null) text.setText(contactName);
		
		//get the location information from main
		Intent intent2 = getIntent();
		String address = intent2.getStringExtra(MainActivity.ADDRESS);
		String longitude = intent2.getStringExtra(MainActivity.LAT);
		String latitude = intent2.getStringExtra(MainActivity.LONG);
		
		if (address != null && longitude != null && latitude != null){
			Log.d(TAG, address + " : " + longitude + " : " + latitude);
		}
		
		TextView tv1 = (TextView)findViewById(R.id.editCompleteMessage);
        tv1.setText("Current Street Address: \n" + address + "\nExact Coordinates: \n" + latitude + ", " + longitude);
        
//		sendSMS("2145976764","https://www.google.com/maps/@"+ longitude + "," + latitude + ",18z");
	}
	
	/* This method sends a text message to a specific phone number */
	private void sendSMS(String phoneNumber, String message){
		SmsManager sms = SmsManager.getDefault();
	    sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	
	public void getContacts(View view) {
		Intent intent = new Intent(this, GetContacts.class);
	    startActivity(intent);
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
