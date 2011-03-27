package com.phasip.lectureview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * A small java application for android that indexes academicearth.org and makes
 * the lectures browsable.
 * 
 * @author Pasi Saarinen
 * @email phasip@gmail.com
 */
public class LectureViewer extends ListActivity implements ViewHandler<Link> {
	private static final String APP_NAME = "Lecture Viewer";
	/* Our fun constants... not much really */
	private final static String MAIN_URL = "http://www.academicearth.org";
	private final static String STORE_SEPARATOR = "|_|";
	private final static int MENU_SUBJECTS = 0;
	private final static int MENU_TOPICS = 1;
	private final static int MENU_COURSES = 2;
	private final static int MENU_LECTURES = 3;
	private final static int MENU_PLAY = 4;
	private final static int PATTERN_DL = 0;
	private final static int PATTERN_YOUTUBE = 1;
	private final static int PATTERN_FLV = 2;
	private final static int PATTERN_FLASH = 3;
	private final static int PLAY_PATTERN_END = 3;
	private String postMsg = "";

	/* All regex patterns used to find the matches */
	private enum MyPattern {
		SUBJECTS("<li><a href=\"(/subjects/.+?)\">(.*?)</a></li>"), TOPICS(
				"<li><a href=\"(/subjects/.*?)\" class='o?n? ?clearfix'>(.*?)</a></li>"), COURSES(
				"<h3><a href=\"(/courses/.*?)\">(.*?)</a></h3>\\s*<h4><a href=\".*?\">(.*?)</a> / <a href=\".*?\">(.*?)</a></h4>\\s*(<h5 class=\"speakers-long\">\\s*<a href=\".*?\">\\s*.*?\\s*</a><br/>\\s*</h5>)?") {
			public void parseMatch(Link result, Matcher m) {
				result.setName(m.group(2));
				result.setUrl(MAIN_URL + m.group(1));
				String desc = m.group(3) + " / " + m.group(4);
				if (m.groupCount() == 5 && m.group(5) != null) {
					String n = m.group(5);
					// Log.d(APP_NAME,n);
					Pattern p = Pattern
							.compile("<h5 class=\"speakers-long\">\\s*<a href=\".*?\">\\s*(.*?)\\s*</a><br/>\\s*</h5>");
					Matcher nm = p.matcher(n);
					if (nm.find())
						desc += " - " + nm.group(1);
				}
				result.setDesc(desc);
			}
		},
		LECTURES("<h4><a href=\"(/lectures/.*?)\">(.*?)</a></h4>"), PAGES(
				"<li><span><a href=\"(/subjects/view/.*?/../../../subjects/.*?/page:[0-9]+/category:.*?)\">([0-9]+)</a></span>");
		public void parseMatch(Link result, Matcher m) {
			result.setName(m.group(2));
			result.setUrl(MAIN_URL + m.group(1));
		}

		String pattern;

		public String getPattern() {
			return pattern;
		}

		MyPattern(String s) {
			pattern = s;
		}

		public static MyPattern fromInt(int a) {
			switch (a) {
			case MENU_SUBJECTS:
				return SUBJECTS;
			case MENU_TOPICS:
				return TOPICS;
			case MENU_COURSES:
				return COURSES;
			case MENU_LECTURES:
				return LECTURES;
			}
			return null;
		}
	}

	private final static String[] PATTERNS = new String[4];
	{
		PATTERNS[PATTERN_FLASH] = "<textarea id=\"lecture-embed\" readonly=\"readonly\">(.+?)</textarea>";
		PATTERNS[PATTERN_DL] = "<a href=\"(.+?)\" class=\"download-link\">Download Video </a>";
		PATTERNS[PATTERN_FLV] = "flashVars.flvURL = \"(http://.*?flv)\";";
		PATTERNS[PATTERN_YOUTUBE] = "<textarea id=\"lecture-embed\" readonly=\"readonly\"><div><embed src=\"http://www.youtube.com/v/(.+?)\" type=\"application/x-shockwave-flash\"";
	}

