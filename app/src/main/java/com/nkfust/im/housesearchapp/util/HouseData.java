package com.nkfust.im.housesearchapp.util;

public class HouseData {

	
    public double lat,lng;
    public String adddress,date,totalprice,amount,place,buildingnumber,bType,bStruct;
	public HouseData(double lat, double lng, String adddress, String date,
			String totalprice, String amount, String place,
			String buildingnumber,  String bStruct) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.adddress = adddress;
		this.date = date;
		this.totalprice = totalprice;
		this.amount = amount;
		this.place = place;
		this.buildingnumber = buildingnumber;
		this.bStruct = bStruct;	
	}

     
    
	
}
