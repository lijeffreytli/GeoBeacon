package com.geobeacondev.geobeacon;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
 
public class RecentContactsFragment extends Fragment {
	private RelativeLayout mRelativeLayout;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
		mRelativeLayout = (RelativeLayout)inflater.inflate(R.layout.fragment_all_contacts, container, false);
		ListView listView = (ListView) mRelativeLayout.findViewById(R.id.listView);
		((ContactList) getActivity()).initializeRecentContactsAdapter(listView);
		return mRelativeLayout;
    }
}