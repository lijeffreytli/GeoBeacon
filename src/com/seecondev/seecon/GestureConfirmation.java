package com.seecondev.seecon;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture_confirmation);

		Intent intent = getIntent();
		mStrOptionalMessage = intent.getStringExtra(Emergency.OPTIONAL_MESSAGE);
		mMessage = intent.getStringExtra(Emergency.MESSAGE);
		mMapURL = intent.getStringExtra(Emergency.MAP_URL);


		//Testing purposes
		Toast.makeText(getApplicationContext(),
				"Sent message: " + mStrOptionalMessage, 
				Toast.LENGTH_LONG).show();

		mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!mLibrary.load()) {
			finish();
		}

		overlay = (GestureOverlayView) findViewById(R.id.gestures);
		overlay.addOnGesturePerformedListener(mGestureListener);
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
				if (prediction.score > 5.0) {
					String name = prediction.name;
					if(name.contains("triangle") || name.contains("circle")) {
						figure = prediction.name;
						/* AlertDialog box for user confirmation */
						AlertDialog.Builder builder1 = new AlertDialog.Builder(GestureConfirmation.this);
						builder1.setMessage("Send emergency message?");
						builder1.setCancelable(true);
						builder1.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								/* Debugging purposes - Send to ourselves */
								//String phoneNo = mContactNumber;
								String phoneKatie = "12145976764";
								String phoneJeff = "15129653085";
								String phoneJared = "14693942157";

								sendSMS(phoneJeff, mStrOptionalMessage);
								//sendSMS(phoneKatie, mStrOptionalMessage);
								//sendSMS(phoneJared, mStrOptionalMessage);

								if (mMessage.length() > 160) {
									int i = 0;
									while (i < mMessage.length()) {
										int endIdx = Math.min(mMessage.length(), i + 160);
										sendSMS(phoneJeff, mMessage.substring(i, endIdx));
										//sendSMS(phoneKatie, mMessage.substring(i, endIdx));
										//sendSMS(phoneJared, mMessage.substring(i, endIdx));
										i += 160;
									}
									sendSMS(phoneJeff, mMapURL);
									//sendSMS(phoneKatie, mMapURL);
									//sendSMS(phoneJared, mMapURL);
								} 
								//							else if (mMessage.length() + mMapURL.length() < 160) {
								//								mMessage = mMessage + "\n" + mMapURL;
								//								sendSMS(phoneJeff, mMessage);
								//								//sendSMS(phoneKatie, mMessage);
								//								//sendSMS(phoneJared, mMessage);
								else {
									sendSMS(phoneJeff, mMessage);
									//sendSMS(phoneKatie, mMessage);
									//sendSMS(phoneJared, mMessage);
									sendSMS(phoneJeff, mMapURL);
									//sendSMS(phoneKatie, mMapURL);
									//sendSMS(phoneJared, mMapURL);	
								}
								finish();
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
}
