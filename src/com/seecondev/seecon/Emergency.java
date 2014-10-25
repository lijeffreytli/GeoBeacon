package com.seecondev.seecon;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class Emergency extends ActionBarActivity {
	
	static final int DIALOG_ABOUT_ID = 1;
	static final int DIALOG_HELP_ID = 2;
	
	private Button btnSendSMS;
	private String mMessage = "";
	private String mContactNumber;
	private String mContactName;
	private String mAddress;
	private double mLatitude;
	private double mLongitude;

	/* Debugging Purposes */
	private static final String TAG = "SEECON_EMERGENCY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_emergency);
		
		
		// Get the location information from MainActivity
		Intent intent2 = getIntent(); //is this necessary?
		mAddress = intent2.getStringExtra(MainActivity.ADDRESS);
		String latStr = intent2.getStringExtra(MainActivity.LAT);
		String longStr = intent2.getStringExtra(MainActivity.LONG);
		
		if (longStr != null) {
			mLongitude = Double.parseDouble(longStr);	
		}
		if (latStr != null) {
			mLatitude = Double.parseDouble(latStr);
		}

		Log.d(TAG, "In Emergency: Address: " + mAddress);
		Log.d(TAG, "In Emergency: Lat: " + latStr);
		Log.d(TAG, "In Emergency: Long: " + longStr);
		
		/* Obtain the view of the 'Send Button' */
		btnSendSMS = (Button) findViewById(R.id.buttonSendEmergency);
		/* Once the user hits the "Send" button */
		btnSendSMS.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{   
				/* Optional message */
				EditText mOptionalMessage = (EditText)findViewById(R.id.editMessageToEmergency);
				String mStrOptionalMessage = mOptionalMessage.getText().toString();
				Log.d(TAG, "Optional Message" + mStrOptionalMessage);
				
				/* Obtain spinner spinner information */
				Spinner spinner = (Spinner)findViewById(R.id.spinnerEmergencyDialogs);
				String spinnerText =spinner.getSelectedItem().toString();
				
				Log.d(TAG, "In Emergency: text: " + spinnerText);
				
				/* This is the message that will be sent to emergency contacts */
				mMessage += spinnerText + "\nCurrent address: " + mAddress + "\nCoordinates: " + "https://www.google.com/maps?z=18&t=m&q=loc:" + mLatitude + "+" + mLongitude + "\n\n";
				
				/* AlertDialog box for user confirmation */
				AlertDialog.Builder builder1 = new AlertDialog.Builder(Emergency.this);
				builder1.setMessage("Send to emergency contacts?");
				builder1.setCancelable(true);
				builder1.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						/* Debugging purposes - Send to ourselves */
						//String phoneNo = mContactNumber;
						String phoneKatie = "12145976764";
						String phoneJeff = "15129653085";
						String phoneJared = "14693942157";

						if (phoneJeff != null && phoneJeff.length() > 0) { //Checks whether the number is not null      
							//sendSMS(phoneNo, mMessage); 
							sendSMS(phoneKatie, mMessage);
							sendSMS(phoneJeff, mMessage);
							//sendSMS(phoneJared, mMessage);
							finish(); //After sending the message, return back to MainActivity
						} else //Throw an exception if the number is invalid
							Toast.makeText(getBaseContext(), 
									"Please enter a valid phone number.", 
									Toast.LENGTH_SHORT).show();
					}
				});
				builder1.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				AlertDialog alert11 = builder1.create();
				alert11.show();
			}
		});
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
    		startActivityForResult(new Intent(this, GetSettings.class),0);  
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
