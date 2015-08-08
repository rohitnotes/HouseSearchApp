package com.nkfust.im.housesearchapp.admin;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nkfust.im.housesearchapp.R;
import com.nkfust.im.housesearchapp.util.SQLiteDB;

public class AdminDetail extends Activity implements OnClickListener{

	private static final String TAG = "AdminDetail";

	private SQLiteDatabase db;

	private TextView detail_title, idTv;
	private EditText buildingEt, lngEt, latEt, contenttextEt,
					priceEt,addrEt,patternEt,footageEt,ageEt,towardEt;
	private Button button;
	private int id;
	private String layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admindetail);
		//在一進入程式時隱藏鍵盤輸入
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// 取得前一個Activity傳過來的Bundle物件
		Bundle bundle = getIntent().getExtras();
		id = bundle.getInt("id");
		layout = bundle.getString("layout");
		Log.i(TAG, "id:" + id + " layout:" + layout);
		
		// 建立SQLite資料庫
		SQLiteDB sqlitedb = new SQLiteDB(AdminDetail.this);
		// 開啟SQLite資料庫
		sqlitedb.OpenDB();
		// 取得SQLite資料庫
		db = sqlitedb.getDB();

		detail_title = (TextView) this.findViewById(R.id.detail_title);
		idTv = (TextView) this.findViewById(R.id.idTv);
		latEt = (EditText) this.findViewById(R.id.latEt);
		lngEt = (EditText) this.findViewById(R.id.lngEt);
		buildingEt = (EditText) this.findViewById(R.id.buildingEt);
		contenttextEt = (EditText) this.findViewById(R.id.contenttextEt);
		priceEt = (EditText) this.findViewById(R.id.priceEt);
		addrEt = (EditText) this.findViewById(R.id.addrEt);
		patternEt = (EditText) this.findViewById(R.id.patternEt);
		footageEt = (EditText) this.findViewById(R.id.footageEt);
		ageEt = (EditText) this.findViewById(R.id.ageEt);
		towardEt = (EditText) this.findViewById(R.id.towardEt);
		button = (Button) this.findViewById(R.id.button);
		button.setOnClickListener(this);
		if (layout.equals("New")) {
			Detail_NewView();
		}
		if (layout.equals("Edit")) {
			Detail_EditView();
		}
	}

	private int findMaxId() {
		// 取得主鍵編號
		// cursor select (找目前最大的主鍵編號)
		Cursor c = db.rawQuery("SELECT max(id) FROM building;", null);
		// 移至搜尋出來的第一筆資料
		c.moveToFirst();
		// 資料庫中建築物的最大編號
		int maxid = c.getInt(0);
		return maxid+1;
	}

	private void Detail_NewView() {
		Log.i(TAG, "Detail_NewView");
		// set text
		detail_title.setText("新增");
		idTv.setText(""+findMaxId());
		button.setText("新增資料");
	}

	private void Detail_EditView() {
		Log.i(TAG, "Detail_EditView");
		// cursor select
		Cursor c = db.rawQuery(
				"SELECT id,lat,lng,buildingname,contenttext," +
				"price,addr,pattern,footage,age,toward FROM building " +
				"WHERE id = "+id+";", null);

		Log.i(TAG, String.valueOf(c.getCount()));
		
		// 移至搜尋出來的第一筆資料
		c.moveToFirst();
		// set text
		detail_title.setText("編輯");
		idTv.setText(c.getString(0));
		latEt.setText(""+c.getDouble(1));
		lngEt.setText(""+c.getDouble(2));
		buildingEt.setText(c.getString(3));
		contenttextEt.setText(c.getString(4));
		priceEt.setText(""+c.getInt(5));
		addrEt.setText(c.getString(6));
		patternEt.setText(c.getString(7));
		footageEt.setText(""+c.getDouble(8));
		ageEt.setText(""+c.getDouble(9));
		towardEt.setText(c.getString(10));
		button.setText("修改完成");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button:
			if (layout.equals("New")) {							
				ContentValues cv = new ContentValues();
				cv.put("id", findMaxId());
				cv.put("lat", latEt.getText().toString());
				cv.put("lng", lngEt.getText().toString());
				cv.put("buildingname", buildingEt.getText().toString());
				cv.put("contenttext", contenttextEt.getText().toString());
				cv.put("price",priceEt.getText().toString());
				cv.put("addr", addrEt.getText().toString());
				cv.put("pattern",patternEt.getText().toString());
				cv.put("footage", footageEt.getText().toString());
				cv.put("age",ageEt.getText().toString());
				cv.put("toward", towardEt.getText().toString());
				long long1 = db.insert("building", "", cv);
				if (long1 == -1) {
					Toast.makeText(AdminDetail.this, "新增失敗!", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(AdminDetail.this, "新增成功!", Toast.LENGTH_LONG).show();
				}
			}
			if (layout.equals("Edit")) {
				ContentValues cv = new ContentValues();
				cv.put("lat", latEt.getText().toString());
				cv.put("lng", lngEt.getText().toString());
				cv.put("buildingname", buildingEt.getText().toString());
				cv.put("contenttext", contenttextEt.getText().toString());
				cv.put("price",priceEt.getText().toString());
				cv.put("addr", addrEt.getText().toString());
				cv.put("pattern",patternEt.getText().toString());
				cv.put("footage", footageEt.getText().toString());
				cv.put("age",ageEt.getText().toString());
				cv.put("toward", towardEt.getText().toString());
				long long2 = db.update("building", cv, "id="+id, null);
				if (long2 == -1) {
					Toast.makeText(AdminDetail.this, "修改失敗!", Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(AdminDetail.this, "修改成功!", Toast.LENGTH_LONG)
							.show();
				}
			}
			break;
		}
		buildingEt.setEnabled(false);
		lngEt.setEnabled(false);
		latEt.setEnabled(false);
		contenttextEt.setEnabled(false);
		priceEt.setEnabled(false);
		addrEt.setEnabled(false);
		patternEt.setEnabled(false);
		footageEt.setEnabled(false);
		ageEt.setEnabled(false);
		towardEt.setEnabled(false);
		button.setEnabled(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

}
