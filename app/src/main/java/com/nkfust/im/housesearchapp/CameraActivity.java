package com.nkfust.im.housesearchapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nkfust.im.housesearchapp.util.DBData;
import com.nkfust.im.housesearchapp.util.ItemData;
import com.nkfust.im.housesearchapp.util.Tool;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import java.util.ArrayList;
import java.util.List;

/*****
 * @author tony
 * 建築物影像辨識
 */

public class CameraActivity extends Activity implements CvCameraViewListener2, OnClickListener {

	private static final String TAG = "CameraActivity";
	private static final int buildingThreshold = 30;
	private static final int DMThreshold = 3;
	private String cameraType;
	
	private Mat mRgba;
	private Mat mGray;
	private Mat query_descriptors;
	private Mat[] db_descriptors;

	private CameraBridgeViewBase mOpenCvCameraView;
	private FeatureDetector detector;
	private DescriptorExtractor descriptor;
	private DescriptorMatcher matcher;
	private MatOfKeyPoint keypoints;
	
	private MatOfDMatch matches;

	private ImageView img;
	private TextView title;
	private Button detailBtn,enlargeBtn,decreaseBtn;
	
	private Tool tool;
	private ORB orb;
	private DBData[] dbData;
	private ItemData[] itemData;
	private int match_points,match_number;
	
	private SensorManager smgr;
	private List<Sensor> slist;
	private boolean isStarted;
	private float max;
	
	private int minDistancePKId=0;
	private String minDistancesName;
	
	private int jpgcount;
	
	private boolean isInit = false;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.setMaxFrameSize(320, 320);
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		//tool
		tool = new Tool();
		tool.setContext(getApplicationContext());
		//orb
		orb = new ORB();
		orb.getTool(tool);
		
		//接收來源(Building or DM)
		cameraType = getIntent().getStringExtra("cameraType");
		
		//先撈出資料庫中的building所有資料
		Cursor cursor = tool.SQLiteRawQuery("SELECT id,buildingname,contenttext from building order by id asc;");
		if(cursor.getCount()!=0){
			dbData = new DBData[cursor.getCount()];
			cursor.moveToFirst();
			for(int i=0;i<cursor.getCount();i++) {
				dbData[i] = new DBData(cursor.getInt(0),cursor.getString(1),
						cursor.getString(2));
				cursor.moveToNext();
			}
		}
		
		//
		jpgcount = 0;
		if(cameraType.equals("Building")){
			setContentView(R.layout.activity_camera);
			//取得鄰近建築物編號
			getNearestBuilding();
			//模擬位置
//			minDistancePKId = 1;
			if(minDistancePKId==0){
				//得到全部的jpg數量
				jpgcount = tool.getallfolderfileCount(tool.getBuildingDBpath());
				//取得所有建築物的資訊
				itemData = tool.getallfolderfileInfo(tool.getBuildingDBpath());
			} else {
				//得到最靠近建築物的jpg數量
				jpgcount = tool.getfolderfileCount(tool.getBuildingDBpath()+"/"+minDistancePKId);
				//取得最靠近建築物的資訊
				itemData = tool.getfolderfileInfo(tool.getBuildingDBpath(),minDistancePKId);
			}
		} else if(cameraType.equals("DM")) {
			setContentView(R.layout.dm_activity_camera);
			//得到全部的jpg數量
			jpgcount = tool.getallfolderfileCount(tool.getDMDBpath());
			//取得所有圖片的位置與資料夾(id)
			itemData = tool.getallfolderfileInfo(tool.getDMDBpath());
		}
		Log.i(TAG,"jpgcount:"+jpgcount);
		Log.i(TAG,"alldbPath length"+itemData.length);
		
		//將itemData所缺少的Name 與 content補上
		for(int i=0;i<itemData.length;i++) {
			for(int j=0;j<dbData.length;j++) {
				if(itemData[i].id == dbData[j].id) {
					itemData[i].Name = dbData[j].Name;
					itemData[i].contentText = dbData[j].contentText;
//					Log.i(TAG,"itemData["+i+"] : "+itemData[i].id+" , "
//							+itemData[i].Name+" , "+itemData[i].contentText);
				}
			}
		}
		
		//Set View
		SetView();
		
