package com.seecondev.seecon;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class GestureConfirmation extends Activity {

	protected static final String TAG = "GESTUURE_CONFIRMATION";
	// Gesture Confirmation
	private GestureLibrary mLibrary;
	private GestureOverlayView overlay;

	private String mStrOptionalMessage;
	private String mMapURL;
	private String mMessage;
	private SharedPreferences mPrefs;
	
	// Access Emergency Contact List
	ArrayList<Contact> mEmergencyContacts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture_confirmation);

		Intent intent = getIntent();
		mStrOptionalMessage = intent.getStringExtra(Emergency.OPTIONAL_MESSAGE);
		mMessage = intent.getStringExtra(Emergency.MESSAGE);
		mMapURL = intent.getStringExtra(Emergency.MAP_URL);

		/*DEBUGGING PURPOSES*/
//		if (mStrOptionalMessage == null || mStrOptionalMessage.isEmpty()){
//			//Testing purposes
//			Toast.makeText(getApplicationContext(),
//					"No additional message", 
//					Toast.LENGTH_LONG).show();
//		}  else {
//			//Testing purposes
//			Toast.makeText(getApplicationContext(),
//					"Message content: " + mStrOptionalMessage, 
//					Toast.LENGTH_LONG).show();
//		}

		mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!mLibrary.load()) {
			finish();
		}

		overlay = (GestureOverlayView) findViewById(R.id.gestures);
		overlay.addOnGesturePerformedListener(mGestureListener);
		
		mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
		mEmergencyContacts = getEmergencyContacts();
	}

	private GestureOverlayView.OnGesturePerformedListener mGestureListener 
	= new GestureOverlayView.OnGesturePerformedListener() {

		@Override
		public void onGesturePerformed(GestureOverlayView overlay,
				Gesture gesture) {
			// from http://android-developers.blogspot.com/2009/10/gestures-on-android-16.html

			ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

			// We want at least one prediction
			if (predictions.size() > 0) {
				Prediction prediction = predictions.get(0);
				Log.d(TAG, "prediction score: " + prediction.score + ", name: " + prediction.name);
				String figure = null;
				// We want at least some confidence in the result
				if (prediction.score > 3.5) {
					String name = prediction.name;
					if(name.contains("circle-clockwise") || name.contains("circle-counter-clockwise")) {
						figure = prediction.name;
						/* AlertDialog box for user confirmation */
						AlertDialog.Builder builder1 = new AlertDialog.Builder(GestureConfirmation.this);
						builder1.setMessage("Send emergency message?");
						builder1.setCancelable(true);
						builder1.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (mEmergencyContacts != null && mEmergencyContacts.size() != 0){
									for (Contact contact : mEmergencyContacts){
										sendToContact(contact);
										
									}
								} else {
									Toast.makeText(getBaseContext(), 
											"Message sent to emergency personnel.", 
											Toast.LENGTH_LONG).show();
									/* Send to emergency personnel */
//									sendToContact(emergencyPersonnel); //currently not implemented
									finish();
								}
								
								
								
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
							}
							private void sendToContact(Contact contact) {
								String phoneNo = contact.getPhoneNo();

								mMessage = mMessage + " ";
								mMapURL = mMapURL + " ";

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
						builder1.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
						AlertDialog alert11 = builder1.create();
						alert11.show();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							"Please redraw the figure", 
							Toast.LENGTH_LONG).show();
				}
			}
		}
	};
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
	
	private ArrayList<Contact> getEmergencyContacts() {
		Gson gson = new Gson();
		String json = mPrefs.getString("emergencyContacts", "");
		java.lang.reflect.Type listType = new TypeToken<ArrayList<Contact>>() {}.getType();
		ArrayList<Contact> emergencyContacts = gson.fromJson(json, listType);
		return emergencyContacts;
	}
}
