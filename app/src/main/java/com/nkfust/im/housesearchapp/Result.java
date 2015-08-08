package com.nkfust.im.housesearchapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import java.io.File;

public class Result extends Activity {

	private static final String TAG = "Result";
	
	private TextView building_title,building_content;
	private Gallery gallery;
	private int match_number;
	//建立Bitmap陣列，存放該建築物的所有圖片
	private Bitmap[] bmpBuildings;
	private int[] resBuildings;
	private boolean normal=true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		Intent intent = this.getIntent();
		match_number = intent.getIntExtra("Match_number", 0);
		//建築物標題
		building_title = (TextView)this.findViewById(R.id.building_title);
		building_title.setText(intent.getStringExtra("Building_name"));
		
		//建築物編號是4,8,15,17,30的讀取drawable的圖資
		if(match_number==4 || match_number==8 || match_number==9 || 
				match_number==15 || match_number==17 || match_number==30){
			normal=false; //不是讀取正常圖片
			if(match_number==4){
				//編號4有4張圖片
				resBuildings = new int[4];
				resBuildings[0] = R.drawable.b4_01;
				resBuildings[1] = R.drawable.b4_02;
				resBuildings[2] = R.drawable.b4_03;
				resBuildings[3] = R.drawable.b4_04;
			} else if(match_number==8){
				//編號8有5張圖片
				resBuildings = new int[5];
				resBuildings[0] = R.drawable.b8_01;
				resBuildings[1] = R.drawable.b8_02;
				resBuildings[2] = R.drawable.b8_03;
				resBuildings[3] = R.drawable.b8_04;
				resBuildings[4] = R.drawable.b8_05;
			} else if(match_number==9){
				//編號9有3張圖片
				resBuildings = new int[3];
				resBuildings[0] = R.drawable.b9_01;
				resBuildings[1] = R.drawable.b9_02;
				resBuildings[2] = R.drawable.b9_02;
			} else if(match_number==15){
				//編號15有2張圖片
				resBuildings = new int[2];
				resBuildings[0] = R.drawable.b15_01;
				resBuildings[1] = R.drawable.b15_02;
			} else if(match_number==17){
				//編號17有2張圖片
				resBuildings = new int[2];
				resBuildings[0] = R.drawable.b17_01;
				resBuildings[1] = R.drawable.b17_02;
			} else if(match_number==30){
				//編號30有3張圖片
				resBuildings = new int[3];
				resBuildings[0] = R.drawable.b30_01;
				resBuildings[1] = R.drawable.b30_02;
				resBuildings[2] = R.drawable.b30_03;
			}
		} else { //剩下正常讀取原DB圖資
			normal = true;
			//取得該建築物的圖片數量
			int buildingimageCount = folderjpgcount(new File(Environment.getExternalStorageDirectory().toString()
					+"/DB_NEW/ALL/"+match_number));
			
			//宣告Bitmap陣列數量
			bmpBuildings = new Bitmap[buildingimageCount];
			
			//取得該建築物的所有圖片Bitmap
			for(int i=0;i<buildingimageCount;i++){
				bmpBuildings[i] = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()
					+"/DB_NEW/ALL/"+match_number+"/"+(i+1)+".jpg");
			}
		}
		
		//放建築物image
		gallery = (Gallery)findViewById(R.id.gallery1);
        ImageAdapter imageAdapter = new ImageAdapter(this);
        gallery.setAdapter(imageAdapter);
		
        //建築物內容介紹
		building_content = (TextView)this.findViewById(R.id.building_content);
		building_content.setText(intent.getStringExtra("Building_content"));
		
//		Log.i(TAG,"Match number pos:"+String.valueOf(intent.getIntExtra("Match_number_pos", 0)));
	}

	//取得資料夾內jpg的數量
	private int folderjpgcount(File file) {
		File[] list = file.listFiles();
		int count = 0;
		for (File f: list){
		    String name = f.getName();
		    if (name.endsWith(".jpg"))
		       count++;
		}
		return count;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.result, menu);
		return true;
	}
	
	public class ImageAdapter extends BaseAdapter{

    	private Context myContext;
    	
		public ImageAdapter(Context c) {
			super();
			this.myContext = c;
		}

		@Override
		public int getCount() {
			if(normal)
				return bmpBuildings.length;
			else 
				return resBuildings.length;
		}

		@Override
		public Object getItem(int position) {
		
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(this.myContext);
			if(normal)
				imageView.setImageBitmap(bmpBuildings[position]);
			else
				imageView.setImageResource(resBuildings[position]);
			imageView.setScaleType(ScaleType.FIT_XY);
			imageView.setLayoutParams(new Gallery.LayoutParams(1024, 640));
			return imageView;
		}
		
    }
}
