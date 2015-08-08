package com.nkfust.im.housesearchapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.nkfust.im.housesearchapp.admin.Login;
import com.nkfust.im.housesearchapp.util.ItemData;
import com.nkfust.im.housesearchapp.util.Tool;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GirdView extends Activity implements OnItemClickListener{
	private static final String TAG = "GirdView";
	private GridView girdview1;
	private List<Map<String, Object>>dataList;
	
	private int[] icon = {R.drawable.gmap,R.drawable.ar,R.drawable.search,R.drawable.camera,
			              R.drawable.db,R.drawable.book,R.drawable.about,R.drawable.exit
	                      };
	
	private String[]iconName ={"GoogleMap","AR Guide","DMsearch","CA_Camera"
			                   ,"Database","Info","About","Exit"};
	
	private SimpleAdapter simpleadapter;
	
	private Tool tool;
	private ORB orb;
	
	private ProgressDialog dialog;
	
	private static final int MSG_UPLOAD_OK= 0x00000001;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
	
	private Handler mUI_Handler = new Handler()

	{
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPLOAD_OK:
				Toast.makeText(getApplicationContext(), "完成建立所有圖片的特徵值", 
						Toast.LENGTH_LONG).show();
				dialog.dismiss();
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gird_view);
		
		//tool
		tool = new Tool();
		tool.setContext(getApplicationContext());
		//orb
		orb = new ORB();
		orb.getTool(tool);
		
		girdview1 = (GridView)findViewById(R.id.gridview);
	//	girdview1.setAlpha(255);
		//imgview = (ImageView)findViewById(R.id.image);
		
//		imgview.setAlpha(255);

		//1.prepare 數據源
		//2.選配選配器(SimpleAdapter)
		//3.GridView加仔選配器
		//4.GridView配置事件監聽器(OnItemListener)
		
		dataList = new ArrayList<Map<String,Object>>();
		//getData();
		
		simpleadapter = new SimpleAdapter(this, getData(),R.layout.item, 
				                    new String[]{"image","text"},
				                    new int[]{R.id.image,R.id.text} );
		
		girdview1.setAdapter(simpleadapter);
		
		girdview1.setOnItemClickListener(this);//監聽
	}

	@Override
	protected void onResume() {
		super.onResume();	
		Log.i(TAG,"onResume");
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}
	
	private List<Map<String, Object>> getData() {

		for (int i = 0; i < icon.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("image", icon[i]);
			map.put("text", iconName[i]);
			dataList.add(map);
		}
		return dataList;

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent();
		switch (position) {
		case 0:
			intent.setClass(getApplicationContext(), GoogleMap.class);
			Bundle bundle = new Bundle();
			bundle.putString("type", "ALL");
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case 1:
			intent.setClass(getApplicationContext(), ARActivity.class);
			startActivity(intent);
			break;
		case 2: // DM
			intent.setClass(getApplicationContext(), CameraActivity.class);
			intent.putExtra("cameraType", "DM");
			startActivity(intent);
			break;
		case 3:
			intent.setClass(getApplicationContext(),
					com.nkfust.im.housesearchapp.Map.class);
			startActivity(intent);
			break;
		case 4:
			intent.setClass(getApplicationContext(), Login.class);
			startActivity(intent);
			break;
		case 5:
			Toast.makeText(getApplicationContext(), "使用說明。", Toast.LENGTH_SHORT).show();
			break;
		case 6:
			intent.setClass(getApplicationContext(), About.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case 7:
			finish();
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gridview_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		dialog = ProgressDialog.show(GirdView.this, "建立特徵值",
				"請等待系統為所有資料庫圖片建立相對應特徵值...", true);
		switch (item.getItemId()) {
		case R.id.createdb:
			new Thread(new Runnable() {
				@Override
				public void run() {
					ItemData[] BuildingitemData = tool
							.getallfolderfileInfo(tool.getBuildingDBpath());
					ItemData[] DMitemData = tool.getallfolderfileInfo(tool
							.getDMDBpath());
					// 建立所有圖片的特徵值
					for (int i = 0; i < BuildingitemData.length; i++) {
						orb.createDB(BuildingitemData[i].filepath);
					}
					for (int i = 0; i < DMitemData.length; i++) {
						orb.createDB(DMitemData[i].filepath);
					}
					mUI_Handler.sendEmptyMessage(MSG_UPLOAD_OK);
				}
			}).start();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
