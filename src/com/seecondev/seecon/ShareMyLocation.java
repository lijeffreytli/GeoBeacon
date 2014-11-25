package com.seecondev.seecon;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ShareMyLocation extends ActionBarActivity {

	static final int DIALOG_ABOUT_ID = 1;
	static final int DIALOG_HELP_ID = 2;

	private Button btnSendSMS;
	private String mMessage;
	private ArrayList<Contact> mSelectedContacts;

	private String mAddress;
	private String mMapURL;
	private double mLatitude;
	private double mLongitude;
	private String mStrOptionalMessage = "";
	private EditText mOptionalMessage;
	private boolean mValidMessage = true;
	private SharedPreferences mPrefs;
	private TextView tvCharCount;

	// Sound
	private SoundPool mSounds;	
	private boolean mSoundOn;
	private int mSendSoundID;

	/* Debugging Purposes */
	private static final String TAG = "SEECON_SHAREMYLOCATION";
	static final int PICK_CONTACT_REQUEST = 219;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_share_my_location);
		//setupUI(findViewById(R.id.parent));

		Log.d(TAG, "in onCreate");

		if (savedInstanceState != null) {
			Log.d(TAG, "in onCreate, savedInstanceState isn't null");
			Log.d(TAG, "char sequence we stored for mOptionalMessage is " + 
					savedInstanceState.getCharSequence("mOptionalMessage").toString());
			mMessage = savedInstanceState.getString("mMessage");
			Log.d(TAG, "mMessage is " + mMessage);
			mSelectedContacts = savedInstanceState.getParcelableArrayList("mSelectedContacts");
			Log.d(TAG, "mSelectedContacts is " + mSelectedContacts);
			mAddress = savedInstanceState.getString("mAddress");
			Log.d(TAG, "mAddress is " + mAddress);
			mLongitude = savedInstanceState.getDouble("mLongitude");
			Log.d(TAG, "mLongitude is " + mLongitude);
			mLatitude = savedInstanceState.getDouble("mLatitude");
			Log.d(TAG, "mLatitude is " + mLatitude);
			TextView tv = (TextView) findViewById(R.id.tvOptionalMessage);
			tv.setText(savedInstanceState.getCharSequence("mOptionalMessage"));
		} else {
			if (mSelectedContacts == null)
				mSelectedContacts = new ArrayList<Contact>();
		}

		mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
		mSoundOn = mPrefs.getBoolean("sound", true);

		/* Get the location information from MainActivity */
		Intent intent = getIntent();
		mAddress = intent.getStringExtra(MainActivity.ADDRESS);
		String latStr = intent.getStringExtra(MainActivity.LAT);
		String longStr = intent.getStringExtra(MainActivity.LONG);

		//get the selected contacts from "Select Contacts button"
		Bundle data = getIntent().getExtras();
		mSelectedContacts = data.getParcelableArrayList("SELECTED_CONTACTS");

		/* DEBUGGING PURPOSES */
