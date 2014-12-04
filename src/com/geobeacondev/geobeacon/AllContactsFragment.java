package com.geobeacondev.geobeacon;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class AllContactsFragment extends Fragment {

	private RelativeLayout mRelativeLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mRelativeLayout = (RelativeLayout)inflater.inflate(R.layout.fragment_all_contacts, container, false);

		ListView listView = (ListView) mRelativeLayout.findViewById(R.id.listView);
		((ContactList) getActivity()).initializeAllContactsAdapter(listView);
		return mRelativeLayout;
	}
}