package com.nkfust.im.housesearchapp.util;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.jwetherell.augmented_reality.data.DataSource;
import com.jwetherell.augmented_reality.ui.IconMarker;
import com.jwetherell.augmented_reality.ui.Marker;
import com.nkfust.im.housesearchapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * This class should be used as a example local data source. It is an example of
 * how to add data programatically. You can add data either programatically,
 * SQLite or through any other source.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class LocalDataSource extends DataSource {

	private final static String TAG = "arbuilding::LocalDataSource";
	
    private List<Marker> cachedMarkers = new ArrayList<Marker>();
    private static Bitmap icon = null;
    private Cursor c;
    
    public LocalDataSource(Resources res, Cursor c) {
        if (res == null) throw new NullPointerException();

        createIcon(res);
        
        //取得cursor
        this.c = c;
    }

    protected void createIcon(Resources res) {
        if (res == null) throw new NullPointerException();

//        icon = BitmapFactory.decodeResource(res, R.drawable.icon);
        icon = BitmapFactory.decodeResource(res, R.drawable.home_badge);
    }

  //校內建築物
//  	BuildingObject[] buildingItem = {
//  			new BuildingObject("外語學院",22.756875,120.336141,0,Color.RED),
//  			new BuildingObject("管理學院",22.757249, 120.337291,0,Color.RED),
//  			new BuildingObject("產學合作中心",22.756085,120.337794,0,Color.RED),
//  			new BuildingObject("行政大樓",22.756821,120.338091,0,Color.RED),
//  			new BuildingObject("電資學院",22.757692,120.338335,0,Color.RED),
//  			new BuildingObject("教學大樓",0,0,0,Color.RED),
//  			new BuildingObject("工學院",22.758286,120.338358,0,Color.RED),
//  			new BuildingObject("活動中心",22.754641,120.335386,0,Color.RED),
//  			new BuildingObject("圖資管",	22.755699,120.335081,0,Color.RED),
//  			new BuildingObject("語言教學中心",0,0,0,Color.RED),
//  			new BuildingObject("嘉義吧",23.485038, 120.204688,0,Color.GREEN)
//  	};
    private ARBuildingObject[] buildingItem;
    
    public List<Marker> getMarkers() {        
        
    	Marker[ ] marker = new Marker[c.getCount()];
    	
    	buildingItem = new ARBuildingObject[c.getCount()];
    	c.moveToFirst();
    	for(int i=0;i<c.getCount();i++){
			buildingItem[i] = new ARBuildingObject(c.getInt(0), c.getString(1),
					c.getDouble(2),c.getDouble(3),
					0,Color.BLACK,icon);
			// 移至下一筆資料
			c.moveToNext();
		}
    	for(int i=0;i<buildingItem.length;i++){
    		Log.i(TAG,"("+(i+1)+")"
    				+" 名稱:"+buildingItem[i].buildingname
    				+" 緯度:"+buildingItem[i].lat
    				+" 經度:"+buildingItem[i].lng
    				+" 高度:"+buildingItem[i].att
    				+" 顏色:"+buildingItem[i].color
    				+" 圖片:"+buildingItem[i].bitmap);
    		//建築物名稱 緯度 經度 緯度 顏色 icon
    		marker[i] = new IconMarker(buildingItem[i].buildingname,buildingItem[i].lat,buildingItem[i].lng,
        			buildingItem[i].att,buildingItem[i].color,buildingItem[i].bitmap);
    		cachedMarkers.add(marker[i]);
    	}
        
        /*
         * Marker lon = new IconMarker(
         * "I am a really really long string which should wrap a number of times on the screen."
         * , 39.95335, -74.9223445, 0, Color.MAGENTA, icon);
         * cachedMarkers.add(lon); Marker lon2 = new IconMarker(
         * "2: I am a really really long string which should wrap a number of times on the screen."
         * , 39.95334, -74.9223446, 0, Color.MAGENTA, icon);
         * cachedMarkers.add(lon2);
         */

        /*
         * float max = 10; for (float i=0; i<max; i++) { Marker marker = null;
         * float decimal = i/max; if (i%2==0) marker = new Marker("Test-"+i,
         * 39.99, -75.33+decimal, 0, Color.LTGRAY); marker = new
         * IconMarker("Test-"+i, 39.99+decimal, -75.33, 0, Color.LTGRAY, icon);
         * cachedMarkers.add(marker); }
         */

        return cachedMarkers;
    }
}
