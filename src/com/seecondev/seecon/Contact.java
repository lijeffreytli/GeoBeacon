package com.seecondev.seecon;

public class Contact implements Comparable<Contact>{
	String contactName = null;
	String phoneNo = null;
	boolean selected = false;
	
	public Contact(String phoneNo, String name, boolean selected){
		super();
		this.contactName = name;
		this.phoneNo = phoneNo;
		this.selected = selected;
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
	

}
