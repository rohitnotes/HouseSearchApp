package com.nkfust.im.housesearchapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nkfust.im.housesearchapp.util.BuildingObject;
import com.nkfust.im.housesearchapp.util.SQLiteDB;
import com.nkfust.im.housesearchapp.util.Sort;

@SuppressWarnings("deprecation")
public class Map extends Activity implements LocationListener ,SensorListener {
	private static final String TAG = "schoolMap_new::Map";
	
	//NKFUST center
	static final LatLng NKFUSTCenter = new LatLng(22.756916, 120.336975);
	//Love River center
	static final LatLng LoveRiverCenter = new LatLng(22.624034, 120.289497);
	//NTUG
	private static final LatLng NTUG_LOCATION = new LatLng(25.021748, 121.535312);//台大體育館
	
	private GoogleMap map;
	private boolean enabledGPS,enabledWiFi;
	private TextView showProvider;
	private String building;

	private SoundPool sp;
	private int Sound = 0;
	private int compassdegree;	
	private LocationManager locationManager;
	private Location location = null;
	
	private String minDistancesName;
	private int minDistance, minDistancePKId;
	
	private SensorManager mSensorManager;
	private float Degress = 0.0f;
	private String provider;
	
	private SQLiteDatabase db;
	private ProgressDialog progressdialog;
	