		//Sensor setting
		smgr = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		slist = smgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (slist.size() == 0) {
			Toast.makeText(this, "No accelerometer sensor",
			Toast.LENGTH_SHORT).show();
			finish();
		}
		isStarted = false;
	}

	private void SetView() {
		//Set View
		img = (ImageView)this.findViewById(R.id.img);
		img.setVisibility(View.INVISIBLE);
		title = (TextView)this.findViewById(R.id.title);
		title.setVisibility(View.INVISIBLE);
		detailBtn = (Button)this.findViewById(R.id.detailBtn);
		detailBtn.setVisibility(View.INVISIBLE);
		detailBtn.setOnClickListener(this);
		//
		if(cameraType.equals("DM")){
			enlargeBtn = (Button)this.findViewById(R.id.button1);
	  		enlargeBtn.setVisibility(View.VISIBLE);
	  		enlargeBtn.setOnClickListener(this);
	  		decreaseBtn = (Button)this.findViewById(R.id.button2);
	  		decreaseBtn.setVisibility(View.VISIBLE);
	  		decreaseBtn.setOnClickListener(this);
		}
		//
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}
	
	private void getNearestBuilding() {
		Bundle bundle = getIntent().getExtras();
		try {
			minDistancePKId = bundle.getInt("minDistancePKId",0);
			minDistancesName = bundle.getString("minDistancesName");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.back:
			Intent intent = new Intent();
			intent.setClass(CameraActivity.this, Map.class);
			startActivity(intent);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG,"onStart");
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG,"onRestart");
	}
	
	@Override
	protected void onResume() {
		super.onResume();	
		Log.i(TAG,"onResume");
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		startSensorListener();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG,"onPause");
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG,"onStop");
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG,"onDestroy");
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	private void startSensorListener() {
		if(isStarted) return;
		isStarted = true;
		max = (float)0.0;
		smgr.registerListener(mListener, slist.get(0), SensorManager.SENSOR_DELAY_UI);
	}
	
	private void stopSensorListener() {
		if(!isStarted) return;
		isStarted = false;
		smgr.unregisterListener(mListener, slist.get(0));
	}
	
	private final SensorEventListener mListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			if(event.sensor == slist.get(0)){
				if (isStarted == false) return;
				
				float totalForce = (float)0.0;
				totalForce += (float)Math.pow(event.values[0]/SensorManager.GRAVITY_EARTH, 2.0);
				totalForce += (float)Math.pow(event.values[1]/SensorManager.GRAVITY_EARTH, 2.0);
				totalForce += (float)Math.pow(event.values[2]/SensorManager.GRAVITY_EARTH, 2.0);
				totalForce = (float)Math.sqrt(totalForce);
				
				if (totalForce > max)
					max = totalForce;
				
				// 如果晃動大於2.5
				if (max >= 2.5) {
					// 關閉Sensor感測器
					stopSensorListener();
					// 顯示結果
					GoToResult();
				}
			}
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	}; 
	
	private int cameraWidth=0,cameraHeight=0;
	private int cameraWidth_10percent=0,cameraHeight_10percent=0;
	
	@Override
	public void onCameraViewStarted(int width, int height) {
		Log.i(TAG,"onCameraViewStarted");
		
		if(isInit) //如果已經初始化過，就直接跳出
			return;
		
		if(cameraType.equals("DM")){
			//camera的寬高
			cameraWidth=width;
			cameraHeight=height;
			//camera長寬10%數值
			cameraWidth_10percent=width/20;
			cameraHeight_10percent=height/20;
		}
		
		//初始化mRgba與mGray的Mat大小
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
		//初始化
		detector = FeatureDetector.create(FeatureDetector.ORB);
		descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		
		//建立全部圖片特徵值的存放數量（長度）
		db_descriptors = new Mat[itemData.length];
		
		/********************************************************
		//建立所有圖片的特徵值
		for(int i=0;i<db_descriptors.length;i++){
			orb.createDB(itemData[i].filepath);
		}
		********************************************************/
		

		//讀取所有圖片的特徵值
		for(int i=0;i<db_descriptors.length;i++){
//			Log.i(TAG,"alldbPath["+i+"] :"+itemData[i].filepath);
			db_descriptors[i] = orb.resdDB(itemData[i].filepath);
		}
		Log.i(TAG,"db count:"+db_descriptors.length);
	}

	@Override
	public void onCameraViewStopped() {
		Log.i(TAG,"onCameraViewStopped");
		mRgba.release();
		mGray.release();
		keypoints.release();
		query_descriptors.release();
		matches.release();
	}
	
	private long startTime=0;//開始時間
	private boolean doRecognition = true; //是否辨識，預設為"是"
	
	//
	private int left,top,width,height;
	
	//寬與高的差
	private int wDifference = 0;
	private int hDifference = 0;
	
	private int A = 0;
	
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		
		Mat mGrayInnerWindow = null;
		if(cameraType.equals("DM")){
			//取得最上和最左邊
	        left = (cameraWidth / 8)+50;
	        top = (cameraHeight / 8)+50;//4
	        //寬和高的額外加總距離
	        width = cameraWidth * 3/8;  //3/4
	        height = cameraHeight * 3 / 8; //3/4
			//設置左上角(pt1)和右下角(pt2)的點
	        Point pt1 = new Point(left + wDifference,top + hDifference);
	        Point pt2 = new Point(left + width - wDifference,top + height - hDifference);
	        //在mRgba畫方形
	        Core.rectangle(mRgba, pt1, pt2, new Scalar( 0, 0, 255 ),0,8, 0 );
	        
	        //辨識範圍
			mGrayInnerWindow = mGray.submat(top + hDifference, top + height - hDifference
					, left + wDifference, left + width - wDifference);
		}
		
		if(System.currentTimeMillis()-startTime>=3000)
			doRecognition = true;
		if(doRecognition){
			keypoints = new MatOfKeyPoint();
			query_descriptors = new Mat();
			if(cameraType.equals("Building")){
				detector.detect(mGray, keypoints);
				descriptor.compute(mGray, keypoints, query_descriptors);
			} else if(cameraType.equals("DM")){
				detector.detect(mGrayInnerWindow, keypoints);
				descriptor.compute(mGrayInnerWindow, keypoints, query_descriptors);
			}
			match_points = 0; match_number = 0;
			for (int i = 1; i <= db_descriptors.length; i++) {
				matches = new MatOfDMatch();
				matcher.match(query_descriptors, db_descriptors[i - 1], matches);
				int DIST_LIMIT = 45;
				
				List<DMatch> matchesList = matches.toList();
				List<DMatch> matches_final = new ArrayList<DMatch>();
				for (int ii = 0; ii < matchesList.size(); ii++) {
					if (matchesList.get(ii).distance <= DIST_LIMIT) {
						matches_final.add(matches.toList().get(ii));
					}
				}
				if (match_points < matches_final.size()) {
					match_points = matches_final.size();
					match_number = i;
				}
			}
			int threshold = 0;
			if(cameraType.equals("Building"))
				threshold = buildingThreshold;
			else if(cameraType.equals("DM")) 
				threshold = DMThreshold;
			
			if(match_points > threshold){ //判定有辨識到 (building:18)
				startTime = System.currentTimeMillis();
				doRecognition = false;
				ShowBuilding();	
			} else { 			   //判定無辨識到
				CloseBuilding();
			}
		}
		
		return mRgba;
	}
	
	private void ShowBuilding() {
		runOnUiThread(new Runnable() { 
		    public void run() 
		    { 
		    	Bitmap bitmap = BitmapFactory.decodeFile(itemData[match_number - 1].filepath);
				img.setImageBitmap(bitmap);
				img.setVisibility(View.VISIBLE);
				title.setText(itemData[match_number - 1].Name);
				title.setVisibility(View.VISIBLE);
				detailBtn.setVisibility(View.VISIBLE);
		    } 
		}); 
	}
	
	private void CloseBuilding() {
		runOnUiThread(new Runnable() { 
	        public void run() 
	        { 
	        	img.setVisibility(View.INVISIBLE);
	        	title.setVisibility(View.INVISIBLE);
	        	detailBtn.setVisibility(View.INVISIBLE);
	        } 
	    }); 
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.detailBtn:
			GoToResult();
			break;
		case R.id.button1: //放大
			Log.i(TAG,"放大");
			A++;
			if(A<=5){
				wDifference -= cameraWidth_10percent;
				hDifference -= cameraHeight_10percent;
			} else {
				A = 5;
			}
			break;
		case R.id.button2: //縮小
		    Log.i(TAG,"縮小");
	    	A--;
	    	if(A>=-3){
	    		wDifference += cameraWidth_10percent;
				hDifference += cameraHeight_10percent;
	    	} else {
	    		A = -3;
	    	}
			break;
		}
	}
	
	private void GoToResult(){
		int arrNum = match_number -1; //資料夾是 1 那會放在陣列的[0]中，所以-1
		Log.i(TAG,"arrNum:"+arrNum);
		
		if(itemData[arrNum].id==0){
			Toast.makeText(getApplicationContext(), "資料庫裡找不到關於此圖片的資料", Toast.LENGTH_LONG).show();
			return;
		}
			
		Cursor c = tool.SQLiteRawQuery("SELECT id,lat,lng,buildingname,contenttext,price,addr,pattern,footage,age,toward from building where id="+itemData[arrNum].id+";");
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
        //age
        double buildingAge = c.getDouble(9);
        //toward
        String buildingToward = c.getString(10);
        
        //new一個intent物件，並指定Activity切換的class
    	Intent intent = new Intent();
		intent.setClass(CameraActivity.this, MainActivity.class);
		
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
        bundle.putString("type", "CameraActivity");
        //將Bundle物件assign給intent
        intent.putExtras(bundle);
        //切換activity
		startActivity(intent);	
		//finish
		finish();
	}
}