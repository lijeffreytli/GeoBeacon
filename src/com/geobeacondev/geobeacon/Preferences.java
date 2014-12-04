package com.geobeacondev.geobeacon;

import java.util.ArrayList;
import android.app.ProgressDialog;


import com.geobeacondev.geobeacon.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.renderscript.Type;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

public class Preferences extends PreferenceActivity {
	private static final String TAG = "GEOBEACON_PREFERENCES";
	static final int PICK_CONTACT_REQUEST = 219;
	private ArrayList<Contact> mEmergencyContacts;
	private SharedPreferences mPrefs;
	
	// Sound
	private SoundPool mSounds;	
	private boolean mSoundOn;
	private int mClickSoundID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getPreferenceManager().setSharedPreferencesName("ttt_prefs");
		addPreferencesFromResource(R.xml.preferences);
		mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
		initializeContacts();

		Preference button = (Preference)findPreference("emergencyContacts");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) { 
				launchContactList();
				return true;
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "in on Resume");
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
	
	private void createSoundPool() {
		mSoundOn = mPrefs.getBoolean("sound", true);
		mSounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		// 2 = maximum sounds to play at the same time,
		// AudioManager.STREAM_MUSIC is the stream type typically used for games
		// 0 is the "the sample-rate converter quality. Currently has no effect. Use 0 for the default."
		mClickSoundID = mSounds.load(this, R.raw.click, 1);
	}
	
	private void playSound(int soundID) {
		// redo this just in case the user changed it
		if(mSounds != null) {
			mSounds.release();
			mSounds = null;
		}
		createSoundPool();
		if (mSoundOn && mSounds != null)
			mSounds.play(soundID, 1, 1, 1, 0, 1);
	}
	
	private void launchContactList() {
		playSound(mClickSoundID);
		ProgressDialog progress = new ProgressDialog(this);
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

	// http://stackoverflow.com/questions/7145606/how-android-sharedpreferences-save-store-object
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT_REQUEST) :
			if (resultCode == Activity.RESULT_OK) {
				mEmergencyContacts = data.getParcelableArrayListExtra("SELECTED_CONTACTS");
				Gson gson = new Gson();
				String emergencyContactsJSON = gson.toJson(mEmergencyContacts);
				Editor prefsEditor = mPrefs.edit();
				prefsEditor.putString("emergencyContacts", emergencyContactsJSON);
				prefsEditor.commit();
			}
		break;
		}
	}

	private void initializeContacts() {
		Gson gson = new Gson();
		String emergencyJson = mPrefs.getString("emergencyContacts", "");
		java.lang.reflect.Type listType = new TypeToken<ArrayList<Contact>>() {}.getType();
		mEmergencyContacts = gson.fromJson(emergencyJson, listType);	
		if (mEmergencyContacts == null)
			mEmergencyContacts = new ArrayList<Contact>();
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
			Intent intent = new Intent(Preferences.this, ContactList.class);
			intent.putExtra("SELECTED_CONTACTS", mEmergencyContacts);
			startActivityForResult(intent, PICK_CONTACT_REQUEST);
			return null;
		}

		public void onPostExecute(Void unused) {
			progress.dismiss();
		}
	}
}