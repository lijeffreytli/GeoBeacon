package com.geobeacondev.geobeacon;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class ContactList extends FragmentActivity implements
ActionBar.TabListener {

	private FragmentActivity a = this;

	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;
	// Tab titles
	private String[] tabs = { "All", "Recent" };

	private ArrayList<Contact> mContactList;
	private ArrayList<Contact> mSelectedContactList;
	private ArrayList<Contact> mPrevSelectedContactList;
	private ArrayList<Contact> mRecentContacts;

	private SharedPreferences mPrefs;

	private AllContactsAdapter mAllDataAdapter;
	private RecentContactsAdapter mRecentDataAdapter;

	private ListView allLV;
	private ListView recentLV;

	// Sound
	private SoundPool mSounds;	
	private boolean mSoundOn;
	private int mClickSoundID;

	private static final String TAG = "GEOBEACON_CONTACT_LIST";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "in onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_list_contacts);

		// Initilization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        

		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}

		// on swiping the viewpager make respective tab selected
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		mPrefs = getSharedPreferences("ttt_prefs", Context.MODE_PRIVATE);
		// Get the selected contacts from the caller
		Intent intent = getIntent();
		mPrevSelectedContactList = intent.getParcelableArrayListExtra("SELECTED_CONTACTS");
		initRecentContacts();
	}

	public void initializeAllContactsAdapter(ListView lv) {
		allLV = lv;
		initAllContacts();
		mAllDataAdapter = new AllContactsAdapter(this, R.layout.contact_info, mContactList);
		allLV.setAdapter(mAllDataAdapter);
	}

	public void initializeRecentContactsAdapter(ListView lv) {
		recentLV = lv;
		mRecentDataAdapter = new RecentContactsAdapter(this, R.layout.contact_info, mRecentContacts);
		recentLV.setAdapter(mRecentDataAdapter);
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
		if (mSoundOn && mSounds != null)
			mSounds.play(soundID, 1, 1, 1, 0, 1);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		saveRecentContacts();
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected
		// show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}


	public void exitActivity(View v) {
		Log.d(TAG, "exitActivity");
		playSound(mClickSoundID);
		updateSelectedContacts();
		saveRecentContacts();

		Intent returnIntent = new Intent();
		returnIntent.putExtra("SELECTED_CONTACTS", mSelectedContactList);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	public void sortContacts() {
		Collections.sort(mContactList);
	}

	private void clearRecentContacts() {
		boolean needsUpdate = false;
		for (Contact contact: mRecentContacts) {
			if (contact.selected) {
				Log.d(TAG, "contact " + contact.contactName + " was selected, clearing check in all contacts too");
				needsUpdate = true;
				int i = mContactList.indexOf(contact);
				if (i != -1) {
					Log.d(TAG, "setting allcontacts[" + i + "] to " + false);
					mContactList.get(i).setSelected(false);
				}
				if (mPrevSelectedContactList != null) {
					i = mPrevSelectedContactList.indexOf(contact);
					if (i != -1) {
						Log.d(TAG, "removing selected contacts[" + i + "]");
						mPrevSelectedContactList.remove(i);
					}
				}
			}	
		}
		if (needsUpdate) {
			Log.d(TAG, "needs update, so we are updating");
			mAllDataAdapter = new AllContactsAdapter(a, R.layout.contact_info, mContactList);
			allLV.setAdapter(mAllDataAdapter);
		}
		mRecentContacts.clear();
		Gson gson = new Gson();
		Editor prefsEditor = mPrefs.edit();
		prefsEditor.remove("recentContacts");
		prefsEditor.commit();
		mRecentDataAdapter = new RecentContactsAdapter(a, R.layout.contact_info, mRecentContacts);
		recentLV.setAdapter(mRecentDataAdapter);
	}

	private void initRecentContacts() {
		Log.d(TAG, "in initRecentContacts, is mRecentContacts null? " + (mRecentContacts == null));
		if (mRecentContacts != null)
			return;
		Gson gson = new Gson();
		String recentJson = mPrefs.getString("recentContacts", "");
		java.lang.reflect.Type listType = new TypeToken<ArrayList<Contact>>() {}.getType();
		mRecentContacts = gson.fromJson(recentJson, listType);	
		if (mRecentContacts == null)
			mRecentContacts = new ArrayList<Contact>();	
		for (Contact c: mRecentContacts) {
			Log.v(TAG, "initrec contact " + c.contactName + "is selected? " + c.isSelected());
		}
	}

	private void saveRecentContacts() {
		if (mSelectedContactList == null)
			return;

		for (Contact c: mSelectedContactList) {
			if (!mRecentContacts.contains(c)) {
				mRecentContacts.add(0, c);
			}
		}

		for (Contact c: mRecentContacts) {
			c.selected = false;
		}

		Gson gson = new Gson();
		String recentContactsJSON = gson.toJson(mRecentContacts);
		Editor prefsEditor = mPrefs.edit();
		prefsEditor.putString("recentContacts", recentContactsJSON);
		prefsEditor.commit();
	}

	public class AllContactsAdapter extends ArrayAdapter<Contact>{
		private ArrayList<Contact> contactsList;

		public AllContactsAdapter(Context context, int textViewResourceId, ArrayList<Contact> contactsList){
			super(context, textViewResourceId, contactsList);
			this.contactsList = new ArrayList<Contact>();
			this.contactsList.addAll(contactsList);
		}
		public class ViewHolder {
			TextView phoneNo;
			CheckBox name;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = new ViewHolder();
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.contact_info, null);

				holder.phoneNo = (TextView) convertView.findViewById(R.id.code);
				holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
				final CheckBox fcb = holder.name;
				convertView.setTag(holder);

				holder.name.setOnClickListener(new View.OnClickListener() {  
					public void onClick(View v) {  
						CheckBox cb = (CheckBox)(v);  
						Contact contact = (Contact) cb.getTag();
						boolean value = cb.isChecked();
						contact.setSelected(value);
						String s = value ? "checking" : "unchecking";
						Log.d(TAG, s + " " + contact.contactName);
						int i = mRecentContacts.indexOf(contact);
						if (i != -1) {
							Log.d(TAG, "setting recentcontacts[" + i + "] to " + value);
							mRecentContacts.get(i).setSelected(value);
						} else {
							mRecentContacts.add(0, contact);
						}
						if (mPrevSelectedContactList != null) {
							i = mPrevSelectedContactList.indexOf(contact);
							if (i != -1) {
								Log.d(TAG, "removing selected contacts[" + i + "]");
								mPrevSelectedContactList.remove(i);
							}
						}

						mRecentDataAdapter = new RecentContactsAdapter(a, R.layout.contact_info, mRecentContacts);
						recentLV.setAdapter(mRecentDataAdapter);
					}  
				});  
				holder.phoneNo.setOnClickListener(new View.OnClickListener() {  
					public void onClick(View v) {  
						CheckBox cb = fcb; 
						Contact contact = (Contact) cb.getTag(); 
						cb.setChecked(!cb.isChecked());
						boolean value = cb.isChecked();
						String s = value ? "checking" : "unchecking";
						Log.d(TAG, s + " " + contact.contactName);
						contact.setSelected(value);
						int i = mRecentContacts.indexOf(contact);
						if (i != -1) {
							Log.d(TAG, "setting recentcontacts[" + i + "] to " + value);
							mRecentContacts.get(i).setSelected(value);
						} else {
							mRecentContacts.add(0, contact);
						}
						if (mPrevSelectedContactList != null) {
							i = mPrevSelectedContactList.indexOf(contact);
							if (i != -1) {
								Log.d(TAG, "removing selected contacts[" + i + "]");
								mPrevSelectedContactList.remove(i);
							}
						}
						saveRecentContacts();
						mRecentDataAdapter = new RecentContactsAdapter(a, R.layout.contact_info, mRecentContacts);
						recentLV.setAdapter(mRecentDataAdapter);
					}  
				});
			} 
			else {
				holder = (ViewHolder) convertView.getTag();
			}
			Contact contact = contactsList.get(position);
			Log.d(TAG, "all contact " + contact.contactName + " is selected? " + contact.isSelected());
			if (mPrevSelectedContactList != null && mPrevSelectedContactList.contains(contact))
				contact.setSelected(true);
			holder.phoneNo.setText(contact.getPhoneNo());
			holder.name.setText(contact.getName());
			holder.name.setChecked(contact.isSelected());
			holder.name.setTag(contact);

			return convertView;
		}
	}

	public class RecentContactsAdapter extends ArrayAdapter<Contact>{
		private ArrayList<Contact> contactsList;

		public RecentContactsAdapter(Context context, int textViewResourceId, ArrayList<Contact> contactsList){
			super(context, textViewResourceId, contactsList);
			this.contactsList = new ArrayList<Contact>();
			this.contactsList.addAll(contactsList);
		}
		public class ViewHolder {
			TextView phoneNo;
			CheckBox name;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = new ViewHolder();
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.contact_info, null);

				holder.phoneNo = (TextView) convertView.findViewById(R.id.code);
				holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
				final CheckBox fcb = holder.name;
				convertView.setTag(holder);

				holder.name.setOnClickListener(new View.OnClickListener() {  
					public void onClick(View v) {  
						CheckBox cb = (CheckBox)(v);  
						Contact contact = (Contact) cb.getTag();
						boolean value = cb.isChecked();
						String s = value ? "checking" : "unchecking";
						Log.d(TAG, s + " " + contact.contactName);
						contact.setSelected(value);
						int i = mContactList.indexOf(contact);
						if (i != -1) {
							Log.d(TAG, "setting allcontacts[" + i + "] to " + value);
							mContactList.get(i).setSelected(value);
							mAllDataAdapter = new AllContactsAdapter(a, R.layout.contact_info, mContactList);
							allLV.setAdapter(mAllDataAdapter);
						}
						if (mPrevSelectedContactList != null) {
							i = mPrevSelectedContactList.indexOf(contact);
							if (i != -1) {
								Log.d(TAG, "removing selected contacts[" + i + "]");
								mPrevSelectedContactList.remove(i);
							}
						}
					}  
				});  
				holder.phoneNo.setOnClickListener(new View.OnClickListener() {  
					public void onClick(View v) {  
						CheckBox cb = fcb; 
						Contact contact = (Contact) cb.getTag();  
						cb.setChecked(!cb.isChecked());
						boolean value = cb.isChecked();
						String s = value ? "checking" : "unchecking";
						Log.d(TAG, s + " " + contact.contactName);
						contact.setSelected(value);
						int i = mContactList.indexOf(contact);
						if (i != -1) {
							Log.d(TAG, "setting allcontacts[" + i + "] to " + value);
							mContactList.get(i).setSelected(value);
							mAllDataAdapter = new AllContactsAdapter(a, R.layout.contact_info, mContactList);
							allLV.setAdapter(mAllDataAdapter);
						} 
						if (mPrevSelectedContactList != null) {
							i = mPrevSelectedContactList.indexOf(contact);
							if (i != -1) {
								Log.d(TAG, "removing selected contacts[" + i + "]");
								mPrevSelectedContactList.remove(i);
							}
						}
					}  
				});
			} 
			else {
				holder = (ViewHolder) convertView.getTag();
			}
			Contact contact = contactsList.get(position);
			Log.d(TAG, "recent contact " + contact.contactName + " is selected? " + contact.isSelected());
			if (mPrevSelectedContactList != null && mPrevSelectedContactList.contains(contact))
				contact.setSelected(true);
			holder.phoneNo.setText(contact.getPhoneNo());
			holder.name.setText(contact.getName());
			holder.name.setChecked(contact.isSelected());
			holder.name.setTag(contact);
			Log.d(TAG, "recent contact " + contact.contactName + "'s box is checked? " + holder.name.isChecked());

			return convertView;
		}
	}

	private void initAllContacts(){
		Log.d(TAG, "IN INIT ALL CONTACTS ");
		ArrayList<Contact> contacts = new ArrayList<Contact>();

		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
							null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
							new String[]{id}, null);
					while (pCur.moveToNext()) {
						int phoneType = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
						String phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						Contact contact = new Contact(phoneNumber, name, false);
						//		if (mPrevSelectedContactList != null && mPrevSelectedContactList.contains(contact))
						//		contact.setSelected(true);
						contacts.add(contact);
					} 
					pCur.close();
				}
			}
		}
		mContactList = contacts;
		sortContacts();
	}

	public void updateSelectedContacts(){
		Log.d(TAG, "in init selected contacts");

		mSelectedContactList = new ArrayList<Contact>();

		for (Contact c: mContactList) {
			Log.d(TAG, "all contact " + c.contactName + " is selected? " + (c.isSelected()));
			if (c.isSelected() && !mSelectedContactList.contains(c))
				mSelectedContactList.add(c);
		}

		for (Contact c: mRecentContacts) {
			Log.d(TAG, "recent contact " + c.contactName + " is selected? " + (c.isSelected()));
			if (c.isSelected() && !mSelectedContactList.contains(c))
				mSelectedContactList.add(c);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contact_list, menu);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		switch (item.getItemId()){
		case R.id.action_clear:
			clearRecentContacts();
			return true;
		}
		return false;
	}
}
