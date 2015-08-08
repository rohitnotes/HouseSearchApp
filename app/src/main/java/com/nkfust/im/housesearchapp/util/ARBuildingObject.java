package com.nkfust.im.housesearchapp.util;

import android.graphics.Bitmap;

public class ARBuildingObject {
	public int id;
	public String buildingname; //建築名稱
	public double lat;			//緯度
	public double lng;			//經度
	public int att;			//高度
	public int color;			//顏色
	public Bitmap bitmap;		//icon
	public ARBuildingObject(int id, String buildingname,double lat, double lng , int att, int color, Bitmap bitmap) {
		super();
		this.id = id;
		this.buildingname = buildingname;
		this.lat = (double)lat;
		this.lng = (double)lng;
		this.att = att;
		this.color = color;
		this.bitmap = bitmap;
	}
	
}
