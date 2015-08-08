package com.nkfust.im.housesearchapp;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.nkfust.im.housesearchapp.util.Tool;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "HouseSearchApp::MainActivity";
	private Tool tool;
	private ImageButton img;
	//建立Bitmap陣列，存放該建築物的所有圖片
	private Bitmap[] bmpBuildings;
	private int buildingID,buildingPrice;
	private double buildingLat,buildingLng,buildingFootage,buildingAge;
	private String buildingName,buildingContent,buildingAddr,buildingPattern,buildingToward,buildingTable;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			             WindowManager.LayoutParams.FLAG_FULLSCREEN);	
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		
		//tool
        tool = new Tool();
      	tool.setContext(getApplicationContext());
		
		//接收intent中的bundle物件
	    Bundle bundle = getIntent().getExtras();
		buildingID = bundle.getInt("buildingID");				//建築物編號
		buildingLat = bundle.getDouble("buildingLat");			//建築物緯度
		buildingLng = bundle.getDouble("buildingLng");			//建築物經度
		buildingName = bundle.getString("buildingName");		//建築物名稱
		buildingContent = bundle.getString("buildingContent");	//建築物內容
		buildingPrice = bundle.getInt("buildingPrice");			//建築物價格
	    buildingAddr = bundle.getString("buildingAddr");		//建築物地址
	    buildingPattern = bundle.getString("buildingPattern");	//建築物格局
	    buildingFootage = bundle.getDouble("buildingFootage");	//建築物坪數
	    buildingAge = bundle.getDouble("buildingAge");			//建築物屋齡
	    buildingToward = bundle.getString("buildingToward");	//建築物朝向
	    String path = tool.getBuildingDBpath();
	    SetView();
	    
		//取得該建築物的所有圖片Bitmap
		ArrayList<String> buildingArrayList = tool.getfolderfileName(path+"/"+buildingID);
		
		//宣告Bitmap陣列數量
		bmpBuildings = new Bitmap[buildingArrayList.size()];
		
		//將每張圖放入bitmap
		for(int i=0;i<buildingArrayList.size();i++){
			bmpBuildings[i] = BitmapFactory.decodeFile(buildingArrayList.get(i));
		}
		
		//設定畫廊
		Gallery gallery = (Gallery)findViewById(R.id.gallery1);
		ImageAdapter imageAdapter = new ImageAdapter(this);
		img = (ImageButton)findViewById(R.id.imageButton1);
		img.setScaleType(ScaleType.FIT_XY);
		img.getBackground().setAlpha(0);
		gallery.setAdapter(imageAdapter);
	}
	
	private void SetView() {
		//設定標題
		TextView title = (TextView)findViewById(R.id.title); title.setText(buildingName);	
		TextView priceTV = (TextView)findViewById(R.id.price2); priceTV.setText(buildingPrice+"萬");
		TextView addrTV = (TextView)findViewById(R.id.address2); addrTV.setText(buildingAddr);
		TextView patternTV = (TextView)findViewById(R.id.inside2); patternTV.setText(buildingPattern);
		TextView footageTV = (TextView)findViewById(R.id.size2); footageTV.setText(buildingFootage+"坪");
		TextView ageTV = (TextView)findViewById(R.id.year2); ageTV.setText(buildingAge+"年");
		TextView towardTV = (TextView)findViewById(R.id.orentation2); towardTV.setText(buildingToward);
		//設定按鈕事件
		ImageButton img1 = (ImageButton)this.findViewById(R.id.imageButton1);
		img1.setOnClickListener(this);
		ImageButton img2 = (ImageButton)this.findViewById(R.id.imageButton2);
		img2.setOnClickListener(this);
		ImageButton img3 = (ImageButton)this.findViewById(R.id.imageButton3);
		img3.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.imageButton1: //街景
			String uri = "google.streetview:cbll="+buildingLat+","+buildingLng;
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW,Uri.parse(uri));
			startActivity(intent);
			break;
		case R.id.imageButton2: //地圖
			Intent intent1 = new Intent();
			intent1.setClass(MainActivity.this, GoogleMap.class);
			//new一個Bundle物件，傳入緯度和經度
	        Bundle bundle = new Bundle();
	        bundle.putDouble("buildingLat", buildingLat);
	        bundle.putDouble("buildingLng", buildingLng);
	        bundle.putString("buildingName", buildingName);
	        bundle.putString("buildingContent", buildingContent);
	        intent1.putExtras(bundle);
	        //不重複啟動相同的Activity
	        intent1.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent1);
			break;
		case R.id.imageButton3:
			Intent intent2 = new Intent();
			intent2.setClass(MainActivity.this, HousePrice.class);
			//new一個Bundle物件，傳入緯度和經度
	        Bundle bundle2 = new Bundle();
	        bundle2.putDouble("buildingLat", buildingLat);
	        bundle2.putDouble("buildingLng", buildingLng);
	        bundle2.putString("buildingName", buildingName);
	        bundle2.putString("buildingContent", buildingContent);
	        intent2.putExtras(bundle2);
			startActivity(intent2);
			break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			Intent intent = new Intent(this,GirdView.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public class ImageAdapter extends BaseAdapter{
		
		private Context myContext;
		
		public ImageAdapter(Context c) {
			this.myContext=c;
		}
		
		@Override
		public int getCount() {
			return bmpBuildings.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(this.myContext);
			imageView.setImageBitmap(bmpBuildings[position]);
			imageView.setScaleType(ScaleType.FIT_XY);
			imageView.setLayoutParams(new Gallery.LayoutParams(1024, 640));
			return imageView;
		}
		
//		public float getScale(boolean focused, int offset){
//			return Math.max(0, 1.0f/(float)Math.pow(2, Math.abs(offset)));
//		}
	}
}