	/* The array storing the info about the current items in the datalist */
	private ArrayList<Link> currentView = new ArrayList<Link>();
	/* Progressbar */
	private ProgressDialog proccessDialog;
	/* Where we are in the browsing */
	private int menuLevel = MENU_SUBJECTS;
	/* The stack tree */
	private StorableStringList lastUrl;
	/* Current url */
	private String currUrl = "";
	/* Shall we play a flvmovie */
	private boolean flvPlayback = false;
	/* Our version */
	private int version = -1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Create listview */
		SpecArrayAdapter<Link> arrayAdapter = new SpecArrayAdapter<Link>(this,
				R.layout.listitem, currentView, this);
		setListAdapter(arrayAdapter);
		this.setContentView(R.layout.main);
		final ListView mainListview = getListView();
		mainListview.setTextFilterEnabled(true);
		mainListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			/* What to do when listview clicked */
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final Link loadUrl = (Link) parent.getItemAtPosition(position);
				showMenu(menuLevel + 1, loadUrl.getUrl()); // Yes, check the
				// menulevel
				// numbers and you'll
				// see
			}
		});

		loadPreferences();
		versionCheck(); // Ugly, but also calls showmenu.
	}

	private void versionCheck() {
		final int myVer = getVersion();
		if (myVer > version) {
			/*
			 * Stolen from
			 * http://stackoverflow.com/questions/4300012/displaying-
			 * a-dialog-in-oncreate
			 */
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.first_run_version_title);
			builder.setMessage(R.string.first_run_message);
			builder.setNeutralButton(R.string.ok_menu_button,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							final SharedPreferences settings = getPreferences(0);
							SharedPreferences.Editor e = settings.edit();
							e.putInt("VERSION", myVer);
							e.commit();
							showMenu();
						}
					});
			AlertDialog alert = builder.create();
			alert.show(); // <-- Forgot this in the original post
		} else {
			showMenu();
		}
	}

	private void showMenu() {
		showMenu(menuLevel, currUrl);
	}

	private int getVersion() {
		PackageInfo pinfo;
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return pinfo.versionCode;
		} catch (NameNotFoundException e) {
			return -2;
		}
	}

	private void loadPreferences() {
		currUrl = MAIN_URL + "/subjects";
		menuLevel = MENU_SUBJECTS;
		final SharedPreferences settings = getPreferences(0);
		flvPlayback = settings.getBoolean("playflv", false);
		// Log.i(APP_NAME, "playflv:" + flvPlayback);
		version = settings.getInt("VERSION", -1);
		currUrl = settings.getString("currUrl", MAIN_URL + "/subjects");
		menuLevel = settings.getInt("menuLevel", MENU_SUBJECTS);
		String tmp = settings.getString("last", MAIN_URL + "/subjects");
		lastUrl = StorableStringList.loadString(tmp, STORE_SEPARATOR);
	}

	@Override
	public void onBackPressed() {
		// Move one url back.
		if (lastUrl.size() < 2) {
			this.finish();
			return;
		}
		String tmp = lastUrl.removeLast();
		showMenu(menuLevel - 1, tmp);
		lastUrl.removeLast();
		// Log.i(APP_NAME, "Lasturl list(backpress): " + lastUrl.toString());
	}

	private void showMenu(int id, String url) {
		// Log.d(APP_NAME, "showMenu, id: " + id + " url: " + url);
		menuLevel = id;

		// If clicking Link entry with url "retry", try to reload stuff.
		if (url.equals("retry")) {
			// Log.d(APP_NAME, "Retrying: " + currUrl);
			url = currUrl;
			menuLevel--;
		} else if (!currUrl.equals(url)) {
			lastUrl.add(currUrl);
			// Log.i(APP_NAME, "Lasturl list: " + lastUrl.toString());
		}
		currUrl = url;

		Resources res = getResources(); // Get string array list
		String[] loadString = res.getStringArray(R.array.loadStringArray);
		// The array is corresponding to the constants
		// Show ProgressDialog

		String mess = "Loading Unknown";
		if (loadString.length > id && id >= 0)
			mess = loadString[id];
		// Log.d(APP_NAME,"id: " + id + " - loadString: " + loadString[id] +
		// " mess: " +mess );
		proccessDialog = ProgressDialog.show(this, "Working..", mess, true,
				false);

		new DownloadHandler().execute();

	}

	private void loadPattern(MyPattern pattern, String data,
			ArrayList<Link> result) {
		if (data == null)
			return;

		Pattern p = Pattern.compile(pattern.getPattern(), Pattern.MULTILINE);
		Matcher m = p.matcher(data);
		while (m.find()) {
			if (m.group(1).length() == 0)
				continue;
			// Log.d("LectureViewer", "loadPattern: " + MAIN_URL +
			// m.group(1) + " - " + m.group(2));
			Link l = new Link();
			pattern.parseMatch(l, m);
			result.add(l);

		}
	}

	private void parseCourses(String data) throws ClientProtocolException,
			URISyntaxException, IOException {
		ArrayList<Link> pages = new ArrayList<Link>();
		loadPattern(MyPattern.PAGES, data, pages);
		loadPattern(MyPattern.COURSES, data, currentView);
		for (int i = 0; i < pages.size() / 2; i++) {
			data = getUrlData(pages.get(i).getUrl());
			loadPattern(MyPattern.COURSES, data, currentView);
		}

	}

	private void shortToast(String msg) {
		Context context = getApplicationContext();
		int len = Toast.LENGTH_SHORT;
		if (msg.length() > 20)
			len = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, msg, len);
		toast.show();
	}

	/**
	 * Reads url
	 * 
	 * @param url
	 *            Web page to fetch, eg http://google.com
	 * @return String containing page.
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	/*
	 * Gets data from url, throws all exceptions, Urisyntax when bad url give,
	 * clientprotocol... not sure and io when connection error.
	 */
	private String getUrlData(String url) throws URISyntaxException,
			ClientProtocolException, IOException {
		/* Return value */
		String websiteData = null;
		/* Create a http client */
		DefaultHttpClient client = new DefaultHttpClient();
		/* Load the uri (http://... ) */
		URI uri = new URI(url);
		/* Get method, use HttpPost for post (eg login) */
		HttpGet method = new HttpGet(uri);
		/* Execute the query */
		HttpResponse res = client.execute(method);
		/* make data ready to read the page */
		InputStream data = res.getEntity().getContent();
		websiteData = generateString(data);
		return websiteData;
	}

	/**
	 * Read string from InputStream
	 * 
	 * @param stream
	 *            Ready to read InputStream.
	 * @return String.
	 * @throws IOException
	 *             when read error occurs.
	 */
	private String generateString(InputStream stream) throws IOException {
		BufferedReader buffer = new BufferedReader(
				new InputStreamReader(stream));

		/* Use StringBuilder because java's strings are horribly slow */
		StringBuilder sb = new StringBuilder();
		char[] buff = new char[256];
		int read = buffer.read(buff);
		while (read != -1) {
			sb.append(buff, 0, read);
			read = buffer.read(buff);
		}
		stream.close();
		return sb.toString();
	}

	// Maybe a bit ugly.
	private void playMovie(String data) {

		menuLevel--; // We never stay in playmovie.
		currUrl = lastUrl.remove(lastUrl.size() - 1);
		int currPattern = PATTERN_DL;
		Matcher m = null;
		/* Find a working pattern */
		for (currPattern = 0; currPattern <= PLAY_PATTERN_END; currPattern++) {
			/* If flv pattern enabled */
			if (currPattern == PATTERN_FLV && !flvPlayback)
				continue;

			Pattern p = Pattern.compile(PATTERNS[currPattern]);
			m = p.matcher(data);
			if (m.find())
				break;
			Log.d(APP_NAME, "Pattern " + currPattern
					+ " not matched in playMovie");
		}
		/* If we looped trough */
		if (currPattern > PLAY_PATTERN_END) {
			Log.d(APP_NAME, "No play patterns found, something is wrong");
			return;
		}

		String match = m.group(1);
		String type = null;
		switch (currPattern) {
		case PATTERN_DL:
			type = "video/*";
			break;
		case PATTERN_YOUTUBE:
			match = "vnd.youtube:" + match;
			break;
		case PATTERN_FLV:
			type = "video/x-flv";
		case PATTERN_FLASH:
			if (match.startsWith("<div><embed src=\"\"")) {
				postMsg = "Could not find movie to play, if this lecture has a video please report error and refer to the lecture";
				return;
			}
			postMsg = "Could not find movie to play, if this lecture has a video please report error and refer to the lecture";
			return;

		}

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(match), type);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			switch (currPattern) {
			case PATTERN_DL:
				postMsg = "Could not find application to play video file (video/*)";
				break;
			case PATTERN_YOUTUBE:
				postMsg = "Could not find application to play youtube clip (vnd.youtube)";
				break;
			case PATTERN_FLV:
				postMsg = "Could not find application to play flv clip (video/x-flv)";
				break;

			}
			return;
		}
		/*
		 * handler.sendEmptyMessage(SHOW_ERROR_TOASTER); String html =
		 * "<html><body bgcolor=\"#000000\">" + match + "</body></html>"; html =
		 * html.replace("width=\"500\" height=\"311\"",
		 * "width=\"100%\" height=\"100%\"");
		 * 
		 * flashPlayer.putExtra("url", currUrl); flashPlayer.putExtra("data",
		 * html); startActivity(flashPlayer);
		 */
	}

	// @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences settings = getPreferences(0);
		SharedPreferences.Editor editor = settings.edit();
		if (item.getItemId() == R.id.enableflv) {
			flvPlayback = !flvPlayback;
			int icon = flvPlayback ? android.R.drawable.button_onoff_indicator_on
					: android.R.drawable.button_onoff_indicator_off;
			item.setIcon(icon);
			editor.putBoolean("playflv", flvPlayback);
			Log.i(APP_NAME, "FlvPlayback: " + flvPlayback);
			shortToast(flvPlayback ? ".flv playback enabled"
					: ".flv playback disabled");
		} else if (item.getItemId() == R.id.setasdefault) {
			editor.putString("currUrl", currUrl);
			editor.putInt("menuLevel", menuLevel);
			editor.putString("last", lastUrl.saveString(STORE_SEPARATOR));
			shortToast("Current view set to default");
		}
		editor.commit();
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!flvPlayback)
			menu.getItem(0).setIcon(
					android.R.drawable.button_onoff_indicator_off);
		return true;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	private class DownloadHandler extends AsyncTask<Void, Void, String> {
		protected String doInBackground(Void... none) {
			String data;
			try {
				data = getUrlData(currUrl);
				switch (menuLevel) {
				case MENU_SUBJECTS:
				case MENU_TOPICS:
				case MENU_LECTURES:
					currentView.clear();
					loadPattern(MyPattern.fromInt(menuLevel), data, currentView);
					break;
				case MENU_COURSES:
					currentView.clear();
					parseCourses(data);
					Collections.sort(currentView); // This can have multiple
					// pages...
					break;
				case MENU_PLAY:
					playMovie(data);
					break;
				default:
					finish();
					return null;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return "Exception... Your phone doesn't support http?";
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return "Exception... Maybe the regex?";
			} catch (IOException e) {
				e.printStackTrace();
				return "Connection error, please retry.";
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		protected void onPostExecute(String error) {
			proccessDialog.dismiss();
			if (postMsg != null && !postMsg.equals("")) {
				shortToast(postMsg);
				postMsg = null;
			}
			if (error != null) {
				currentView.clear();
				currentView.add(new Link(
						"Connection Fails, Press here to retry", "retry")); // Maybe
				// this
				// is
				// ugly...
				shortToast(error);
			}
			((ArrayAdapter<Link>) getListAdapter()).notifyDataSetChanged();
		}

	}

	@Override
	public void handle(View v, Link object) {
		TextView t = (TextView) v.findViewById(R.id.maintext_sa);
		t.setText(object.getName());
		t = (TextView) v.findViewById(R.id.desctext_sa);
		if (object.getDesc() == null)
			t.setText("");
		else
			t.setText(object.getDesc());
	}

}