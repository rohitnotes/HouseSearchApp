package com.nkfust.im.housesearchapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nkfust.im.housesearchapp.util.ARBuildingObject;
import com.nkfust.im.housesearchapp.util.Tool;

public class GoogleMap extends FragmentActivity implements LocationListener,OnMarkerClickListener, OnInfoWindowClickListener{
	private static final String TAG = "DM::GoogleMap";
	private LocationManager locationMgr;
	private String provider;
	private com.google.android.gms.maps.GoogleMap googleMap;
	private static final LatLng TWTC_LOCATION = new LatLng(25.034250, 121.562042);
	private static final LatLng NKFUST_LOCATION = new LatLng(22.752970, 120.331400);
	private static final LatLng NTUG_LOCATION = new LatLng(25.021748, 121.535312);//台大體育館
	private ARBuildingObject[] arbuildingItem;
	private Tool tool;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_map);
		
		//tool
		tool = new Tool();
		tool.setContext(getApplicationContext());
		
		// setUpMap();
		FragmentManager fm = this.getSupportFragmentManager();
		SupportMapFragment mapFragment = new SupportMapFragment() {
			@Override
			public View onCreateView(LayoutInflater inflater,
					ViewGroup container, Bundle savedInstanceState) {
				View view = super.onCreateView(inflater, container,	savedInstanceState);
				// 取得GoogleMap
				googleMap = this.getMap();
				googleMap.setMyLocationEnabled(true);
				googleMap.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL);
				googleMap.getUiSettings().setRotateGesturesEnabled(true);
				googleMap.getUiSettings().setCompassEnabled(true);
				googleMap.getUiSettings().setZoomControlsEnabled(true);
				googleMap.getUiSettings().setMyLocationButtonEnabled(true);
				
				//設定Marker Click Listener
				googleMap.setOnMarkerClickListener(GoogleMap.this);
				
				//設定InfoWindow的 Click Listener
				googleMap.setOnInfoWindowClickListener(GoogleMap.this);
				
				//取得Marker icon
				Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.home_badge);
				
				Cursor c = tool.SQLiteRawQuery("SELECT id,buildingname,lat,lng " +
						"FROM building ORDER BY id ASC;");
				arbuildingItem = new ARBuildingObject[c.getCount()];
				c.moveToFirst();
				for(int i=0;i<arbuildingItem.length;i++) {
		        	arbuildingItem[i] = new ARBuildingObject(c.getInt(0),c.getString(1),c.getDouble(2),c.getDouble(3),0,Color.RED,icon);
					// 移至下一筆資料
					c.moveToNext();
		        }
				
				//地圖中心點
			    LatLng Center = NTUG_LOCATION;
			    
			    //地圖zoom
			    int zoomsize = 15;
			    
			    //設定攝影機中心點與大小
			    CameraPositionChange(Center,zoomsize);
				
		        // Marker building;
				for (ARBuildingObject d : arbuildingItem) {
					LatLng loc = new LatLng(d.lat, d.lng);
					Bitmap scaled_bmp=Bitmap.createScaledBitmap(d.bitmap,90,90,true);
//					Bitmap scaled_bmp = d.bitmap;
					// addMarker : 將marker新增至地圖中
					googleMap.addMarker(new MarkerOptions()
							.position(loc)
							.title(d.buildingname)
							.icon(BitmapDescriptorFactory
							.fromBitmap(scaled_bmp)));
				}
		        
				/*======== LOCATION ==========*/
			     // Getting LocationManager object from System Service LOCATION_SERVICE
			    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			    // Creating a criteria object to retrieve provider
			    Criteria criteria = new Criteria();

			    // Getting the name of the best provider
			    String provider = locationManager.getBestProvider(criteria, true);

			    // Getting Current Location
			    Location location = locationManager.getLastKnownLocation(provider);

			    if(location!=null){
			    	onLocationChanged(location);
			    }

			    //回傳目前位置
			    locationManager.requestLocationUpdates(provider, 20000, 0, GoogleMap.this);
				
				return view;
			}
		};
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.map, mapFragment);
		ft.commit();
	}	
	
	@Override
	public void onLocationChanged(Location location) {
		//lat lng
	    double lat = location.getLatitude();
	    double lng = location.getLongitude();
	    //--maps
	    LatLng latlng = new LatLng(lat, lng);
	    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
	    googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
	}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status,
			Bundle extras) {}
	
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

	int clickcount=0;
	String lastclicktitle="";
	@Override
	public boolean onMarkerClick(Marker arg0){return false;}
	
	@Override
	public void onInfoWindowClick(Marker arg0) {
		GoToResult(arg0.getTitle());
	}
	
	private void GoToResult(String Name) {
		Cursor c = tool.SQLiteRawQuery("SELECT id,lat,lng,buildingname,contenttext"
			    + ",price,addr,pattern,footage,age,toward from building "
		        + " WHERE buildingname='"+Name+"';");
        c.moveToFirst();
        //id
        int buildingID = c.getInt(0);
        //lat
        double buildingLat = c.getDouble(1);
        //lng
        double buildingLng = c.getDouble(2);
        //name
        String buildingName = c.getString(3);
        //content
        String buildingContent = c.getString(4);
        //price 
        int buildingPrice = c.getInt(5);
        //addr
        String buildingAddr = c.getString(6);
        //pattern 
        String buildingPattern = c.getString(7);
        //footage
        double buildingFootage = c.getDouble(8);
        //building's age
        double buildingAge = c.getDouble(9);
        //toward 
        String buildingToward = c.getString(10);
        
    	//new一個intent物件，並指定Activity切換的class
    	Intent intent = new Intent();
		intent.setClass(GoogleMap.this, MainActivity.class);
		
		//new一個Bundle物件，並將要傳遞的資料傳入
        Bundle bundle = new Bundle();
        bundle.putInt("buildingID",buildingID);
        bundle.putDouble("buildingLat", buildingLat);
        bundle.putDouble("buildingLng", buildingLng);
        bundle.putString("buildingName",buildingName);
        bundle.putString("buildingContent",buildingContent);
        bundle.putInt("buildingPrice", buildingPrice);
        bundle.putString("buildingAddr", buildingAddr);
        bundle.putString("buildingPattern", buildingPattern);
        bundle.putDouble("buildingFootage", buildingFootage);
        bundle.putDouble("buildingAge", buildingAge);
        bundle.putString("buildingToward", buildingToward);
        //將Bundle物件assign給intent
        intent.putExtras(bundle);
        //切換activity
		startActivity(intent);
		//finish
		finish();
    }
}
