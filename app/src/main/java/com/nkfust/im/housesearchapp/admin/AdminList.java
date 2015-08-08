package com.nkfust.im.housesearchapp.admin;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.nkfust.im.housesearchapp.R;
import com.nkfust.im.housesearchapp.util.SQLiteDB;
import com.nkfust.im.housesearchapp.util.Tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminList extends Activity implements OnItemClickListener, OnItemLongClickListener {
	private static final String TAG = "AdminList";
	
	private ArrayList<String> listName = new ArrayList<String>();
	
	private ListView listview;
	
	private Tool tool;
	
	private List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();

	private SQLiteDatabase db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adminlist);
		
		//tool
        tool = new Tool();
      	tool.setContext(getApplicationContext());

		// 建立SQLite資料庫
		SQLiteDB sqlitedb = new SQLiteDB(AdminList.this);
		// 開啟SQLite資料庫
		sqlitedb.OpenDB();
		// 取得SQLite資料庫
		db = sqlitedb.getDB();
      	
		//cursor select 
		Cursor c = tool.SQLiteRawQuery("SELECT id,buildingname FROM building;");
		
		/*
		// 移至搜尋出來的第一筆資料
		c.moveToFirst();
		//清空ArrayList避免重複
		listName.clear();
		for (int i = 0; i < c.getCount(); i++) {
			listName.add(c.getString(0));
			// 移至下一筆資料
			c.moveToNext();
		}
		*/
//		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//        android.R.layout.simple_list_item_1, listName);
		
		c.moveToFirst();
		items.clear();
        for (int i = 0; i < c.getCount(); i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("id", c.getString(0));
            item.put("buildingname", c.getString(1));
            items.add(item);
            c.moveToNext();
        }
		
		listview = (ListView)this.findViewById(R.id.listView1);
		

		SimpleAdapter adapter = new SimpleAdapter(this, 
                items, R.layout.simple_adapter, new String[]{"id", "buildingname"},
                new int[]{R.id.idtv, R.id.buildingnametv});
		
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(this);
		listview.setOnItemLongClickListener(this);
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		int itemId = Integer.valueOf((String) items.get(position).get("id"));
		
		//進入編輯頁面
		Intent intent = new Intent();
		intent.setClass(AdminList.this, AdminDetail.class);
		Bundle bundle = new Bundle();
		bundle.putInt("id", itemId);
		bundle.putString("layout", "Edit");
		intent.putExtras(bundle);
		startActivity(intent);
		
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		//刪除長按之建築物
		db.delete("building", "id="+items.get(position).get("id"), null);
		//刷新畫面
		Refresh();
		return true;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.adminlist_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		switch(item.getItemId()){
		case R.id.item1:
			// new building
			Bundle bundle = new Bundle();
			bundle.putString("layout", "New");
			intent.putExtras(bundle);
			intent.setClass(AdminList.this, AdminDetail.class);
			startActivity(intent);
			break;
		case R.id.item2:
			// go back
			intent.setClass(AdminList.this, Login.class);
			startActivity(intent);
			finish();
			break;
		case R.id.item3:
			// refresh activity
			Refresh();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Refresh();
	}
	
	private void Refresh(){
		onCreate(null);
	}
}
