package com.nkfust.im.housesearchapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nkfust.im.housesearchapp.util.SQLiteDB;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DMCameraActivity extends Activity implements CvCameraViewListener2, OnClickListener {

	private static int db_numbers = 20;
	private static final String TAG = "DM::MainActivity";
	private static final String db_path = Environment.getExternalStorageDirectory().toString() + "/DB_DM";
	
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
	
	private SQLiteDatabase db;
	
	private int alljpgcount=0;
	
	private int[] db_building;
	private int[] db_building_pos;
	
	private String[] AllBuildingName,AllBuildingContentText;
	
	//取得資料夾內jpg的數量
	private int folderjpgcount(File file) {
		File[] list = file.listFiles();
		int count = 0;
		for (File f: list){
		    String name = f.getName();
		    Log.i(TAG, name);
		    if (name.endsWith(".jpg"))
		       count++;
		}
		return count;
	}
	
	private Cursor sqliteSelect(String query) {
		// 建立SQLite資料庫
		SQLiteDB sqlitedb = new SQLiteDB(DMCameraActivity.this);
		// 開啟SQLite資料庫
		sqlitedb.OpenDB();
		// 取得SQLite資料庫
		SQLiteDatabase db = sqlitedb.getDB();
		// cursor select
		Cursor c = db.rawQuery(query,null);
		return c;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.dm_activity_camera);
        //Set View
  		img = (ImageView)this.findViewById(R.id.img);
  		img.setVisibility(View.INVISIBLE);
  		title = (TextView)this.findViewById(R.id.title);
  		title.setVisibility(View.INVISIBLE);
  		detailBtn = (Button)this.findViewById(R.id.detailBtn);
  		detailBtn.setVisibility(View.INVISIBLE);
  		detailBtn.setOnClickListener(this);
  		enlargeBtn = (Button)this.findViewById(R.id.button1);
  		enlargeBtn.setVisibility(View.VISIBLE);
  		enlargeBtn.setOnClickListener(this);
  		decreaseBtn = (Button)this.findViewById(R.id.button2);
  		decreaseBtn.setVisibility(View.VISIBLE);
  		decreaseBtn.setOnClickListener(this);
		
  		//cursor select
		Cursor c = sqliteSelect("SELECT buildingname,contenttext FROM building WHERE bdareaid=3;");
		Log.i(TAG,"count:"+c.getCount());
		//
		AllBuildingName = new String[c.getCount()];
		AllBuildingContentText = new String[c.getCount()];
		// 移至搜尋出來的第一筆資料
		c.moveToFirst();
		for(int i=0;i<c.getCount();i++) {
			AllBuildingName[i] = c.getString(0);
			AllBuildingContentText[i] = c.getString(1);
			//移動到下一筆資料
			c.moveToNext();
		}
		
		db_numbers = new File(db_path+"/ALL").list().length;
		Toast.makeText(getApplicationContext(), "讀取資料庫中"+String.valueOf(db_numbers)+"張DM圖資。", Toast.LENGTH_LONG).show();
  		
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
    }

    protected void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}
    
	private int cameraWidth=0,cameraHeight=0;
	
	private int cameraWidth_10percent=0,cameraHeight_10percent;
	
	@Override
	public void onCameraViewStarted(int width, int height) {
		/****
		//camera的寬高
		cameraWidth=width;
		cameraHeight=height;
		//camera長寬10%數值
		cameraWidth_10percent=width/20;
		cameraHeight_10percent=height/20;
		
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
		detector = FeatureDetector.create(FeatureDetector.ORB);
		descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		
		alljpgcount = 0;	
		String buildingfolder = ""; //建築物資料夾
		
		buildingfolder = db_path+"/ALL";
		
		//該資料夾底下有多少建築物
		int count1 = new File(buildingfolder).list().length;
		//建立檔案
		for(int j=1;j<=count1;j++){
			int count2 = folderjpgcount(new File(buildingfolder+"/"+(j)));
			for(int i=1;i<=count2;i++){
//				new ORB().createDB(buildingfolder+"/"+(j), Integer.toString(i));
			}
			alljpgcount+=count2;
		}
		//建立descriptors數量
		db_descriptors = new Mat[alljpgcount];
		//建立db_building數量
		db_building = new int[alljpgcount];
		//建立db_building_pos數量
		db_building_pos = new int[alljpgcount];
		//讀取.des檔案
		int j_number=0;
		for(int j=1;j<=count1;j++){
			int count2 = folderjpgcount(new File(buildingfolder+"/"+(j)));
			for(int i=1;i<=count2;i++){
				//descriptors
				db_descriptors[j_number] = new ORB().resdDB(buildingfolder+"/"+(j), Integer.toString(i));
				db_building[j_number]=j;     //第幾個資料夾
				db_building_pos[j_number]=i; //全部圖片的第幾個
				j_number++;
			}
		}
		***/
	}
	
	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		mRgba.release();
		mGray.release();
		keypoints.release();
		query_descriptors.release();
		matches.release();
	}
	
	private int match_points,match_number;
	private int thenumber;
	
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
		Mat mGrayInnerWindow;
		mGrayInnerWindow = mGray.submat(top + hDifference, top + height - hDifference
				, left + wDifference, left + width - wDifference);
				
		keypoints = new MatOfKeyPoint();
		query_descriptors = new Mat();
		detector.detect(mGrayInnerWindow, keypoints);
		descriptor.compute(mGrayInnerWindow, keypoints, query_descriptors);
		match_points = 0; match_number = 0;
		
		for (int i = 1; i <= db_descriptors.length; i++) {
			matches = new MatOfDMatch();
			matcher.match(query_descriptors, db_descriptors[i - 1], matches);
			int DIST_LIMIT = 45;//45
			List<DMatch> matchesList = matches.toList();
			List<DMatch> matches_final = new ArrayList<DMatch>();
			for (int ii = 0; ii < matchesList.size(); ii++) {
//				Log.i(TAG,String.valueOf(matchesList.get(ii).distance));
				if (matchesList.get(ii).distance <= DIST_LIMIT) {
					matches_final.add(matches.toList().get(ii));
				}
			}
			if (match_points < matches_final.size()) {
				match_points = matches_final.size();
				match_number = i;
			}
		}
			
		if (match_points > 3) {
			ShowBuilding();
		} else { 
			CloseBuilding();
		}
		
		return mRgba;
	}

	private void ShowBuilding() {
		thenumber=match_number;
		runOnUiThread(new Runnable() { 
		    public void run() 
		    { 
		    	if(db_building_pos[thenumber - 1]>=1&&db_building_pos[thenumber - 1]<=3){
			    	Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString()
							+"/DB_DM/ALL/"+Integer.toString(db_building[thenumber - 1])
							+"/"+Integer.toString(db_building_pos[thenumber - 1])+".jpg");
					img.setImageBitmap(bitmap);
					img.setVisibility(View.VISIBLE);
					title.setText(AllBuildingName[db_building_pos[thenumber - 1] - 1 ]);
					title.setVisibility(View.VISIBLE);
					detailBtn.setVisibility(View.VISIBLE);
		    	}
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
//			Bitmap bitmap = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
//			Utils.matToBitmap(mRgba, bitmap, true);
			int a = thenumber -1;
			
			if(db_building_pos[a]>=1&&db_building_pos[a]<=3){
				//get cursor
		    	Cursor c = sqliteSelect("SELECT id,lat,lng,buildingname,contenttext from building " +
		    			" where bdareaid=3 and bdid="+db_building_pos[a]+";");
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
		        
		        Log.i("DAMN","buildingID:"+buildingID);
		        Log.i("DAMN","buildingLat:"+buildingLat);
		        Log.i("DAMN","buildingLng:"+buildingLng);
		        Log.i("DAMN","buildingName:"+buildingName);
		        Log.i("DAMN","buildingContent:"+buildingContent);
		        
		        //new一個intent物件，並指定Activity切換的class
		    	Intent intent = new Intent();
				intent.setClass(DMCameraActivity.this, MainActivity.class);
				
				//new一個Bundle物件，並將要傳遞的資料傳入
		        Bundle bundle = new Bundle();
		        bundle.putInt("buildingID",buildingID);
		        bundle.putDouble("buildingLat", buildingLat);
		        bundle.putDouble("buildingLng", buildingLng);
		        bundle.putString("buildingName",buildingName);
		        bundle.putString("buildingContent",buildingContent);
		        bundle.putString("type", "DMCameraActivity");
		        //將Bundle物件assign給intent
		        intent.putExtras(bundle);
				
		        //切換activity
				startActivity(intent);
			} else {
				Toast.makeText(getApplicationContext(), "沒有此DM相關資料", Toast.LENGTH_SHORT).show();
			}
			
			
			
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
}
