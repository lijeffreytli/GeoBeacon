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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ShareMyLocation extends ActionBarActivity {
	
	static final int DIALOG_ABOUT_ID = 1;
	static final int DIALOG_HELP_ID = 2;
	
	Button btnSendSMS;
	private String message;
	private String contactNumber;
	private String contactName;
	
	/* Debugging Purposes */
	private static final String TAG = "Tag";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_share_my_location);
		
		// Get the message from the intent
		Intent intent = getIntent(); 
		contactName = intent.getStringExtra(GetContacts.CONTACT_NAME);
		contactNumber = intent.getStringExtra(GetContacts.CONTACT_NUMBER);
		
		// Get the location information from MainActivity
		Intent intent2 = getIntent(); //is this necessary?
		String address = intent2.getStringExtra(MainActivity.ADDRESS);
		String longitude = intent2.getStringExtra(MainActivity.LAT);
		String latitude = intent2.getStringExtra(MainActivity.LONG);
		
		Log.d(TAG, "In ShareMyLocation: Contact Name: " + contactName);
		Log.d(TAG, "In ShareMyLocation: Contact Number: " + contactNumber);
		
		EditText text = (EditText) findViewById(R.id.editPhoneNumber);
		if (contactNumber != null) 
			text.setText(contactName);
		
		/*This code doesn't work.*/
		//EditText messageView = (EditText)findViewById(R.id.editMessage);
		//messageView.setSingleLine();
		
		/* Debugging Purposes */
		if (address != null && longitude != null && latitude != null){
			Log.d(TAG, address + " : " + longitude + " : " + latitude);
		}
		
		/* Print out the Current Street Address to the screen */
		TextView currentAddress = (TextView)findViewById(R.id.editCompleteMessage);
        currentAddress.setText("Current Street Address: \n" + address);
        
//		sendSMS("2145976764","https://www.google.com/maps/@"+ longitude + "," + latitude + ",18z");
        
        /* Obtain the view of the 'Send Button' */
        btnSendSMS = (Button) findViewById(R.id.buttonSend);
        
        /* Once the user hits the "Send" button */
        btnSendSMS.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {   
            	/* AlertDialog box for user confirmation */
            	AlertDialog.Builder builder1 = new AlertDialog.Builder(ShareMyLocation.this);
                builder1.setMessage("Send to this number?");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    	
                    	message = "HAI. THIS IS NOT A TEST";
                    	String phoneNo = contactNumber;
                    	
                    	if (phoneNo.length()>0) { //Checks whether the number is not null      
                        	sendSMS(phoneNo, message); 
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
	
	public void getContacts(View view) {
		Intent intent = new Intent(this, GetContacts.class);
	    startActivity(intent);
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
