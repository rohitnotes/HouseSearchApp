package com.jwetherell.augmented_reality.data;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.jwetherell.augmented_reality.R;
import com.jwetherell.augmented_reality.ui.Marker;

/**
 * This class should be used as a example local data source. It is an example of
 * how to add data programatically. You can add data either programatically,
 * SQLite or through any other source.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class LocalDataSource extends DataSource {

    private List<Marker> cachedMarkers = new ArrayList<Marker>();
    private static Bitmap icon = null;

    public LocalDataSource(Resources res) {
        if (res == null) throw new NullPointerException();

        createIcon(res);
    }

    protected void createIcon(Resources res) {
        if (res == null) throw new NullPointerException();

        icon = BitmapFactory.decodeResource(res, R.drawable.icon);
    }

  //校內建築物
  	BuildingObject[] buildingItem = {
  			new BuildingObject("外語學院",22.756875,120.336141,0,Color.RED),
  			new BuildingObject("管理學院",22.757249, 120.337291,0,Color.RED),
  			new BuildingObject("產學合作中心",22.756085,120.337794,0,Color.RED),
  			new BuildingObject("行政大樓",22.756821,120.338091,0,Color.RED),
  			new BuildingObject("電資學院",22.757692,120.338335,0,Color.RED),
  			new BuildingObject("教學大樓",0,0,0,Color.RED),
  			new BuildingObject("工學院",22.758286,120.338358,0,Color.RED),
  			new BuildingObject("活動中心",22.754641,120.335386,0,Color.RED),
  			new BuildingObject("圖資管",	22.755699,120.335081,0,Color.RED),
  			new BuildingObject("語言教學中心",0,0,0,Color.RED),
  	};
    
    public List<Marker> getMarkers() {        
        
    	Marker[ ] marker = new Marker[buildingItem.length];
    	
    	for(int i=0;i<buildingItem.length;i++){
    		//標題 緯度 經度 不知道 顏色
    		marker[i] = new Marker(buildingItem[i].title,buildingItem[i].lat,buildingItem[i].lng,
        			buildingItem[i].aaa,buildingItem[i].color);
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
