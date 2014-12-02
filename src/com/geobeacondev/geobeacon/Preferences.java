package com.geobeacondev.geobeacon;

import java.util.ArrayList;

import com.geobeacondev.geobeacon.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.renderscript.Type;
import android.util.Log;
import android.view.View;

public class Preferences extends PreferenceActivity {
	private static final String TAG = "GEOBEACON_PREFERENCES";
	static final int PICK_CONTACT_REQUEST = 219;
	private ArrayList<Contact> mEmergencyContacts;
	private SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getPreferenceManager().setSharedPreferencesName("ttt_prefs");
		addPreferencesFromResource(R.xml.preferences);
		mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
		mEmergencyContacts = getEmergencyContacts();
		if (mEmergencyContacts == null)
			mEmergencyContacts = new ArrayList<Contact>();
		
		Preference button = (Preference)findPreference("emergencyContacts");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) { 
				Intent intent = new Intent(Preferences.this, ContactList.class);
				intent.putExtra("SELECTED_CONTACTS", mEmergencyContacts);
				startActivityForResult(intent, PICK_CONTACT_REQUEST);
				return true;
			}
		});
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
	
	private ArrayList<Contact> getEmergencyContacts() {
		Gson gson = new Gson();
		String json = mPrefs.getString("emergencyContacts", "");
		java.lang.reflect.Type listType = new TypeToken<ArrayList<Contact>>() {}.getType();
		ArrayList<Contact> emergencyContacts = gson.fromJson(json, listType);
		return emergencyContacts;
	}
	
}