//		if (mSelectedContacts == null)
//			Toast.makeText(getBaseContext(), 
//					"No contacts selected.", 
//					Toast.LENGTH_LONG).show();
//		else {
//			Toast.makeText(getBaseContext(), 
//					String.valueOf(mSelectedContacts.size()) + " contact(s) selected.", 
//					Toast.LENGTH_LONG).show();
//		}


		if (longStr != null) {
			mLongitude = Double.parseDouble(longStr);	
		}
		if (latStr != null) {
			mLatitude = Double.parseDouble(latStr);
		}

		//		Log.d(TAG, "In ShareMyLocation: Contact Name: " + mContactName);
		//		Log.d(TAG, "In ShareMyLocation: Contact Number: " + mContactNumber);

		/* Debugging Purposes */
		if (mAddress != null){
			Log.d(TAG, mAddress + " : " + mLongitude + " : " + mLatitude);
		} else
			Log.d(TAG, "address is null");

		/* Display the current street address to the screen */
		mMessage = mAddress;
		TextView currentAddress = (TextView)findViewById(R.id.editCompleteMessage);
		currentAddress.setMovementMethod(new ScrollingMovementMethod());
		currentAddress.setTextColor(getResources().getColor(R.color.cyan));
		currentAddress.setText(mMessage);
		/* Add the googlemaps link for the sent message */
		mMapURL = "https://www.google.com/maps?z=18&t=m&q=loc:" + mLatitude + "+" + mLongitude;

		/* Obtain the view of the 'Send Button' */
		btnSendSMS = (Button) findViewById(R.id.buttonSend);

		/* Once the user hits the "Send" button */
		btnSendSMS.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{   
				playSound(mSendSoundID);
				/* Error handling for null contact */
				if (mSelectedContacts == null || mSelectedContacts.size() == 0){
					/* AlertDialog box for user confirmation */
					AlertDialog.Builder builder = new AlertDialog.Builder(ShareMyLocation.this);
					builder.setMessage("Please select a contact");
					builder.setCancelable(true);
					builder.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					AlertDialog noContactAlert = builder.create();
					noContactAlert.show();
				} else {
					/* Error handling */
					if (mValidMessage == false){
						Toast.makeText(getBaseContext(), 
								"Message exceeds character limit.", 
								Toast.LENGTH_SHORT).show();
					} else {
						/* Obtain the optional message if any */
						// mOptionalMessage = (EditText)findViewById(R.id.editMessage);
						if (mOptionalMessage == null || mOptionalMessage.getText().toString().isEmpty()){
							Log.e(TAG, "No Optional Message");
						} else {
							mStrOptionalMessage = mOptionalMessage.getText().toString();
							Log.e(TAG, "Optional Message" + mStrOptionalMessage);
						}

						/* AlertDialog box for user confirmation */
						AlertDialog.Builder builder = new AlertDialog.Builder(ShareMyLocation.this);

						String contactNames = "";
						for (Contact contact : mSelectedContacts){
							contactNames += contact.getName() + ", ";
						}
						// remove the trailing comma for the last one
						if (contactNames.length() > 2)
							contactNames = contactNames.substring(0, contactNames.length() - 2);

						builder.setMessage("Send to " + contactNames + "?");
						builder.setCancelable(true);
						builder.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (mSelectedContacts != null){
									for (Contact contact: mSelectedContacts) {
										sendToContact(contact);
										Intent returnMain = new Intent(getApplicationContext(), MainActivity.class);
										startActivity(returnMain);
										//finish();
									}
								} else {
									Toast.makeText(getBaseContext(), 
											"Please select a contact.", 
											Toast.LENGTH_SHORT).show();
								}


							}

							private void sendToContact(Contact contact) {
								String phoneNo = contact.getPhoneNo();

								mMessage = "My location: " + mMessage + " ";
								mMapURL = "Map coordinates: " + mMapURL + " ";

								if (phoneNo != null && phoneNo.length() > 0) {    
									/* Send the user's location */
									if (mMessage.length() > 160) {
										int i = 0;
										while (i < mMessage.length()) {
											int endIdx = Math.min(mMessage.length(), i + 160);
											sendSMS(phoneNo, mMessage.substring(i, endIdx));
											i += 160;
										}
										sendSMS(phoneNo, mMapURL);
									} 
									else if (mMessage.length() + mMapURL.length() < 160) {
										mMessage = mMessage + "\n" + mMapURL;
										sendSMS(phoneNo, mMessage); 
									} else {
										sendSMS(phoneNo, mMessage);
										sendSMS(phoneNo, mMapURL);
									}
									/* Send the optional message */
									sendSMS(phoneNo, mStrOptionalMessage);

									finish(); //After sending the message, return back to MainActivity
								} 


							}
						});
						builder.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

						AlertDialog alert = builder.create();
						alert.show();
					}
				}
			}
		});  
		displaySelectedContacts();
	}


	public void getContactList(View view){
		Intent intent = new Intent(this, ContactList.class);
		intent.putExtra("SELECTED_CONTACTS", mSelectedContacts);
		startActivityForResult(intent, PICK_CONTACT_REQUEST);
	}

	/* This method sends a text message to a specific phone number */
	private void sendSMS(String phoneNumber, String message){
		if (message == null || message.isEmpty()){
			return;
		}
		if (phoneNumber == null || phoneNumber.isEmpty()){
			return;
		}
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

	public void getAdditionalMessage(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		LinearLayout layout = new LinearLayout(this);
		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(parms);

		layout.setGravity(Gravity.CLIP_VERTICAL);
		layout.setPadding(2, 2, 2, 2);

		TextView tv = new TextView(this);
		tv.setText("Additional Message");
		tv.setPadding(10, 10, 10, 10);
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(20);

		TextView currentMessage = (TextView) findViewById(R.id.tvOptionalMessage);
		mOptionalMessage = new EditText(this);
		mOptionalMessage.setText(currentMessage.getText());
		mOptionalMessage.setSelection(mOptionalMessage.getText().length());
		mOptionalMessage.setTextColor(Color.parseColor("#000000"));
		tvCharCount = new TextView(this);
		tvCharCount.setText(mOptionalMessage.getText().toString().length() + "/160");

		mOptionalMessage.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				//checkSMSLength(mOptionalMessage); // pass your EditText Obj here.
				int valid_len = 0;
				tvCharCount.setTextColor(Color.parseColor("#000000"));
				try {
					if (mOptionalMessage.getText().toString().length() <= 0) {
						mOptionalMessage.setError(null);
						valid_len = 0;
						tvCharCount.setText("0/160");
						tvCharCount.setTextColor(Color.parseColor("000000"));
					} else if (mOptionalMessage.getText().toString().length() > 160){
						mValidMessage = false;
						mOptionalMessage.setError("Error: Character limit exceeded");
						valid_len = 0;
						tvCharCount.setText("Error");
						tvCharCount.setTextColor(Color.parseColor("#D00000"));
					} else {
						mValidMessage = true;
						mOptionalMessage.setError(null);
						valid_len = mOptionalMessage.getText().toString().length();
						tvCharCount.setText(String.valueOf(valid_len) + "/" + 160);
						tvCharCount.setTextColor(Color.parseColor("000000"));
					}
				} catch (Exception e) {
					Log.e("error", "" + e);
				}
			}
		});

		LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		tv1Params.bottomMargin = 5;

		layout.addView(mOptionalMessage, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(tvCharCount,tv1Params);

		alertDialogBuilder.setView(layout);
		alertDialogBuilder.setTitle("Title");
		alertDialogBuilder.setCustomTitle(tv);


		alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				TextView mtvOptionalMessage = (TextView) findViewById(R.id.tvOptionalMessage);
				mtvOptionalMessage.setText(mOptionalMessage.getText().toString());
				mtvOptionalMessage.setTextColor(Color.parseColor("#FFFFFF"));
			}
		});

		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alertDialogBuilder.show();
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
			startActivityForResult(new Intent(this, Preferences.class),0);  
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("mMessage", mMessage);
		outState.putParcelableArrayList("mSelectedContacts", mSelectedContacts);
		TextView tv = (TextView) findViewById(R.id.tvOptionalMessage);
		outState.putCharSequence("mOptionalMessage", tv.getText());
		outState.putString("mAddress", mAddress);
		outState.putDouble("mLongitude", mLongitude);
		outState.putDouble("mLatitude", mLatitude);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMessage = savedInstanceState.getString("mMessage");
		mSelectedContacts = savedInstanceState.getParcelableArrayList("mSelectedContacts");
		mAddress = savedInstanceState.getString("mAddress");
		mLongitude = savedInstanceState.getDouble("mLongitude");
		mLatitude = savedInstanceState.getDouble("mLatitude");
		TextView tv = (TextView) findViewById(R.id.tvOptionalMessage);
		tv.setText(savedInstanceState.getCharSequence("mOptionalMessage"));
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

	/* Method obtains phone number from the contact Uri.  */
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT_REQUEST) :
		if (resultCode == Activity.RESULT_OK) {
			mSelectedContacts = data.getParcelableArrayListExtra("SELECTED_CONTACTS");
//			ArrayList<Contact> newContacts = data.getParcelableArrayListExtra("SELECTED_CONTACTS");
//			if (mSelectedContacts == null) {
//				mSelectedContacts = new ArrayList<Contact>();
//			}
//			// basically need to get the intersection of the two sets
//			
//			// add any new contacts we didn't have
//			for (Contact contact: newContacts) {
//				if (!mSelectedContacts.contains(contact))
//					mSelectedContacts.add(contact);
//			}
//			// remove any contacts that were unselected
//			for (Contact oldContact: mSelectedContacts) {
//				if (!newContacts.contains(oldContact))
//					mSelectedContacts.remove(oldContact);
//			}
			displaySelectedContacts();
		}
		break;
		case RESULT_CANCELED:
			// Apply potentially new settings
			mSoundOn = mPrefs.getBoolean("sound", true);
			break;
		}
	}

	private String getContactNames() {
		String contactNames = "";
		for (Contact contact: mSelectedContacts) {
			contactNames += contact.contactName + ", ";
		}
		// remove the trailing comma for the last one
		if (contactNames.length() >= 2)
			contactNames = contactNames.substring(0, contactNames.length() - 2);
		return contactNames;
	}
	
	private void displaySelectedContacts(){
		TextView text = (TextView) findViewById(R.id.selectedContacts);
		text.setTextColor(getResources().getColor(R.color.white));
		text.setMovementMethod(new ScrollingMovementMethod());
		if (mSelectedContacts != null){
			String output = getContactNames();
			if (output != null && !output.isEmpty()){
				text.setText(output);
			}
			else
				text.setText("No contacts selected");
		}
	}

	private void createSoundPool() {
		mSounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		// 2 = maximum sounds to play at the same time,
		// AudioManager.STREAM_MUSIC is the stream type typically used for games
		// 0 is the "the sample-rate converter quality. Currently has no effect. Use 0 for the default."
		mSendSoundID = mSounds.load(this, R.raw.click, 1);
	}

	private void playSound(int soundID) {
		if (mSoundOn)
			mSounds.play(soundID, 1, 1, 1, 0, 1);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "in on Resume");
		createSoundPool();
	}

}

