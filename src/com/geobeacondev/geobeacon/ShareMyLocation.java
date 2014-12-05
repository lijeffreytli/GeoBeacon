package com.geobeacondev.geobeacon;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ShareMyLocation extends ActionBarActivity {

	private static final int DIALOG_ABOUT_ID = 1;
	private static final int DIALOG_HELP_ID = 2;
	static final int DIALOG_GETTING_STARTED_ID = 3;

	private static final String DELIVERED_ACTION = "SMS_DELIVERED";
	private static final String SENT_ACTION = "SMS_SENT";

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
	private static final String TAG = "GEOBEACON_SHAREMYLOCATION";
	static final int PICK_CONTACT_REQUEST = 219;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_share_my_location);
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

		/* Get the location information from MainActivity */
		Intent intent = getIntent();
		mAddress = intent.getStringExtra(MainActivity.ADDRESS);
		String latStr = intent.getStringExtra(MainActivity.LAT);
		String longStr = intent.getStringExtra(MainActivity.LONG);

		//get the selected contacts from "Select Contacts button"
		Bundle data = getIntent().getExtras();
		mSelectedContacts = data.getParcelableArrayList("SELECTED_CONTACTS");

		registerReceiver(sentReceiver, new IntentFilter(SENT_ACTION));
		registerReceiver(deliverReceiver, new IntentFilter(DELIVERED_ACTION));

		if (longStr != null) {
			mLongitude = Double.parseDouble(longStr);	
		}
		if (latStr != null) {
			mLatitude = Double.parseDouble(latStr);
		}

		/* Debugging Purposes */
		if (mAddress != null){
			Log.d(TAG, mAddress + " : " + mLongitude + " : " + mLatitude);
		} else
			Log.d(TAG, "address is null");

		/* Display the current street address to the screen */
		mMessage = mAddress;
		TextView currentAddress = (TextView)findViewById(R.id.editCompleteMessage);
		currentAddress.setMovementMethod(new ScrollingMovementMethod());
		currentAddress.setTextColor(getResources().getColor(R.color.katiewhite));
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
					builder.setPositiveButton("OK",
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
							Log.d(TAG, "No Optional Message");
						} else {
							mStrOptionalMessage = mOptionalMessage.getText().toString();
							Log.d(TAG, "Optional Message" + mStrOptionalMessage);
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
									}
								} else {
									Toast.makeText(getBaseContext(), 
											"Please select a contact.", 
											Toast.LENGTH_SHORT).show();
								}


							}

							private void sendToContact(Contact contact) {
								String phoneNo = contact.getPhoneNo();

								String message = "My location: " + mMessage + " ";
								String mapURL = "Map coordinates: " + mMapURL + " ";

								if (phoneNo != null && phoneNo.length() > 0) {    
									/* Send the user's location */
									if (message.length() > 160) {
										int i = 0;
										while (i < message.length()) {
											int endIdx = Math.min(message.length(), i + 160);
											sendSMS(phoneNo, message.substring(i, endIdx));
											i += 160;
										}
										sendSMS(phoneNo, mapURL);
									} 
									else if (message.length() + mapURL.length() < 160) {
										message = message + "\n" + mapURL;
										sendSMS(phoneNo, message); 
									} else {
										sendSMS(phoneNo, message);
										sendSMS(phoneNo, mapURL);
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "in onDestroy");
		unregisterReceiver(deliverReceiver);
		unregisterReceiver(sentReceiver);
	}


	public void getContactList(View view){
		playSound(mSendSoundID);
		ProgressDialog progress = new ProgressDialog(this, R.style.ProgressDialogTheme);
		String message = "Loading Contact List...";
        SpannableString ss1=  new SpannableString(message);
        ss1.setSpan(new RelativeSizeSpan(1.3f), 0, ss1.length(), 0);  
        ss1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss1.length(), 0); 
		progress.setMessage(ss1);
//		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		progress.setMessage(message);
		progress.setTitle("Please wait");
		new LaunchContactsTask(progress).execute();
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
		/* delivery confirmation code adapted from Mike's SMS 
		 * automatic responder, ResponserService.java
		 */
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
				new Intent(SENT_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}

	public void getAdditionalMessage(View view) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.ProgressDialogTheme);

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


		alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				TextView mtvOptionalMessage = (TextView) findViewById(R.id.tvOptionalMessage);
				mtvOptionalMessage.setText(mOptionalMessage.getText().toString());
				mtvOptionalMessage.setTextColor(Color.parseColor("#EBE6C5"));
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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.share_my_location, menu);
		return super.onCreateOptionsMenu(menu);
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
		case R.id.menu_getting_started:
			showDialog(DIALOG_GETTING_STARTED_ID);
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
		case DIALOG_GETTING_STARTED_ID:
			dialog = createGettingStartedDialog(builder);
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
	
	private Dialog createGettingStartedDialog(Builder builder) {
		Context context = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.getting_started_dialog, null); 		
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
		text.setTextColor(getResources().getColor(R.color.katiewhite));
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
		if (mSoundOn && mSounds != null)
			mSounds.play(soundID, 1, 1, 1, 0, 1);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "in on Resume");
		mSoundOn = mPrefs.getBoolean("sound", true);
		createSoundPool();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "in onPause");
		if(mSounds != null) {
			mSounds.release();
			mSounds = null;
		}	
	}
	
    private BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent in) {
        	Log.d(TAG, "in onReceive method of sentReceiver");
        	Log.d(TAG, "result code: " + getResultCode());
        	Log.d(TAG, "result code equals Activity.RESULT_OK " + (getResultCode() == Activity.RESULT_OK));
        	Log.d(TAG, "Context: " + c);
        	Log.d(TAG, "Intent: " + in);
        	if(getResultCode() == Activity.RESULT_OK && in.getAction().equals(SENT_ACTION)) {
        	    Log.d(TAG, "Activity result ok");
        	    smsSent();
        	}
        	else {
        	    Log.d(TAG, "Activity result NOT ok");
        	    smsFailed();
        	}
         }
    };
    
    public void smsSent(){
    	Toast.makeText(this, "SMS sent", Toast.LENGTH_SHORT).show();
    }
    
    public void smsFailed(){
    	Toast.makeText(this, "SMS failed to send", Toast.LENGTH_SHORT).show();
    } 

	private BroadcastReceiver deliverReceiver = new BroadcastReceiver() {
		// this is never getting called

		@Override public void onReceive(Context c, Intent in) {
			//SMS delivered actions
			Log.d(TAG, "in onReceive method of deliverReceiver");
			smsDelivered();
		}    
	};

	public void smsDelivered(){
		Log.d(TAG, "in smsDelivered method");
		Toast.makeText(this, "SMS delivered", Toast.LENGTH_LONG).show();
	}
	
	// http://stackoverflow.com/questions/5202158/how-to-display-progress-dialog-before-starting-an-activity-in-android
	public class LaunchContactsTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress;

		public LaunchContactsTask(ProgressDialog progress) {
			this.progress = progress;
		}

		public void onPreExecute() {
			progress.show();
		}

		public Void doInBackground(Void... unused) {
			Intent intent = new Intent(ShareMyLocation.this, ContactList.class);
			intent.putExtra("SELECTED_CONTACTS", mSelectedContacts);
			startActivityForResult(intent, PICK_CONTACT_REQUEST);
			return null;
		}

		public void onPostExecute(Void unused) {
			progress.dismiss();
		}
	}
}

