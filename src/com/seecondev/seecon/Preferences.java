package com.seecondev.seecon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;

public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getPreferenceManager().setSharedPreferencesName("ttt_prefs");
		addPreferencesFromResource(R.xml.preferences);
		final SharedPreferences prefs =
				getSharedPreferences("ttt_prefs", MODE_PRIVATE);

	}
}
