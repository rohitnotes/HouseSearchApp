package com.nkfust.im.housesearchapp.util;


public class BuildingObject {
	//經度,緯度,建築物標題,建築物介紹,建築物地區編號,建築物地區,建築物主編號
	public BuildingObject( double lat, double lng, 
			String title, String snippet,int id) {
		super();
		this.lat = (double)lat;
		this.lng = (double)lng;
		this.title = title;
		this.snippet = snippet;
		this.id = id;
	}
	public double lat;
	public double lng;
	public String title;
	public String snippet;
	public int id;
}
