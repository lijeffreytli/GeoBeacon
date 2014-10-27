package com.seecondev.seecon;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.telephony.gsm.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
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

	private Button btnSendSMS;
	private String mMessage;
	private String mContactNumber;
	private String mContactName;
	private String mAddress;
	private double mLatitude;
	private double mLongitude;
    private String mStrOptionalMessage = "";
    EditText mOptionalMessage;
    boolean mValidMessage = true;
	
	/* Debugging Purposes */
	private static final String TAG = "SEECON_SHAREMYLOCATION";
	static final int PICK_CONTACT_REQUEST = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_share_my_location);

		if (savedInstanceState != null) {
			mMessage = savedInstanceState.getString("mMessage");
			mContactNumber = savedInstanceState.getString("mContactNumber");
			mContactName = savedInstanceState.getString("mContactName");
			mAddress = savedInstanceState.getString("mAddress");
			mLongitude = savedInstanceState.getDouble("mLongitude");
			mLatitude = savedInstanceState.getDouble("mLatitude");
		}

		/* Get the location information from MainActivity */
		Intent intent = getIntent(); //is this necessary?
		mAddress = intent.getStringExtra(MainActivity.ADDRESS);
		String latStr = intent.getStringExtra(MainActivity.LAT);
		String longStr = intent.getStringExtra(MainActivity.LONG);

		if (longStr != null) {
			mLongitude = Double.parseDouble(longStr);	
		}
		if (latStr != null) {
			mLatitude = Double.parseDouble(latStr);
		}

		Log.d(TAG, "In ShareMyLocation: Contact Name: " + mContactName);
		Log.d(TAG, "In ShareMyLocation: Contact Number: " + mContactNumber);

		/* Debugging Purposes */
		if (mAddress != null){
			Log.d(TAG, mAddress + " : " + mLongitude + " : " + mLatitude);
		} else
			Log.d(TAG, "address is null");

		/* Display the current street address to the screen */
		mMessage = "Current Location: " + mAddress + "\n";
		TextView currentAddress = (TextView)findViewById(R.id.editCompleteMessage);
		currentAddress.setTextColor(getResources().getColor(R.color.cyan));
		currentAddress.setText(mMessage);

		/* Add the googlemaps link for the sent message */
		mMessage += "\nExact Coordinates: https://www.google.com/maps?z=18&t=m&q=loc:" + mLatitude + "+" + mLongitude + "\n\n";

		mOptionalMessage = (EditText)findViewById(R.id.editMessage);
		checkSMSLength(mOptionalMessage);
		mOptionalMessage.addTextChangedListener(new TextWatcher() {

		    @Override
		    public void onTextChanged(CharSequence s, int start, int before, int count) {
		        // TODO Auto-generated method stub
		    }

		    @Override
		    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		        // TODO Auto-generated method stub
		    }

		    @Override
		    public void afterTextChanged(Editable s) {
		        // TODO Auto-generated method stub
		        checkSMSLength(mOptionalMessage); // pass your EditText Obj here.
		    }
		});
		
		
		
		/* Obtain the view of the 'Send Button' */
		btnSendSMS = (Button) findViewById(R.id.buttonSend);

		/* Once the user hits the "Send" button */
		btnSendSMS.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{   
				/* Error handling */
				TextView text = (TextView) findViewById(R.id.editPhoneNumber);
				if (mContactName == "" || mContactName == null || mContactName.length() == 0){
					/* AlertDialog box for user confirmation */
					AlertDialog.Builder builder1 = new AlertDialog.Builder(ShareMyLocation.this);
					builder1.setMessage("Please select a contact");
					builder1.setCancelable(true);
					builder1.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					AlertDialog noContactAlert = builder1.create();
					noContactAlert.show();
				} else {
					/* Error handling */
					if (mValidMessage == false){
						Toast.makeText(getBaseContext(), 
								"Message exceeds character limit.", 
								Toast.LENGTH_SHORT).show();
					} else {
						/* Obtain the optional message if any */
//						mOptionalMessage = (EditText)findViewById(R.id.editMessage);
						mStrOptionalMessage = mOptionalMessage.getText().toString();
						Log.d(TAG, "Optional Message" + mStrOptionalMessage);
						
						/* AlertDialog box for user confirmation */
						AlertDialog.Builder builder1 = new AlertDialog.Builder(ShareMyLocation.this);
						builder1.setMessage("Send to " + mContactName + "?");
						builder1.setCancelable(true);
						builder1.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								String phoneNo = mContactNumber;

								if (phoneNo != null && phoneNo.length() > 0) { //Checks whether the number is not null      
									/* Send the user's location */
									sendSMS(phoneNo, mMessage); 
									/* Send the optional message */
									if (mStrOptionalMessage != "" && mStrOptionalMessage != null && !mStrOptionalMessage.isEmpty()) {
										sendSMS(phoneNo, mStrOptionalMessage);
									}
									finish(); //After sending the message, return back to MainActivity
								} else //Throw an exception if the number is invalid
									Toast.makeText(getBaseContext(), 
											"Please select a contact.", 
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
				}
			}
		});  
	}
	
	public void checkSMSLength(EditText edt) throws NumberFormatException {
		int valid_len = 0;
		TextView tvCharactersUsed = (TextView) findViewById(R.id.textCharactersUsed);
		tvCharactersUsed.setTextColor(Color.parseColor("#F8F8F8"));
		try {
			if (edt.getText().toString().length() <= 0) {
				edt.setError(null);
				valid_len = 0;
				tvCharactersUsed.setText("0/160");

			} else if (edt.getText().toString().length() > 160){
				mValidMessage = false;
				edt.setError("Error: Character limit exceeded");
				valid_len = 0;
				tvCharactersUsed.setText("Error");
				tvCharactersUsed.setTextColor(Color.parseColor("#D00000"));
			} else {
				mValidMessage = true;
				edt.setError(null);
				valid_len = edt.getText().toString().length();
				tvCharactersUsed.setText(String.valueOf(valid_len) + "/" + 160);
			}
		} catch (Exception e) {
			Log.e("error", "" + e);
		}

	}


	/* This method sends a text message to a specific phone number */
	private void sendSMS(String phoneNumber, String message){
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

	public void getContacts(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(intent, PICK_CONTACT_REQUEST);
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
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		mMessage = savedInstanceState.getString("mMessage");
		mContactNumber = savedInstanceState.getString("mContactNumber");
		mContactName = savedInstanceState.getString("mContactName");
		mAddress = savedInstanceState.getString("mAddress");
		mLongitude = savedInstanceState.getDouble("mLongitude");
		mLatitude = savedInstanceState.getDouble("mLatitude");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("mMessage", mMessage);
		outState.putString("mContactNumber", mContactNumber);
		outState.putString("mContactName", mContactName);
		outState.putString("mAddress", mAddress);
		outState.putDouble("mLongitude", mLongitude);
		outState.putDouble("mLatitude", mLatitude);
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
	
	/* Method obtains phone number from the contact Uri. */
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT_REQUEST) :
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c =  managedQuery(contactData, null, null, null, null);
				
				if (c.moveToFirst()) {
					String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
					String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
					if (hasPhone.equalsIgnoreCase("1")) {
//						Cursor phones = getContentResolver().query( 
//								ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
//								ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, 
//								null, null);
						Cursor phones = getContentResolver().query(contactData, null, null, null, null);
						if(phones.moveToFirst()){
	//						String cNumber = phones.getString(phones.getColumnIndex("data1"));
							String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							mContactNumber = cNumber;
						}
						//else?
					}
					mContactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					Log.d(TAG, "Contact Name: " + mContactName);
					Log.d(TAG, "Contact Number: " + mContactNumber);
					
				}
			}
		break;
		}
		TextView text = (TextView) findViewById(R.id.editPhoneNumber);
		text.setTextColor(getResources().getColor(R.color.cyan));
		if (mContactNumber != null) {
			text.setText(mContactName);
		}
	}
	
	
}
