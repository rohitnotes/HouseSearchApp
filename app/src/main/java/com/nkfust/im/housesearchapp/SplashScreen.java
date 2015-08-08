package com.nkfust.im.housesearchapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;


public class SplashScreen extends Activity {

	// Splash screen timer
	private static int SPLASH_TIME_OUT = 3000;
	private ImageView logo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		logo = (ImageView) findViewById(R.id.imaglogo);

		new Handler().postDelayed(new Runnable() {

			/*
			 * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

			@Override
			public void run() {
				// This method will be executed once the timer is over
				// Start your app main activity
				Intent i = new Intent(SplashScreen.this, GirdView.class);
				startActivity(i);
				finish();
				// logo.setAnimation(AnimationUtils.loadAnimation
				// (SplashScreen.this, R.layout.activity_splash));

				// close this activity
				// finish();
			}
		}, SPLASH_TIME_OUT);
	}
}
