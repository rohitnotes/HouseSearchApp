package com.nkfust.im.housesearchapp.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tool {
	private static final String TAG = "CameraRecognition::Tool";
	private static final String DB_PATH = Environment.getExternalStorageDirectory().toString() + "/HouseSearchApp_DB";
	private static final String BUILDING_DB_PATH = DB_PATH + "/Building";
	private static final String DM_DB_PATH = DB_PATH + "/DM";
	private Context cxt;
	
	public Tool() {}
	
	public void setContext(Context context) {
		cxt = context;
	}
	
	public String getDBpath() {
		return DB_PATH;
	}
	
	public String getBuildingDBpath() {
		return BUILDING_DB_PATH;
	}
	
	public String getDMDBpath() {
		return DM_DB_PATH;
	}
	
	//執行SQL指令
	public Cursor SQLiteRawQuery(String query) {
		// 建立SQLite資料庫
		SQLiteDB sqlitedb = new SQLiteDB(cxt);
		// 開啟SQLite資料庫
		sqlitedb.OpenDB();
		// 取得SQLite資料庫
		SQLiteDatabase db = sqlitedb.getDB();
		// cursor select
		Cursor c = db.rawQuery(query,null);
		return c;
	}
	
	//取得資料夾內jpg的圖片名稱
	public ArrayList<String> getfolderfileName(String filepath) {
		//File物件建立
		File file = new File(filepath);
		//arraylist陣列
		ArrayList<String> arrayList = new ArrayList<String>();
		//如果沒有此圖片資料夾
		if(!file.exists()){
			arrayList.add(DB_PATH+"/nf.png");
			return arrayList;
		}
		//取得此資料夾底下的所有file
		File[] list = file.listFiles();
		//如果有資料夾但是底下沒檔案
		if(list.length==0){
			arrayList.add(DB_PATH+"/nf.png");
			return arrayList;
		}
		//有幾張jpg檔案
		int count=0;
		//每一個file依序列出來
		for (File f: list){
			//取得每個file的名稱
		    String name = f.getName();
		    //如果附檔名是jpg或JPG，則數量+1
		    if(name.endsWith(".jpg") || name.endsWith(".JPG")){
		    	count++;
		    	arrayList.add(f.getAbsolutePath());
		    }
		}
		//如果沒jpg圖
		if(count==0){
			arrayList.add(DB_PATH+"/nf.png");
			return arrayList;
		}
		return arrayList;
	}
	
	//取得資料夾內jpg的數量
	public int getfolderfileCount(String filepath) {
		//File物件建立
		File file = new File(filepath);
		//初始化數量
		int count = 0;
		//取得此資料夾底下的所有file
		File[] list = file.listFiles();
		//每一個file依序列出來
		for (File f: list){
			//取得每個file的名稱
		    String name = f.getName();
		    //如果附檔名是jpg或JPG，則數量+1
		    if(name.endsWith(".jpg") || name.endsWith(".JPG"))
		       count++;
		}
		return count;
	}
	
	//遞迴取得資料夾內的jpg的數量
	public int getallfolderfileCount(String filepath){
		//File物件建立
		File file = new File(filepath); 
		//初始化數量
		int count = 0;
		//宣告名稱
		String name;
		//取得此資料夾底下的所有file
		File[] list = file.listFiles();
		//每一個file依序列出來
		for (File f: list){
		    if(f.isDirectory()){ //如果是資料夾才進入判斷
		    	count += getfolderfileCount(filepath+"/"+f.getName());
		    }
		}
		return count;
	}
	
	//取得所有資料夾中的圖片路徑
	public String[] getallfolderfilePath(String filepath) {
		//取得jpg圖片數量
		int jpgcount = getallfolderfileCount(filepath);
		//建立String陣列存放每個圖片的位置
		String[] allfileArr = new String[jpgcount];
		//File物件建立
		File file = new File(filepath); 
		//取得此資料夾底下的所有file
		File[] list = file.listFiles();
		//
		int i = 0;
		//每一個file依序列出來
		for (File f: list){
		    if(f.isDirectory()){ //如果是資料夾才進入判斷
		    	//取得此資料夾底下的所有file
				File[] llist = f.listFiles();
				//每一個file依序列出來
				for (File ff: llist){
				    //如果附檔名是jpg或JPG，則數量+1
				    if(ff.getName().endsWith(".jpg") || ff.getName().endsWith(".JPG")){
				    	allfileArr[i] = ff.getAbsolutePath();
				    	i++;
				    }
				}
		    }
		}
		return allfileArr;
	}
	
	//取得單一資料夾中的圖片路徑與資料夾名稱(id)
	public ItemData[] getfolderfileInfo(String filepath, int id)
	{
		filepath+="/"+id;
		//取得jpg圖片數量
		int jpgcount = getfolderfileCount(filepath);
		//建立ARRAY
		ItemData[] itemData = new ItemData[jpgcount];
		//File物件建立
		File file = new File(filepath);
		//取得此資料夾底下的所有file
		File[] list = file.listFiles();
		//
		int i = 0;
		for (File f:list){
			if(f.getName().endsWith(".jpg") || f.getName().endsWith(".JPG")){
				itemData[i] = new ItemData(id,f.getAbsolutePath(),"","");
				i++;
			}
		}
		return itemData;
	}
	
	//取得所有資料夾中的圖片路徑與資料夾名稱(id)
	public ItemData[] getallfolderfileInfo(String filepath)
	{	
		//取得jpg圖片數量
		int jpgcount = getallfolderfileCount(filepath);
		//建立ARRAY
		ItemData[] itemData = new ItemData[jpgcount];
		Log.i(TAG,"itemData length:"+itemData.length);
		//File物件建立
		File file = new File(filepath); 
		//取得此資料夾底下的所有file
		File[] list = file.listFiles();
		//
		int i = 0;
		//每一個file依序列出來
		for (File f: list){
		    if(f.isDirectory()){ //如果是資料夾才進入判斷
		    	//取得此資料夾底下的所有file
				File[] llist = f.listFiles();
				//每一個file依序列出來
				for (File ff: llist){
				    //如果附檔名是jpg或JPG，則數量+1
				    if(ff.getName().endsWith(".jpg") || ff.getName().endsWith(".JPG")){
				    	String folder = f.getName();
				    	boolean isNum = isNumeric(folder);
				    	int id;
				    	if(isNum)
				    		id = Integer.parseInt(folder);
				    	else 
				    		id = 0;
				    	Log.i(TAG,"id:"+id+" absolutepath:"+ff.getAbsolutePath()+" folder:"+f.getName());
				    	itemData[i] = new ItemData(id,ff.getAbsolutePath(),"不明建築","資料庫沒此資料。");
				    	i++;
				    }
				}
		    }
		}
		
		return itemData;
	}
	
	//取得附檔名
	public String getExtension(File file)
    {
        int startIndex = file.getName().lastIndexOf(46) + 1;
        int endIndex = file.getName().length();
        return  file.getName().substring(startIndex, endIndex);
    }
	
	//修改路徑的附檔名
	public String editExtension(String filepath, String newExtension)
	{
		return filepath.substring(0, filepath.lastIndexOf('.')) + "." + newExtension;
	}
	
	//判斷字串是不是數字
	public boolean isNumeric(String str)
	{
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if(!isNum.matches())
		{
			return false;
		}
		return true;
	}
}
