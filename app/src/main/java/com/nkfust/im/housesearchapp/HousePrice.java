package com.nkfust.im.housesearchapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nkfust.im.housesearchapp.util.HouseData;

import java.util.ArrayList;

public class HousePrice extends FragmentActivity implements
OnInfoWindowClickListener {

	private LocationManager locationMgr;

	private ArrayList<LatLng> traceOfMe;
	private String provider;
	private com.google.android.gms.maps.GoogleMap googleMap;
	double lat, lng;
	private LatLng LOCATION;
	private TextView info;

	private HouseData[] housedata = {
			new HouseData(22.74086, 120.32530, "創新路121~150號", "102/9",
					"1,200萬元", "64.47萬元", "土地1建物1", "透天厝", "5房2廳4衛"),

			new HouseData(22.74064, 120.32936, "土庫路271~300號", "103/6", "950萬元",
					"59.43萬元", "土地1建物1", "透天厝", "5房2廳4衛"),
			new HouseData(22.74019, 120.33043, "土庫路272巷1~30號", "102/4",
					"615萬元", "31.20萬元", "土地3建物1", "透天厝", "4房1廳3衛"),

			new HouseData(22.74029, 120.32957, "土庫路241~270號", "102/7", "95萬元",
					"5.417.57萬元", "土地1建物1", "公寓", "2房1廳1衛"),
			new HouseData(22.74015, 120.32966, "土庫路241~270號", "103/3", "57萬元",
					"3.814.90萬元", "土地1建物1", "套房(1房(1廳)1衛)", "1房1廳1衛"),

			new HouseData(22.73779, 120.33108, "土庫路61~90號", "102/4", "1,250萬元",
					"71.57萬元", "土地1建物1", "透天厝", "8房4廳3衛"),

			new HouseData(22.73793, 120.33060, "土庫路93巷1~30號", "103/5",
					"1,035萬元", "54.22萬元", "土地2建物1", "透天厝", "4房2廳3衛"),
			new HouseData(22.73780, 120.32798, "清豐三路389巷1~30號", "102/10",
					"1,750萬元", "107.98萬元", "土地2建物1", "透天厝", "5房2廳6衛"),
			new HouseData(22.73767, 120.32794, "清豐三路389巷1~30號", "102/11",
					"1,755萬元", "92.13萬元", "土地2建物1", "透天厝", "5房2廳5衛"),
			new HouseData(22.73771, 120.32787, "清豐三路361~390號", "102/12",
					"2,075萬元", "96.96萬元", "土地1建物1", "透天厝", "5房2廳5衛"),
			new HouseData(22.73752, 120.32775, "清豐三路391~420號", "103/04",
					"1,935萬元", "95.48萬元", "土地1建物1", "透天厝", "4房2廳5衛"),
			new HouseData(25.03323, 121.55968, "信義路四段391~420號", "102/08",
					"4,058萬元", "85.347.57萬元", "土地2建物1", "", "4房2廳5衛"),
			new HouseData(25.03393, 121.55985, "基隆路一段364巷1~30號", "103/02",
					"3,313萬元", "85.638.70萬元", "土地3建物1", "住宅大樓(11層含以上有電梯)",
					"3房2廳2衛"),
			new HouseData(25.03331, 121.55886, "信義路四段391~420號", "103/06",
					"28,200萬元", "78.3360.19萬元", "土地6建物1", "辦公商業大樓", "6房0廳0衛"),
			new HouseData(25.03199, 121.55821, "文昌街301~330號", "103/04",
					"3,296萬元", "6649.96萬元", "土地1建物1", "辦公商業大樓", "4房1廳0衛"),
			new HouseData(25.03206, 121.55958, "基隆路二段1~30號", "102/03",
					"6,300萬元", "6695.49萬元", "土地2建物2", "", ""),
			new HouseData(25.03454, 121.55998, "基隆路一段391~420號", "102/05",
					"4,630萬元", "79.458.32萬元", "土地1建物1", "辦公商業大樓", "3房2廳1衛"),
			new HouseData(25.03373, 121.55937, "基隆路一段421~450號", "102/02",
					"8,500萬元", "109.577.61萬元", "土地2建物1", "辦公商業大樓", "0房1廳1衛"),
			new HouseData(25.03234, 121.55818, "光復南路481~510號", "102/7",
					"4,500萬元", "72.761.88萬元", "土地1建物1", "辦公商業大樓", "0房2廳0衛"),
			new HouseData(25.03186, 121.55779, "光復南路481~510號", "102/7",
					"4,500萬元", "72.761.88萬元", "土地1建物1", "辦公商業大樓", "0房2廳0衛"),
					
			//new 5 building 在溫州黃金三角店面附近的實價交易建築
			new HouseData(25.021886,121.533636, "新生南路三段31~60號", "103/7",
					"4,988", "96.8", "土地1建物2", "住宅大樓(11層含以上有電梯)", "4房2廳2衛"),
			new HouseData(25.021579,121.534081, "新生南路三段31~60號", "103/4",
					"910", "16.4", "土地1建物1", "住宅大樓(11層含以上有電梯)", "3房2廳2衛"),
			new HouseData(25.020962,121.533947, "新生南路三段31~60號", "102/10",
					"19,000", "106.2", "土地1建物2", "店面（店舖)", "尚無"),
			new HouseData(25.020418,121.533052, "新生南路三段60巷1~30號", "102/2",
					"2,820", "75.9", "土地1建物1", "華廈(10層含以下有電梯)", "3房2廳2衛"),
			new HouseData(25.018935,121.533218, "新生南路三段76巷1~30號", "102/12",
					"795", "34.2", "土地1建物1", "公寓(5樓含以下無電梯)", "尚無")
	};

	private double buildingLat,buildingLng;

	private String buildingName,buildingContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_house_price);

		Bundle bundle = this.getIntent().getExtras();
		buildingLat = bundle.getDouble("buildingLat");			//建築物緯度
		buildingLng = bundle.getDouble("buildingLng");			//建築物經度
		buildingName = bundle.getString("buildingName");
		buildingContent = bundle.getString("buildingContent");
		
		FragmentManager fm = this.getSupportFragmentManager();
		SupportMapFragment mapFragment = new SupportMapFragment() {

			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				View view = super.onCreateView(inflater, container,
						savedInstanceState);

				Bitmap icon = BitmapFactory.decodeResource(getResources(),
						R.drawable.house1);

				googleMap = this.getMap();
				googleMap.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL);
				googleMap.getUiSettings().setRotateGesturesEnabled(true);
				googleMap.getUiSettings().setCompassEnabled(true);
				googleMap.getUiSettings().setZoomControlsEnabled(true);
				googleMap.getUiSettings().setMyLocationButtonEnabled(true);
				googleMap.setOnInfoWindowClickListener(HousePrice.this);

				//地圖中心點為目前房屋（物件）位置的 >（代入經緯度）
			    LatLng Center = new LatLng(buildingLat, buildingLng);
			    
			    //地圖zoom
			    int zoomsize = 16;
			    
			    //設定攝影機中心點與大小
			    CameraPositionChange(Center,zoomsize);
			    
			    MarkerOptions options = new MarkerOptions();
			    options.position(Center);
				options.title(buildingName);
				options.snippet(buildingContent);
				googleMap.addMarker(options);
				
				for (int i = 0; i < housedata.length; i++) {
					lat = housedata[i].lat;
					lng = housedata[i].lng;
					LOCATION = new LatLng(lat, lng);
					options.position(LOCATION);
					options.title(housedata[i].adddress);
					options.snippet("按我詳細資料");
					Bitmap scaled_bmp = Bitmap.createScaledBitmap(icon, 100, 100,true);
					options.icon(BitmapDescriptorFactory.fromBitmap(scaled_bmp));
					googleMap.addMarker(options);
				}
			    
				return view;
			}

		};

		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.map2, mapFragment);
		ft.commit();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		String markerId = marker.getId();
		//取得id，要-1是因為第一個是本身位置的marker
		int id = Integer.valueOf(markerId.replaceAll("m", "")) -1;
		Log.i("MARKERid", "" + id);
		
		if(id>=0) {
			Toast.makeText(getBaseContext(),
					"地址: " + housedata[id].adddress + "\r\n" + "成交價(萬): "
							+ housedata[id].totalprice + "\r\n" + "單價(萬/坪): "
							+ housedata[id].amount + "\r\n" + "交易年月: "
							+ housedata[id].date + "\r\n" + "總面積: "
							+ housedata[id].place + "\r\n" + "建築型態: "
							+ housedata[id].buildingnumber + "\r\n" + "現況格局: "
							+ housedata[id].bStruct + "\r\n", Toast.LENGTH_LONG)
					.show();
		}
	}
	
	private void CameraPositionChange(LatLng latlng, int zoomsize) {
		// Move the camera instantly to hamburg with a zoom of 15.
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomsize));

		// normal view
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomsize), 1000, null);

		// change map view
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(latlng) // Sets the center of the map to Mountain View
				.zoom(zoomsize) // Sets the zoom
//				.bearing(compassdegree) // Sets the orientation of the camera to
										// east
				.tilt(40) // Sets the tilt of the camera to 40 degrees
				.build(); // Creates a CameraPosition from the builder
		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}// CameraPositionChange-END

}
