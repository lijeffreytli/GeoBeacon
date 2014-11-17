package com.seecondev.seecon;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;

public class Preferences extends PreferenceActivity {
	static final int PICK_CONTACT_REQUEST = 219;
	private ArrayList<Contact> mEmergencyContacts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getPreferenceManager().setSharedPreferencesName("ttt_prefs");
		addPreferencesFromResource(R.xml.preferences);
		final SharedPreferences prefs =
				getSharedPreferences("ttt_prefs", MODE_PRIVATE);
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

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT_REQUEST) :
			if (resultCode == Activity.RESULT_OK) {
				mEmergencyContacts = data.getParcelableArrayListExtra("SELECTED_CONTACTS");
//				Gson gson = new Gson();
	//			String emergencyContactsJSON = gson.to
			}
		break;
		}
	}
}