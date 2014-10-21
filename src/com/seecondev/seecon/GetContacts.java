package com.seecondev.seecon;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class GetContacts extends Activity {
	
	/* Stores the indicated contact's phone number */
	public String contactNumber; 
	/* Stores the indicated contact's name (Currently not used) */
	public String contactName;
	/* Request code for contracts */
	private static final int CONTACT_CODE = 1;
	/* Debugging Purposes */
	private static final String TAG = "Tag";
	
	public final static String CONTACT_NUMBER = "com.seecondev.seecon.CONTACTNUM";
	public final static String CONTACT_NAME = "com.seecondev.seecon.CONTACTNAME";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_contacts);
		
		Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(intent, CONTACT_CODE);
	}
	
	/* Method obtains phone number from the contact Uri. */
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (CONTACT_CODE) :
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c =  managedQuery(contactData, null, null, null, null);
				
				if (c.moveToFirst()) {
					String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
					String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
					if (hasPhone.equalsIgnoreCase("1")) {
//						Cursor phones = getContentResolver().query( 
//								ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
//								ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, 
//								null, null);
						Cursor phones = getContentResolver().query(contactData, null, null, null, null);
						if(phones.moveToFirst()){
	//						String cNumber = phones.getString(phones.getColumnIndex("data1"));
							String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							contactNumber = cNumber;
						}
						//else?
					}
					contactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					Log.d(TAG, "Contact Name: " + contactName);
					Log.d(TAG, "Contact Number: " + contactNumber);
					
					/* Return back to previous activity with contact information */
					Intent intent = new Intent(this, ShareMyLocation.class);
					intent.putExtra(CONTACT_NAME, contactName);
					intent.putExtra(CONTACT_NUMBER, contactNumber);
					startActivity(intent);
				}
			}
		break;
		}
		Log.d(TAG, "WRONG");
		finish(); //After sending the message, return back to MainActivity
	}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.get_contacts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
