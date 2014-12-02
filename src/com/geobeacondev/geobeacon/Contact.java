package com.geobeacondev.geobeacon;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Comparable<Contact>, Parcelable{
	String contactName = null;
	String phoneNo = null;
	boolean selected = false;
	
	public Contact(String phoneNo, String name, boolean selected){
		super();
		this.contactName = name;
		this.phoneNo = phoneNo;
		this.selected = selected;
	}
	
	public Contact(String phoneNo, String name){
		super();
		this.contactName = name;
		this.phoneNo = phoneNo;
	}
	
	/*Setters*/
	public void setName(String name){
		this.contactName = name;
	}
	public void setPhoneNo(String phoneNo){
		this.phoneNo = phoneNo;
	}
	public void setSelected(boolean selected){
		this.selected = selected;
	}
	
	/* Getters */
	public String getName(){
		return contactName;
	}
	public String getPhoneNo(){
		return phoneNo;
	}
	public boolean isSelected(){
		return selected;
	}
	
	public int compareTo(Contact other) {
		return contactName.compareTo(other.contactName);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	private Contact(Parcel in){
		contactName = in.readString();
		phoneNo = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(contactName);
		dest.writeString(phoneNo);
	}
	
	public String toString() {
		return "Contact Information:\nName: " + contactName + "\nNumber: " + phoneNo;
	}
	
	public boolean equals(Object other){
		if (!(other instanceof Contact))
			return false;
		
		Contact oth = (Contact)(other);
		return ((this.contactName.equals(oth.contactName)) && (this.phoneNo.equals(oth.phoneNo)));
	}
	
	// Just cut and paste this for now
    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

}
