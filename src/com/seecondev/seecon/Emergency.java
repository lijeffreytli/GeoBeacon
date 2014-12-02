package com.seecondev.seecon;


import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import android.telephony.gsm.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Emergency extends ActionBarActivity {

	// Dialog Menu
	static final int DIALOG_ABOUT_ID = 1;
	static final int DIALOG_HELP_ID = 2;

	private Button btnSendSMS;
	private String mMessage = "";
	private String mAddress;
	private double mLatitude;
	private double mLongitude;
	EditText mOptionalMessage;
	String mStrOptionalMessage;
	private String mMapURL;
	boolean mValidMessage = true;
	private SharedPreferences mPrefs;
	private TextView tvCharCount;

	// Sound
	private SoundPool mSounds;	
	private boolean mSoundOn;
	private int mSendSoundID;
	
	// Access Emergency Contact List
	ArrayList<Contact> mEmergencyContacts;

	/* Debugging Purposes */
	private static final String TAG = "SEECON_EMERGENCY";
	
	// Intent variables sent to Gesture Confirmation
	public final static String OPTIONAL_MESSAGE = "com.seecondev.seecon.OPTIONAL_MESSAGE";
	public final static String CONTACT_NAME = "com.seecondev.seecon.CONTACT_NAME";
	public final static String MAP_URL = "com.seecondev.seecon.MAP_URL";
	public final static String CONTACT_PHONE_NUMBER = "com.seecondev.seecon.CONTACT_PHONE_NUMBER";
	public final static String MESSAGE = "com.seecondev.seecon.MESSAGE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_emergency);
//		setupUI(findViewById(R.id.parent));
		
		if (savedInstanceState != null) {
			mMessage = savedInstanceState.getString("mMessage");
			TextView tv = (TextView) findViewById(R.id.tvEmergencyOptionalMessage);
			tv.setText(savedInstanceState.getCharSequence("mOptionalMessage"));	
			mAddress = savedInstanceState.getString("mAddress");
			mLongitude = savedInstanceState.getDouble("mLongitude");
			mLatitude = savedInstanceState.getDouble("mLatitude");
			mMapURL = savedInstanceState.getString("mMapURL");
		}

		mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
		mSoundOn = mPrefs.getBoolean("sound", true);

		mEmergencyContacts = getEmergencyContacts();
		
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

		TextView currentAddress = (TextView)findViewById(R.id.editEmergencyAddress);
		currentAddress.setMovementMethod(new ScrollingMovementMethod());
		currentAddress.setTextColor(getResources().getColor(R.color.cyan));
		currentAddress.setText(mAddress);

		/* Obtain the view of the 'Send Button' */
		btnSendSMS = (Button) findViewById(R.id.buttonSendEmergency);
		/* Once the user hits the "Send" button */
		btnSendSMS.setOnClickListener(new View.OnClickListener() 
		{

			public void onClick(View v) 
			{   
				mEmergencyContacts = getEmergencyContacts();
				playSound(mSendSoundID);
				if (mEmergencyContacts.size() == 0){
					/* AlertDialog box for user confirmation */
					AlertDialog.Builder builder1 = new AlertDialog.Builder(Emergency.this);
					builder1.setMessage("Please select emergency contacts in the Settings menu");
					builder1.setCancelable(true);
					builder1.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					AlertDialog alert11 = builder1.create();
					alert11.show();
				} else {
					if (mValidMessage == false){
						Toast.makeText(getBaseContext(), 
								"Message exceeds character limit.", 
								Toast.LENGTH_SHORT).show();
					} else {
						/* Optional message */
						//mOptionalMessage = (EditText)findViewById(R.id.editMessageToEmergency);
						if (mOptionalMessage == null || mOptionalMessage.getText().toString().isEmpty()){
							mStrOptionalMessage = "";
							Log.d(TAG, "No Optional Message");
						} else {
							mStrOptionalMessage = mOptionalMessage.getText().toString();
							Log.d(TAG, "Optional Message" + mStrOptionalMessage);
						}

						/* Obtain spinner spinner information */
						Spinner spinner = (Spinner)findViewById(R.id.spinnerEmergencyDialogs);
						String spinnerText = spinner.getSelectedItem().toString();

						Log.d(TAG, "In Emergency: text: " + spinnerText);

						mMapURL = "Map Coordinates: https://www.google.com/maps?z=18&t=m&q=loc:" + mLatitude + "+" + mLongitude;

						/* This is the message that will be sent to emergency contacts */
						mMessage += "Emergency: " + spinnerText + "\nMy Location: " + 
								mAddress;
						
						Intent myIntent = new Intent(Emergency.this, GestureConfirmation.class);
						myIntent.putExtra(OPTIONAL_MESSAGE, mStrOptionalMessage);
						myIntent.putExtra(MESSAGE, mMessage);
						myIntent.putExtra(MAP_URL, mMapURL);
						Emergency.this.startActivity(myIntent);

//						/* AlertDialog box for user confirmation */
//						AlertDialog.Builder builder1 = new AlertDialog.Builder(Emergency.this);
//						builder1.setMessage("Send emergency message?");
//						builder1.setCancelable(true);
//						builder1.setPositiveButton("Yes",
//								new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int id) {
//								/* Debugging purposes - Send to ourselves */
//								//String phoneNo = mContactNumber;
//								String phoneKatie = "12145976764";
//								String phoneJeff = "15129653085";
//								String phoneJared = "14693942157";
	//
//								sendSMS(phoneJeff, mStrOptionalMessage);
//								//sendSMS(phoneKatie, mStrOptionalMessage);
//								//sendSMS(phoneJared, mStrOptionalMessage);
	//
//								if (mMessage.length() > 160) {
//									int i = 0;
//									while (i < mMessage.length()) {
//										int endIdx = Math.min(mMessage.length(), i + 160);
//										sendSMS(phoneJeff, mMessage.substring(i, endIdx));
//										//sendSMS(phoneKatie, mMessage.substring(i, endIdx));
//										//sendSMS(phoneJared, mMessage.substring(i, endIdx));
//										i += 160;
//									}
//									sendSMS(phoneJeff, mMapURL);
//									//sendSMS(phoneKatie, mMapURL);
//									//sendSMS(phoneJared, mMapURL);
//								} 
//								//							else if (mMessage.length() + mMapURL.length() < 160) {
//								//								mMessage = mMessage + "\n" + mMapURL;
//								//								sendSMS(phoneJeff, mMessage);
//								//								//sendSMS(phoneKatie, mMessage);
//								//								//sendSMS(phoneJared, mMessage);
//								else {
//									sendSMS(phoneJeff, mMessage);
//									//sendSMS(phoneKatie, mMessage);
//									//sendSMS(phoneJared, mMessage);
//									sendSMS(phoneJeff, mMapURL);
//									//sendSMS(phoneKatie, mMapURL);
//									//sendSMS(phoneJared, mMapURL);	
//								}
//								finish();
//							}
//						});
//						builder1.setNegativeButton("No",
//								new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int id) {
//								dialog.cancel();
//							}
//						});
//						AlertDialog alert11 = builder1.create();
//						alert11.show();
					}
				}
			}
		});
	}

	private ArrayList<Contact> getEmergencyContacts() {
		Gson gson = new Gson();
		String json = mPrefs.getString("emergencyContacts", "");
		java.lang.reflect.Type listType = new TypeToken<ArrayList<Contact>>() {}.getType();
		ArrayList<Contact> emergencyContacts = gson.fromJson(json, listType);
		return emergencyContacts;
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

		mOptionalMessage = new EditText(this);
		TextView currentMessage = (TextView) findViewById(R.id.tvEmergencyOptionalMessage);
		mOptionalMessage.setText(currentMessage.getText());
		mOptionalMessage.setSelection(mOptionalMessage.getText().length());
		mOptionalMessage.setTextColor(Color.parseColor("#000000"));
		tvCharCount = new TextView(this);
		tvCharCount.setText(mOptionalMessage.getText().toString().length() + "/160");


		//checkSMSLength(mOptionalMessage);
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
		// alertDialogBuilder.setMessage("Input Student ID");
		alertDialogBuilder.setCustomTitle(tv);


		alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				TextView mtvOptionalMessage = (TextView) findViewById(R.id.tvEmergencyOptionalMessage);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.emergency, menu);
	    return super.onCreateOptionsMenu(menu);
		
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
		
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
		createSoundPool();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("mMessage", mMessage);
		TextView tv = (TextView) findViewById(R.id.tvEmergencyOptionalMessage);
		outState.putCharSequence("mOptionalMessage", tv.getText());
		outState.putString("mAddress", mAddress);
		outState.putDouble("mLongitude", mLongitude);
		outState.putDouble("mLatitude", mLatitude);
		outState.putString("mMapURL", mMapURL);
	}
		
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		if (resultCode ==  RESULT_CANCELED) {
			// Apply potentially new settings
			mSoundOn = mPrefs.getBoolean("sound", true);
		}
	}
}
