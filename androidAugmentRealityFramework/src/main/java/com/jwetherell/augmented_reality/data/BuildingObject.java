package com.jwetherell.augmented_reality.data;


public class BuildingObject {
	String title;
	double lat;
	double lng;
	int aaa;
	int color;
	public BuildingObject( String title,double lat, double lng , int aaa, int color) {
		super();
		this.title = title;
		this.lat = (double)lat;
		this.lng = (double)lng;
		this.aaa = aaa;
		this.color = color;
	}
	
}
