package com.nkfust.im.housesearchapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jwetherell.augmented_reality.activity.AugmentedReality;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.data.NetworkDataSource;
import com.jwetherell.augmented_reality.ui.Marker;
import com.jwetherell.augmented_reality.widget.VerticalTextView;
import com.nkfust.im.housesearchapp.util.LocalDataSource;
import com.nkfust.im.housesearchapp.util.Tool;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class extends the AugmentedReality and is designed to be an example on
 * how to extends the AugmentedReality class to show multiple data sources.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class ARActivity extends AugmentedReality {
	
    private static final String TAG = "Demo";
    private static final String locale = Locale.getDefault().getLanguage();
    private static final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);
    private static final ThreadPoolExecutor exeService = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS, queue);
    private static final Map<String, NetworkDataSource> sources = new ConcurrentHashMap<String, NetworkDataSource>();

    private static Toast myToast = null;
    private static VerticalTextView text = null;

    private Tool tool;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //tool
        tool = new Tool();
      	tool.setContext(getApplicationContext());
        
        // Create toast
        myToast = new Toast(getApplicationContext());
        myToast.setGravity(Gravity.CENTER, 0, 0);
        // Creating our custom text view, and setting text/rotation
        text = new VerticalTextView(getApplicationContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        text.setLayoutParams(params);
        text.setBackgroundResource(android.R.drawable.toast_frame);
        text.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Small);
        text.setShadowLayer(2.75f, 0f, 0f, Color.parseColor("#BB000000"));
        myToast.setView(text);
        // Setting duration and displaying the toast
        myToast.setDuration(Toast.LENGTH_SHORT);
        
        //get cursor
        Cursor c = tool.SQLiteRawQuery("SELECT id,buildingname,lat,lng from building order by id asc;");
        
        // Local
        LocalDataSource localData = new LocalDataSource(this.getResources(),c);
        ARData.addMarkers(localData.getMarkers());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();

        Location last = ARData.getCurrentLocation();
        updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ar_menu, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected() item=" + item);
        switch (item.getItemId()) {
            case R.id.showRadar:
                showRadar = !showRadar;
                item.setTitle(((showRadar) ? "Hide" : "Show") + " Radar");
                break;
            case R.id.showZoomBar:
                showZoomBar = !showZoomBar;
                item.setTitle(((showZoomBar) ? "Hide" : "Show") + " Zoom Bar");
                zoomLayout.setVisibility((showZoomBar) ? LinearLayout.VISIBLE : LinearLayout.GONE);
                break;
            case R.id.exit:
                finish();
                break;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);

        updateData(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void markerTouched(Marker marker) {
        text.setText(marker.getName());
        myToast.show();
        GoToResult(marker.getName());
    }

    private void GoToResult(String Name) {
    	//get cursor
    	Cursor c = tool.SQLiteRawQuery("SELECT id,lat,lng,buildingname,contenttext"
    			+ ",price,addr,pattern,footage,age,toward from building "
        		+ "WHERE buildingname='"+Name+"';");
    	
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
        //building's age
        double buildingAge = c.getDouble(9);
        //toward 
        String buildingToward = c.getString(10);
        
        Log.i(TAG,"buildingID:"+buildingID);
        Log.i(TAG,"buildingLat:"+buildingLat);
        Log.i(TAG,"buildingLng:"+buildingLng);
        Log.i(TAG,"buildingName:"+buildingName);
        Log.i(TAG,"buildingContent:"+buildingContent);
        Log.i(TAG,"buildingPrice:"+buildingPrice);
        Log.i(TAG,"buildingAddr:"+buildingAddr);
        Log.i(TAG,"buildingPattern:"+buildingPattern);
        Log.i(TAG,"buildingFootage:"+buildingFootage);
        Log.i(TAG,"buildingAge:"+buildingAge);
        Log.i(TAG,"buildingToward:"+buildingToward);
        
        
    	//new一個intent物件，並指定Activity切換的class
    	Intent intent = new Intent();
		intent.setClass(ARActivity.this, MainActivity.class);
		
		//new一個Bundle物件，並將要傳遞的資料傳入
        Bundle bundle = new Bundle();
        bundle.putInt("buildingID",buildingID);
        bundle.putDouble("buildingLat", buildingLat);
        bundle.putDouble("buildingLng", buildingLng);
        bundle.putString("buildingName",buildingName);
        bundle.putString("buildingContent",buildingContent);
        bundle.putString("type", "ARActivity");
        bundle.putInt("buildingPrice", buildingPrice);
        bundle.putString("buildingAddr", buildingAddr);
        bundle.putString("buildingPattern", buildingPattern);
        bundle.putDouble("buildingFootage", buildingFootage);
        bundle.putDouble("buildingAge", buildingAge);
        bundle.putString("buildingToward", buildingToward);
        //將Bundle物件assign給intent
        intent.putExtras(bundle);
		
        //切換activity
		startActivity(intent);
		
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateDataOnZoom() {
        super.updateDataOnZoom();
        Location last = ARData.getCurrentLocation();
        updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
    }

    private void updateData(final double lat, final double lon, final double alt) {
        try {
            exeService.execute(new Runnable() {
                @Override
                public void run() {
                    for (NetworkDataSource source : sources.values())
                        download(source, lat, lon, alt);
                }
            });
        } catch (RejectedExecutionException rej) {
            Log.w(TAG, "Not running new download Runnable, queue is full.");
        } catch (Exception e) {
            Log.e(TAG, "Exception running download Runnable.", e);
        }
    }

    private static boolean download(NetworkDataSource source, double lat, double lon, double alt) {
        if (source == null) return false;

        String url = null;
        try {
            url = source.createRequestURL(lat, lon, alt, ARData.getRadius(), locale);
        } catch (NullPointerException e) {
            return false;
        }

        List<Marker> markers = null;
        try {
            markers = source.parse(url);
        } catch (NullPointerException e) {
            return false;
        }

        ARData.addMarkers(markers);
        return true;
    }
}
