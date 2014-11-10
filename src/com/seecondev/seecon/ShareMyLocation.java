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
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.telephony.gsm.SmsManager;
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
	//private String mContactNumber;
	//private String mContactName;
	private ArrayList<SeeconContact> mContacts;
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


		if (savedInstanceState != null) {
			Log.d(TAG, "in onCreate, savedInstanceState isn't null");
			Log.d(TAG, "char sequence we stored for mOptionalMessage is " + 
					savedInstanceState.getCharSequence("mOptionalMessage").toString());
			mMessage = savedInstanceState.getString("mMessage");
			mContacts = savedInstanceState.getParcelableArrayList("mContacts");
			mAddress = savedInstanceState.getString("mAddress");
			mLongitude = savedInstanceState.getDouble("mLongitude");
			mLatitude = savedInstanceState.getDouble("mLatitude");
			TextView tv = (TextView) findViewById(R.id.tvOptionalMessage);
			tv.setText(savedInstanceState.getCharSequence("mOptionalMessage"));
		} else {
			mContacts = new ArrayList<SeeconContact>();
		}

		mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
		mSoundOn = mPrefs.getBoolean("sound", true);

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
		//		mOptionalMessage = (EditText)findViewById(R.id.editMessage);
		//		checkSMSLength(mOptionalMessage);
		//		mOptionalMessage.addTextChangedListener(new TextWatcher() {
		//
		//			@Override
		//			public void onTextChanged(CharSequence s, int start, int before, int count) {
		//			}
		//
		//			@Override
		//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		//			}
		//
		//			@Override
		//			public void afterTextChanged(Editable s) {
		//				checkSMSLength(mOptionalMessage); // pass your EditText Obj here.
		//			}
		//		});



		/* Obtain the view of the 'Send Button' */
		btnSendSMS = (Button) findViewById(R.id.buttonSend);

		/* Once the user hits the "Send" button */
		btnSendSMS.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{   
				playSound(mSendSoundID);
				/* Error handling for null contact */
				if (mContacts == null || mContacts.size() == 0){
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
						// mOptionalMessage = (EditText)findViewById(R.id.editMessage);
						if (mOptionalMessage == null || mOptionalMessage.getText().toString().isEmpty()){
							Log.e(TAG, "No Optional Message");
						} else {
							mStrOptionalMessage = mOptionalMessage.getText().toString();
							Log.e(TAG, "Optional Message" + mStrOptionalMessage);
						}

						/* AlertDialog box for user confirmation */
						AlertDialog.Builder builder1 = new AlertDialog.Builder(ShareMyLocation.this);

						String contactNames = "";
						for (SeeconContact contact: mContacts) {
							contactNames += contact.mContactName + ", ";
						}
						// remove the trailing comma for the last one
						contactNames = contactNames.substring(0, contactNames.length() - 2);

						builder1.setMessage("Send to " + contactNames + "?");
						builder1.setCancelable(true);
						builder1.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								for (SeeconContact contact: mContacts) {
									sendToContact(contact);
								}
							}

							private void sendToContact(SeeconContact contact) {
								String phoneNo = contact.mContactNumber;

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

	//	public void checkSMSLength(EditText edt) throws NumberFormatException {
	//		int valid_len = 0;
	//		TextView tvCharactersUsed = (TextView) findViewById(R.id.textCharactersUsed);
	//		tvCharactersUsed.setTextColor(Color.parseColor("#F8F8F8"));
	//		try {
	//			if (edt.getText().toString().length() <= 0) {
	//				edt.setError(null);
	//				valid_len = 0;
	//				tvCharactersUsed.setText("0/160");
	//
	//			} else if (edt.getText().toString().length() > 160){
	//				mValidMessage = false;
	//				edt.setError("Error: Character limit exceeded");
	//				valid_len = 0;
	//				tvCharactersUsed.setText("Error");
	//				tvCharactersUsed.setTextColor(Color.parseColor("#D00000"));
	//			} else {
	//				mValidMessage = true;
	//				edt.setError(null);
	//				valid_len = edt.getText().toString().length();
	//				tvCharactersUsed.setText(String.valueOf(valid_len) + "/" + 160);
	//			}
	//		} catch (Exception e) {
	//			Log.e("error", "" + e);
	//		}
	//	}





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

	public void getContacts(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(intent, PICK_CONTACT_REQUEST);
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
		case R.id.menu_emergency_contacts:
			Intent intent = new Intent(this, EmergencyContacts.class);
			this.startActivity(intent);
			break;
		}
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("mMessage", mMessage);
		outState.putParcelableArrayList("mContacts", mContacts);
		TextView tv = (TextView) findViewById(R.id.tvOptionalMessage);
		Log.d(TAG, "tv.getText is " + tv.getText().toString());
		outState.putCharSequence("mOptionalMessage", tv.getText());
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
					String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
					String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
					if (hasPhone.equalsIgnoreCase("1")) {
						//						Cursor phones = getContentResolver().query( 
						//								ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
						//								ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, 
						//								null, null);
						Cursor phones = getContentResolver().query(contactData, null, null, null, null);
						if(phones.moveToFirst()){
							//						String cNumber = phones.getString(phones.getColumnIndex("data1"));
							String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							Log.d(TAG, "cNumber is  " + cNumber);
							String cName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
							Log.d(TAG, "cName is " + cName);
							SeeconContact contact = new SeeconContact(cName, cNumber);
							Log.d(TAG, "contact is " + contact);
							if (!mContacts.contains(contact))
								mContacts.add(contact);
						}
						//else?
					}

					for (SeeconContact contact: mContacts) {
						Log.d(TAG, "Contact Name: " + contact.mContactName);
						Log.d(TAG, "Contact Number: " + contact.mContactNumber);
					}

				}
			}
		break;
		case RESULT_CANCELED:
			// Apply potentially new settings
			mSoundOn = mPrefs.getBoolean("sound", true);
			break;
		}
		TextView text = (TextView) findViewById(R.id.selectedContacts);
		text.setTextColor(getResources().getColor(R.color.white));
		String contactNames = "";
		for (SeeconContact contact: mContacts) {
			contactNames += contact.mContactName + ", ";
		}
		// remove the trailing comma for the last one
		if (contactNames.length() >= 2)
			contactNames = contactNames.substring(0, contactNames.length() - 2);
		Log.d(TAG, "contactNames: " + contactNames);

		if (contactNames != null && !contactNames.isEmpty()) {
			text.setText(contactNames);
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
		createSoundPool();
	}
	//	/* http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext */
	//	public static void hideSoftKeyboard(Activity activity) {
	//		InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
	//		inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	//	}
	//	/* same source as above */
	//	public void setupUI(View view) {
	//		//Set up touch listener for non-text box views to hide keyboard.
	//		if(!(view instanceof EditText)) {
	//			view.setOnTouchListener(new OnTouchListener() {
	//				public boolean onTouch(View v, MotionEvent event) {
	//					hideSoftKeyboard(ShareMyLocation.this);
	//					return false;
	//				}
	//			});
	//		}
	//
	//		//If a layout container, iterate over children and seed recursion.
	//		if (view instanceof ViewGroup) {
	//			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
	//				View innerView = ((ViewGroup) view).getChildAt(i);
	//				setupUI(innerView);
	//			}
	//		}
	//	}
	private class SeeconContact implements Parcelable {
		private String mContactName;
		private String mContactNumber;

		public SeeconContact(String contactName, String contactNumber) {
			mContactName = contactName;
			mContactNumber = contactNumber;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(mContactName);
			dest.writeString(mContactNumber);
		}

		public String toString() {
			return "Contact Information:\nName: " + mContactName + "\nNumber: " + mContactNumber;
		}

		public boolean equals(Object other) {
			if (!(other instanceof SeeconContact))
				return false;

			SeeconContact oth = (SeeconContact)(other);
			return ((this.mContactName.equals(oth.mContactName)) && (this.mContactNumber.equals(oth.mContactNumber)));
		}

	}
}

