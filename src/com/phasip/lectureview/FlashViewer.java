package com.phasip.lectureview;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

public class FlashViewer extends Activity {
	WebView webview;
	WakeLock mWakeLock;
	public void onCreate(Bundle savedInstanceState) {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "My Tag");
		mWakeLock.acquire();

		
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Log.e("LectureViewer", "NO EXTRAS!!!");
		}
		String url = extras.getString("url");
		String data = extras.getString("data");
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.webview);
		webview = (WebView) findViewById(R.id.WebView01);
		webview.getSettings().setJavaScriptEnabled(true);
		//setPluginState(PluginState.ON);
		//PluginManager.getInstance();
		webview.getSettings().setPluginsEnabled(true);
		webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); 
		webview.loadDataWithBaseURL(url, data,
				"text/html", "utf-8", url);
	}
	public void onBackPressed() {
		webview.destroy();
		mWakeLock.release();
		this.finish();
		return;
	}
}
