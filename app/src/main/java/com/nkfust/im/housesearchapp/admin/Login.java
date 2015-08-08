package com.nkfust.im.housesearchapp.admin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nkfust.im.housesearchapp.Map;
import com.nkfust.im.housesearchapp.R;
import com.nkfust.im.housesearchapp.util.SQLiteDB;

public class Login extends Activity implements OnClickListener {
	private static final String TAG = "schoolMap_new::Login";

	private EditText usernameEt, passwordEt;
	private Button submitBtn,prevBtn;

	private ProgressDialog progressdialog;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				progressdialog.dismiss();
				Log.i(TAG, String.valueOf(IsOk));
				if(IsOk){
					Toast.makeText(Login.this, "登入成功!", Toast.LENGTH_LONG).show();
					Intent intent = new Intent();
					intent.setClass(Login.this, AdminList.class);
					startActivity(intent);
				} else {
					Toast.makeText(Login.this, "帳號或密碼錯誤!", Toast.LENGTH_LONG).show();
				} 
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		SetViewAndListener();
		progressdialog = new ProgressDialog(this);
	}

	private void SetViewAndListener() {
		Log.i(TAG, "SetViewAndListener");
		usernameEt = (EditText) this.findViewById(R.id.usernameEt);
		passwordEt = (EditText) this.findViewById(R.id.passwordEt);
		submitBtn = (Button) this.findViewById(R.id.submitBtn);
		submitBtn.setOnClickListener(this);
		prevBtn = (Button) this.findViewById(R.id.prevBtn);
		prevBtn.setOnClickListener(this);
	}

	// 帳密是否正確，預設為false
	boolean IsOk = false;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.submitBtn:
			Log.i(TAG, "-----Submit-----");

			// 取得輸入的帳號密碼
			final String username = usernameEt.getText().toString();
			final String password = passwordEt.getText().toString();

			// progressdialog show
			progressdialog = ProgressDialog.show(this,"登入中","請等待...",true);

			new Thread(new Runnable() {

				@Override
				public void run() {
					// 建立SQLite資料庫
					SQLiteDB sqlitedb = new SQLiteDB(Login.this);
					// 開啟SQLite資料庫
					sqlitedb.OpenDB();
					// 取得SQLite資料庫
					SQLiteDatabase db = sqlitedb.getDB();
					// cursor select
					Cursor c = db.rawQuery("SELECT id,uid,upw FROM admin;",
							null);

					Log.i(TAG, String.valueOf(c.getCount()));

					// 移至搜尋出來的第一筆資料
					c.moveToFirst();
					for (int i = 0; i < c.getCount(); i++) {
						if (c.getString(1).equals(username)
								&& c.getString(2).equals(password)) {
							IsOk = true;
							break;
						}
						// 移至下一筆資料
						c.moveToNext();
					}
					
					Message msg = new Message();
					msg.what = 0;
					mHandler.sendMessage(msg);
				}
			}).start();

			Log.i(TAG, "-----Submit-----END");
			break;
		case R.id.prevBtn:
			Intent intent = new Intent();
			intent.setClass(Login.this, Map.class);
			startActivity(intent);
			finish();
			break;
		}
	}

}
