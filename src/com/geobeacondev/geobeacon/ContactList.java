package com.geobeacondev.geobeacon;

import java.util.ArrayList;
import java.util.Collections;

import com.geobeacondev.geobeacon.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactList extends Activity {
	MyCustomAdapter dataAdapter = null;
	static ArrayList<Contact> contactList;
	static ArrayList<Contact> selectedContactList;
	static ArrayList<Contact> prevSelectedContactList;

	/* Debugging Purposes */
	private static final String TAG = "GEOBEACON_SHARE_LOCATION_CONTACTS";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_list_contacts);

		getActionBar().setDisplayHomeAsUpEnabled(false);
		
//		ProgressDialog progress = new ProgressDialog(this);
//		progress.setTitle("Loading");
//		progress.setMessage("Wait while loading...");
//		progress.show();
//		// To dismiss the dialog
//		progress.dismiss();

		/* Get the selected contacts from the caller */
		Intent intent = getIntent(); 
		prevSelectedContactList = intent.getParcelableArrayListExtra("SELECTED_CONTACTS");

		//Generate list View from ArrayList
		displayListView();
		checkButtonClick();
	}

	private void displayListView(){
		//Array list of contacts
		contactList = new ArrayList<Contact>();
		selectedContactList = new ArrayList<Contact>();
		/* Debugging purposes */
		//Contacts contact = new Contacts("15129653082234234234234234245", "AAAJeffrey asdfasdfasdfasdfasdfasdfasdfasdfasdfLi", false);
		//contactList.add(contact);

		/* Iterate through phone and obtain all the contacts and store them into the ArrayList */
		storeAllContacts();
		/* Sort the list of contacts alphabetically */
		Collections.sort(contactList);

		//create an ArrayAdapter from the String Array
		dataAdapter = new MyCustomAdapter(this,
				R.layout.contact_info, contactList);
		ListView listView = (ListView) findViewById(R.id.listView12);
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);
	}

	private void storeAllContacts(){
		Log.d(TAG, "IN STORE ALL CONTACTS ");
		ContentResolver cr = ContactList.this.getContentResolver();
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
						//						String displayNumber = phoneNumber;
						//						Log.d(TAG, name + ": " + phoneNumber);
						//						displayNumber = phoneNumber.replaceAll("\\D+", "");
						//						displayNumber = "+" + displayNumber;
						//						if (displayNumber.length() > 12)
						//							displayNumber = "Invalid Number";
						Contact contact = new Contact(phoneNumber, name, false);
						if (prevSelectedContactList != null && prevSelectedContactList.contains(contact))
							contact.setSelected(true);
						contactList.add(contact);
						//		                  switch (phoneType) {
						//		                        case Phone.TYPE_MOBILE:
						//		                            Log.e(name + "(mobile number)", phoneNumber);
						//		                            break;
						//		                        case Phone.TYPE_HOME:
						//		                            Log.e(name + "(home number)", phoneNumber);
						//		                            break;
						//		                        case Phone.TYPE_WORK:
						//		                            Log.e(name + "(work number)", phoneNumber);
						//		                            break;
						//		                        case Phone.TYPE_OTHER:
						//		                            Log.e(name + "(other number)", phoneNumber);
						//		                            break;                                  
						//		                        default:
						//		                            break;
						//		                  }
					} 
					pCur.close();
				}
			}
		}
	}

	public class MyCustomAdapter extends ArrayAdapter<Contact>{
		private ArrayList<Contact> contactsList;

		public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Contact> contactsList){
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
			if (convertView == null){
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
						contact.setSelected(cb.isChecked());
					}  
				});  
				holder.phoneNo.setOnClickListener(new View.OnClickListener() {  
					public void onClick(View v) {  
						CheckBox cb = fcb; 
						Contact contact = (Contact) cb.getTag();  
						cb.setChecked(!cb.isChecked());
						contact.setSelected(cb.isChecked());
					}  
				});
			} 
			else {
				holder = (ViewHolder) convertView.getTag();
			}
			Contact contact = contactsList.get(position);
			holder.phoneNo.setText(contact.getPhoneNo());
			holder.name.setText(contact.getName());
			holder.name.setChecked(contact.isSelected());
			holder.name.setTag(contact);

			return convertView;
		}
	}
	private void checkButtonClick() {
		Button myButton = (Button) findViewById(R.id.findSelected2);
		myButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StringBuffer responseText = new StringBuffer();

				ArrayList<Contact> contactsList = dataAdapter.contactsList;
				int selected_count = 0;
				for (int i = 0; i < contactsList.size(); ++i){
					Contact contacts = contactsList.get(i);
					if (contacts.isSelected()){
						++selected_count;
					}
				}
				if (selected_count == 0){
					responseText.append("No contacts were selected");
					Toast.makeText(getApplicationContext(),
							responseText, Toast.LENGTH_LONG).show();
				} else {
					String names[] = new String[selected_count];
					int counter = 0;
					for(int i = 0; i < contactsList.size(); i++){
						Contact contacts = contactsList.get(i);
						if(contacts.isSelected()){
							names[counter] = contacts.getName();
							++counter;
						}
					}
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(ContactList.this);
					LayoutInflater inflater = getLayoutInflater();
					View convertView = (View) inflater.inflate(R.layout.alert_share_location_contacts, null);
					alertDialog.setView(convertView);
					alertDialog.setTitle("Shared Contacts");
					ListView lv = (ListView) convertView.findViewById(R.id.listView12);
					ArrayAdapter<String> adapter =new ArrayAdapter<String>(
							ContactList.this,
							android.R.layout.simple_spinner_dropdown_item, names);
					lv.setAdapter(adapter);
					lv.setClickable(false);
					alertDialog.show();
				}
			}
		});
	}
	public void goToShareMyLocation(View view){
		addToSelectedList();
		Intent returnIntent = new Intent();
		returnIntent.putExtra("SELECTED_CONTACTS", selectedContactList);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	public void addToSelectedList(){
		ArrayList<Contact> contactsList = dataAdapter.contactsList;
		int selected_count = 0;
		int counter = 0;
		for (int i = 0; i < contactsList.size(); ++i){
			Contact contacts = contactsList.get(i);
			if (contacts.isSelected()){
				++selected_count;
			}
		}
		String names[] = new String[selected_count];
		for(int i = 0; i < contactsList.size(); i++){
			Contact contacts = contactsList.get(i);
			if(contacts.isSelected()){
				names[counter] = contacts.getName();
				++counter;
				selectedContactList.add(contacts);
			}
		}
	}
}
