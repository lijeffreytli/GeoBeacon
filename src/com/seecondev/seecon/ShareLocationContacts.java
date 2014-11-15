package com.seecondev.seecon;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
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

public class ShareLocationContacts extends Activity {
	MyCustomAdapter dataAdapter = null;
	static ArrayList<Contact> contactList;
	static ArrayList<Contact> selectedContactList;
	



	/* Debugging Purposes */
	private static final String TAG = "SEECON_SHARE_LOCATION_CONTACTS";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_location_contacts);

		getActionBar().setDisplayHomeAsUpEnabled(false);

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

		//create an ArrayAdaptar from the String Array
		dataAdapter = new MyCustomAdapter(this,
				R.layout.contact_info, contactList);
		ListView listView = (ListView) findViewById(R.id.listView12);
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				Contact contact = (Contact) parent.getItemAtPosition(position);
				Toast.makeText(getApplicationContext(),
						"Clicked on Row: " + contact.getName(), 
						Toast.LENGTH_LONG).show();
			}
		});
	}

	private void storeAllContacts(){
		Log.d(TAG, "IN STORE ALL CONTACTS ");
		ContentResolver cr = ShareLocationContacts.this.getContentResolver();
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

	private class MyCustomAdapter extends ArrayAdapter<Contact>{
		private ArrayList<Contact> contactsList;

		public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Contact> contactsList){
			super(context, textViewResourceId, contactsList);
			this.contactsList = new ArrayList<Contact>();
			this.contactsList.addAll(contactsList);
		}
		private class ViewHolder {
			TextView phoneNo;
			CheckBox name;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null){
				LayoutInflater vi = (LayoutInflater)getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.contact_info, null);

				holder = new ViewHolder();
				holder.phoneNo = (TextView) convertView.findViewById(R.id.code);
				holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
				convertView.setTag(holder);

				holder.name.setOnClickListener( new View.OnClickListener() {  
					public void onClick(View v) {  
						CheckBox cb = (CheckBox) v ;  
						Contact contact = (Contact) cb.getTag();  
						boolean checked;
						String isChecked;
						if (cb.isChecked() == true){
							isChecked = "selected";
						} else {
							isChecked = "deselected";
						}
						Toast.makeText(getApplicationContext(),
								cb.getText() +
								" is " + isChecked, 
								Toast.LENGTH_LONG).show();
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
					for(int i=0;i<contactsList.size();i++){
						Contact contacts = contactsList.get(i);
						if(contacts.isSelected()){
							names[counter] = contacts.getName();
							++counter;
							//selectedContactList.add(contacts);
						}
					}
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShareLocationContacts.this);
					LayoutInflater inflater = getLayoutInflater();
					View convertView = (View) inflater.inflate(R.layout.alert_share_location_contacts, null);
					alertDialog.setView(convertView);
					alertDialog.setTitle("Shared Contacts");
					ListView lv = (ListView) convertView.findViewById(R.id.listView12);
					ArrayAdapter<String> adapter =new ArrayAdapter<String>(
							ShareLocationContacts.this,
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
		Intent intent = new Intent(this, ShareMyLocation.class);
		//TODO: pass the arraylist of names to the ShareMyLocation 
		intent.putExtra("CONTACTS", new Contact("Hi", "Hi", false));
		//Output the number of contacts selected
		Log.e(TAG, String.valueOf(selectedContactList.size()));
		intent.putExtra("SELECTED_CONTACTS", selectedContactList);
		startActivity(intent);
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
		for(int i=0;i<contactsList.size();i++){
			Contact contacts = contactsList.get(i);
			if(contacts.isSelected()){
				names[counter] = contacts.getName();
				++counter;
				selectedContactList.add(contacts);
			}
		}
	}
}