	//校內建築物
	private BuildingObject[] buildingItem;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				progressdialog.dismiss();
				// Marker building;
				for (BuildingObject d : buildingItem) {
					LatLng loc = new LatLng(d.lat, d.lng);
					// addMarker : 將marker新增至地圖中
					map.addMarker(new MarkerOptions()
							.position(loc)
							.title(d.title)
							.icon(BitmapDescriptorFactory
//							.fromResource(R.drawable.building)));
							.fromResource(R.drawable.houseicon)));
				}
			}
		}
	}; //mHandler-END
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_map);
		//set progressdialog
		progressdialog = new ProgressDialog(this);
		//set view
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);
		map.getUiSettings().setRotateGesturesEnabled(true);
		map.getUiSettings().setCompassEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);
		map.getUiSettings().setMyLocationButtonEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);//MAP_TYPE_NORMAL (一般) MAP_TYPE_HYBRID (3D衛星圖)MAP_TYPE_SATELLITE(平面衛星)
		
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabledGPS = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean enabledWiFi = service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	        
		if (!enabledGPS) {
			Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);

		provider = locationManager.getBestProvider(criteria, true);
	     
		if (provider != null && !provider.equals("")) {
			locationManager.requestLocationUpdates(provider, 1, 1, this);
			onLocationChanged(location);
			((TextView) findViewById(R.id.showProvider)).setText(provider);

		} else {
			Toast.makeText(getBaseContext(), "無法提供定位服務", Toast.LENGTH_SHORT)
					.show();
		}
		
	    //地圖中心點
	    LatLng Center = NTUG_LOCATION;
	    //地圖zoom
	    int zoomsize = 18;
	    //設定攝影機中心點與大小
	    CameraPositionChange(Center,zoomsize);
	
	    //Notice music    
	    setVolumeControlStream(AudioManager.STREAM_MUSIC);
		sp =new SoundPool(4, AudioManager.STREAM_MUSIC,0);
		Sound = sp.load(this, R.raw.annu5, 1);		
		
		//SQLite
		SQLiteSettingAndSelect();	
	}//onCreate-END
	
	
	private void SQLiteSettingAndSelect() {
		
		// progressdialog show
		progressdialog = ProgressDialog.show(this,"讀取資料庫中","請等待...",true);
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// 建立SQLite資料庫
				SQLiteDB sqlitedb = new SQLiteDB(Map.this);
				// 開啟SQLite資料庫
				sqlitedb.OpenDB();
				// 取得SQLite資料庫
				db = sqlitedb.getDB();
				
				// cursor select
				Cursor c = db.rawQuery("SELECT lat,lng,buildingname,contenttext,id FROM building;",null);

				Log.i(TAG, String.valueOf(c.getCount()));
				
				// 移至搜尋出來的第一筆資料
				c.moveToFirst();
				buildingItem = new BuildingObject[c.getCount()];
				for(int i=0;i<c.getCount();i++){
					buildingItem[i] = new BuildingObject(c.getDouble(0), c.getDouble(1),
							c.getString(2), c.getString(3), c.getInt(4));
					Log.i(TAG,c.getString(2));
					// 移至下一筆資料
					c.moveToNext();
				}
				
				
				
				Message msg = new Message();
				msg.what = 0;
				mHandler.sendMessage(msg);
			}
		}).start();
	} // SQLiteSettingAndSelect-END


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_option, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		switch (item.getItemId()) {
		case R.id.item1:
			intent.setClass(Map.this, CameraActivity.class);
			intent.putExtra("cameraType", "Building");
			Bundle bundle = new Bundle();
			bundle.putString("minDistancesName", minDistancesName);
			bundle.putInt("minDistancePKId", minDistancePKId);
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
			break;
			/**
		case R.id.item2:
			intent.setClass(Map.this, Login.class);
			startActivity(intent);
			finish();
			break;
			
		case R.id.item3:
			CameraPositionChange(NKFUSTCenter,18);
			break;
		case R.id.item4:
			CameraPositionChange(LoveRiverCenter,17);
			break;
			*/
		case R.id.item5:
			CameraPositionChange(NTUG_LOCATION,17);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void centerToCurrentLocation(View view){	
		sp.play(Sound, 1.0f, 1.0f, 0, 0, 1.0f);
		if(minDistancesName != null && minDistance != 0){
			String toast = "你現在鄰近" + minDistancesName +"約" + minDistance +"公尺";		    				
			Toast.makeText(this, toast , Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, "無法定位您的位置!" , Toast.LENGTH_SHORT).show();
		} 
	}//centerToCurrentLocation-END
	
	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			double mylatitude = (location.getLatitude() * 1e6) / 1e6;
			double mylongitude = (location.getLongitude() * 1e6) / 1e6;
			((TextView) findViewById(R.id.showLatitude)).setText(String.valueOf(mylatitude));
			((TextView) findViewById(R.id.showLongitude)).setText(String.valueOf(mylongitude));
			getbuildingDistance(mylatitude, mylongitude);
			buildingNearbyMyLocation(mylatitude, mylongitude); // 鄰近建築物
		} 
	}
	
	@Override
	public void onSensorChanged(int sensor, float[] values) {
		synchronized (this) {
			if (sensor == SensorManager.SENSOR_ORIENTATION) {

				if (Math.abs(values[0] - Degress) < 1)
					return;
				switch ((int) values[0]) {

				case 0:
					((TextView) findViewById(R.id.showAccuracy)).setText("正北  0  度");
					compassdegree = (int) values[0];
					break;

				case 90:
					((TextView) findViewById(R.id.showAccuracy)).setText("正東  90  度");
					compassdegree = (int) values[0];
					break;

				case 180:
					((TextView) findViewById(R.id.showAccuracy)).setText("正南  180  度");
					compassdegree = (int) values[0];
					break;

				case 270:
					((TextView) findViewById(R.id.showAccuracy)).setText("正西  270  度");
					compassdegree = (int) values[0];
					break;

				default: {
					int v = (int) values[0];

					if (v > 0 && v < 90) {
						((TextView) findViewById(R.id.showAccuracy)).setText("北偏東 " + String.valueOf(v) + " 度");
						compassdegree = v;
					}
					if (v > 90 && v < 180) {
						((TextView) findViewById(R.id.showAccuracy)).setText("南偏東 " + String.valueOf(v) + " 度");
						compassdegree = v;
					}
					if (v > 180 && v < 270) {
						((TextView) findViewById(R.id.showAccuracy)).setText("南偏西 " + String.valueOf(v) + " 度");
						compassdegree = v;
					}
					if (v > 270 && v < 360) {
						((TextView) findViewById(R.id.showAccuracy)).setText("北偏西  " + String.valueOf(v) + " 度");
						compassdegree = v;
					}

				}

				}
			}
		}
	}

	@Override
	protected void onStart() {
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		super.onStart();
	}

	@Override
	protected void onResume() {
		mSensorManager.registerListener(this, SensorManager.SENSOR_ORIENTATION
				| SensorManager.SENSOR_ACCELEROMETER,SensorManager.SENSOR_DELAY_NORMAL);
		locationManager.requestLocationUpdates(provider, 0, 0, this);
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(this);
		locationManager.removeUpdates(this);
		super.onStop();
	}

	@Override
	protected void onPause() {
		mSensorManager.unregisterListener(this);
		locationManager.removeUpdates(this);
		super.onPause();
	}

    @Override
	public void onAccuracyChanged(int sensor, int accuracy) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(Map.this, "無法開啟定位", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(Map.this, "定位啟動", Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Toast.makeText(Map.this, "更新您的所在位置", Toast.LENGTH_SHORT).show();
	}
	
	// TODO 得到建築物距離
	public void getbuildingDistance(double mylat, double mylng) {
		double dis = 0;
		int[] distmp = new int[buildingItem.length];
		int[] buildingtmp = new int[buildingItem.length];

		for (int i = 0; i < buildingItem.length; i++) {
			double tmpLat = buildingItem[i].lat;
			double tmpLng = buildingItem[i].lng;

			double r = 6378137.0;
			double radLat1 = (mylat * Math.PI / 180.0);
			double radLat2 = (tmpLat * Math.PI / 180.0);
			double d1 = Math.abs(radLat1 - radLat2);
			double d2 = (mylng - tmpLng) * Math.PI / 180.0;
			dis = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(d1 / 2), 2)
					+ Math.cos(radLat1) * Math.cos(radLat2)
					* Math.pow(Math.sin(d2 / 2), 2)));
			dis = dis * r;
			dis = Math.round(dis * 10000) / 10000;
			int temp = (int) dis - 10; // 誤差10公尺

			buildingtmp[i] = temp; // 原始距離未排序
			distmp[i] = temp; // 距離以排序

			Log.d("****location", "mylatitude: " + mylat + " mylongitude: "
					+ mylng + " \n tmpLat: " + tmpLat + " tmpLng: " + tmpLng);
			Log.d("****location", "建築物為:" + buildingItem[i].title
					+ "   建築物距離: " + distmp[i]);

		}

		Sort sort = new Sort();
		distmp = sort.heap(distmp);

		for (int i = 0; i < distmp.length; i++) {
			for (int j = 0; j < buildingtmp.length; j++) {
				if (distmp[i] == buildingtmp[j]) {
					getMinDistanceName(buildingItem[j].title);
					getMinDistance(distmp[i]);
					getMinDistancePKId(buildingItem[j].id);
				}
			}
		}
	}// getbuildingDistance-END
						
	private String getMinDistanceName(String name) {
		this.minDistancesName = name;
		return minDistancesName;
	}

	private int getMinDistance(int distance) {
		this.minDistance = distance;
		return minDistance;
	}

	private int getMinDistancePKId(int id) {
		this.minDistancePKId = id;
		return minDistancePKId;
	}
					
	// TODO 得到提示區域距離
	public int getlimitDistance(double mylat, double mylng, BuildingObject[] item) {

		double dis = 0;
		int distance = 0;
		int[] limittmp = new int[item.length];
		int[] distmp = new int[item.length];

		for (int i = 0; i < item.length; i++) {
			double tmpLat = item[i].lat;
			double tmpLng = item[i].lng;

			double r = 6378137.0; // 地球半徑
			double radLat1 = (mylat * Math.PI / 180.0);
			double radLat2 = (tmpLat * Math.PI / 180.0);
			double d1 = Math.abs(radLat1 - radLat2);
			double d2 = (mylng - tmpLng) * Math.PI / 180.0;

			dis = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(d1 / 2), 2)
					+ Math.cos(radLat1) * Math.cos(radLat2)
					* Math.pow(Math.sin(d2 / 2), 2)));
			dis = dis * r;
			dis = Math.round(dis * 10000) / 10000;

			int temp = (int) dis - 10; // 誤差10公尺
			limittmp[i] = temp; // 原始距離未排序
			distmp[i] = temp; // 距離以排序
		}

		Sort sort = new Sort();
		distmp = sort.heap(distmp);

		for (int i = 0; i < distmp.length; i++) {
			for (int j = 0; j < limittmp.length; j++) {
				if (distmp[i] == limittmp[j]) {
					distance = limittmp[j];
					getlimitName(item[j].snippet);
				}
			}
		}
		return distance;
	}// getlimitDistance-END

			private String getlimitName(String name){
						this.building = name;	
						return building;
			}

	// TODO 偵測使用者是否相鄰建築物
	public void buildingNearbyMyLocation(double mylat, double mylng) {

		if (minDistance <= 30 && minDistance != 0) {// 表示附近有提示區域
			sp.play(Sound, 1.0f, 1.0f, 0, 0, 1.0f);
			((ImageView) findViewById(R.id.noticeImage))
					.setVisibility(View.VISIBLE);
		} else
			((ImageView) findViewById(R.id.noticeImage))
					.setVisibility(View.INVISIBLE);

		
	}// buildingNearbyMyLocation-END
	   
	private void CameraPositionChange(LatLng latlng, int zoomsize) {
		// Move the camera instantly to hamburg with a zoom of 15.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomsize));

		// normal view
		map.animateCamera(CameraUpdateFactory.zoomTo(zoomsize), 1000, null);

		// change map view
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(latlng) // Sets the center of the map to Mountain View
				.zoom(zoomsize) // Sets the zoom
				.bearing(compassdegree) // Sets the orientation of the camera to
										// east
				.tilt(40) // Sets the tilt of the camera to 40 degrees
				.build(); // Creates a CameraPosition from the builder
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}// CameraPositionChange-END
	
}//Map-END
