package com.nkfust.im.housesearchapp.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class SQLiteDB {
	private static final String TAG = "schoolMap_new::SQLiteDB";
	private Context cxt;
	private static String dbfile;
	private SQLiteDatabase db;
	public SQLiteDB(Context context) {
		Log.i(TAG,"-----Constructor-----");
		this.cxt = context;
		DataBasePath();
		CopyToDatabasesFile();
	}
	
	//設定dbfile位置
	private void DataBasePath() {
		Log.i(TAG,"-----MyDataBasePath-----");
		String dbdir="/data/data/"+cxt.getApplicationContext().getPackageName()+"/databases";
		File myDataPathdbdir = new File(dbdir);
		if(!myDataPathdbdir.exists())
			(new File(dbdir)).mkdirs();
		dbfile=dbdir+"/main.db";
		Log.i(TAG,dbfile);
	}
	
	//將assets資料夾內的db複製到dbfile位置
	private void CopyToDatabasesFile() {
		Log.i(TAG,"-----CopyToDatabasesFile-----");
		// 複製到database空間
		if ((new File(dbfile)).exists() == false) {
			Log.i(TAG,"----copy----");
			try {
				byte[] buffer = new byte[8192];
//				InputStream fis = cxt.getAssets().open("nsc.db");
//				InputStream fis = cxt.getAssets().open("house.db");
				InputStream fis = cxt.getAssets().open("housedb.db");
				FileOutputStream fos = new FileOutputStream(dbfile);
				BufferedOutputStream dest = new BufferedOutputStream(fos, 8192);
				int count;
				while ((count = fis.read(buffer, 0, 8192)) >= 0)
					dest.write(buffer, 0, count);
				dest.flush();
				dest.close();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//開啟DB
	public void OpenDB() {
		Log.i(TAG,"OpenDB");
		db=SQLiteDatabase.openOrCreateDatabase(dbfile,null);
	}
	
	//關閉DB
	public void CloseDB() {
		Log.i(TAG,"CloseDB");
		db.close();
	}
	
	//取得DB
	public SQLiteDatabase getDB() {
		return db;
	}
}
