package com.seecondev.seecon;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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


//Courtesy: http://www.mysamplecode.com/2012/07/android-listview-checkbox-example.html
public class EmergencyContacts extends Activity {
	MyCustomAdapter dataAdapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emergency_contacts);

		getActionBar().setDisplayHomeAsUpEnabled(false);

		//Generate list View from ArrayList
		displayListView();
		checkButtonClick();

	}
	private void displayListView(){
		//Array list of contacts
		ArrayList<Contacts> contactList = new ArrayList<Contacts>();
		Contacts contact = new Contacts("15129653085", "Jeffrey Li", false);
		contactList.add(contact);
		//TODO /*Iterate through phone and obtain all contacts*/

		//create an ArrayAdaptar from the String Array
		dataAdapter = new MyCustomAdapter(this,
				R.layout.contact_info, contactList);
		ListView listView = (ListView) findViewById(R.id.listView1);
		// Assign adapter to ListView
		listView.setAdapter(dataAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				Contacts contact = (Contacts) parent.getItemAtPosition(position);
				Toast.makeText(getApplicationContext(),
						"Clicked on Row: " + contact.getName(), 
						Toast.LENGTH_LONG).show();
			}
		});
	}

	private class MyCustomAdapter extends ArrayAdapter<Contacts>{
		private ArrayList<Contacts> contactsList;

		public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Contacts> contactsList){
			super(context, textViewResourceId, contactsList);
			this.contactsList = new ArrayList<Contacts>();
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
						Contacts contact = (Contacts) cb.getTag();  
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
			Contacts contact = contactsList.get(position);
			holder.phoneNo.setText(" (#" +  contact.getPhoneNo() + ")");
			holder.name.setText(contact.getName());
			holder.name.setChecked(contact.isSelected());
			holder.name.setTag(contact);

			return convertView;
		}
	}
	private void checkButtonClick() {


		Button myButton = (Button) findViewById(R.id.findSelected);
		myButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				StringBuffer responseText = new StringBuffer();



				ArrayList<Contacts> contactsList = dataAdapter.contactsList;
				int selected_count = 0;
				for (int i = 0; i < contactsList.size(); ++i){
					Contacts contacts = contactsList.get(i);
					if (contacts.isSelected()){
						++selected_count;
					}
				}

				if (selected_count == 0){
					responseText.append("No contacts were selected");
				} else {
					responseText.append("The following contacts were selected:");
					for(int i=0;i<contactsList.size();i++){
						Contacts contacts = contactsList.get(i);
						if(contacts.isSelected()){
							responseText.append("\n" + contacts.getName());
						}
					}
				}
				Toast.makeText(getApplicationContext(),
						responseText, Toast.LENGTH_LONG).show();
			}
		});
	}
}

