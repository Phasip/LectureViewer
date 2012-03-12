package com.phasip.lectureview;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

/**
 * A small java application for android that indexes academicearth.org and makes
 * the lectures browsable.
 * 
 * @author Pasi Saarinen
 * @email phasip@gmail.com
 */
public class LectureViewer extends Activity implements OnItemClickListener {
	private static final String TAB_BROWSE = "browse";
	private static final String TAB_FIND = "find";
	private static final String STORE_LAST = "last";
	private static final String STORE_CACHE = "cache";
	private static final String STORE_FAV = "fav";
	private static final String STORE_VERSION = "VERSION";
	private static final String STORE_FLV = "playflv";
	private static final String STORE_RSS = "loadrss";
	public static final String APP_NAME = "Lecture Viewer";
	/* Our fun constants... not much really */
	public final static String MAIN_URL = "http://www.academicearth.org";
	private final static int PATTERN_DL = 0;
	private final static int PATTERN_YOUTUBE = 1;
	private final static int PATTERN_FLV = 2;
	private final static int PATTERN_FLASH = 3;
	private final static int PLAY_PATTERN_END = 3;
	private String mess = "Loading Unknown...";

	public final static int MENU_SUBJECTS = 0;
	public final static int MENU_TOPICS = 1;
	public final static int MENU_COURSES = 2;
	public final static int MENU_LECTURES = 3;
	public final static int MENU_PLAY = 4;
	public final static int MENU_SEARCH = 5;
	public final static int MENU_RETRY = 6;
	public final static int MENU_PAGES = 7;
	private static final int DIALOG_PROCESS = 2;
	private String postMsg = "";

	/* All regex patterns used to find the matches */

	private final static String[] PATTERNS = new String[4];
	private static final int ID_ADDFAV_FIND = 0;
	private static final int ID_ADDFAV_BROWSE = 1;
	private static final int ID_DELFAV = 2;
	private static final int ID_ADDRSS_BROWSE = 3;
	private static final int ID_ADDRSS_FIND = 4;
	private static final int ID_ADDRSS_FAV = 5;
	{
		PATTERNS[PATTERN_FLASH] = "<textarea id=\"lecture-embed\" readonly=\"readonly\">(.+?)</textarea>";
		PATTERNS[PATTERN_DL] = "<a href=\"(.+?)\" class=\"download-link\">Download Video </a>";
		PATTERNS[PATTERN_FLV] = "flashVars.flvURL ?= ?\"(.+?)\";";
		// flashVars.flvURL =
		// "http://blip.tv/file/get/AEaccount4-ECO39265.flv?source=2";
		PATTERNS[PATTERN_YOUTUBE] = "<textarea id=\"lecture-embed\" readonly=\"readonly\"><div><embed src=\"http://www.youtube.com/v/(.+?)\" type=\"application/x-shockwave-flash\"";
	}
	/* The array storing the info about the current items in the datalist */
	// private ArrayList<Link> currentView = new ArrayList<Link>();
	/* Progressbar */

	/* Where we are in the browsing */
	// private int menuLevel = MENU_SUBJECTS;
	/* The stack tree */
	// private StorableStringList lastUrl;
	/* Current url */
	// private String currUrl = "";
	/* Current Link */
	private Link currLink = null;
	/* Shall we play a flvmovie */
	private boolean flvPlayback = false;
	private boolean rssPlayback = false;
	private ProgressDialog proccessDialog;
	/* Our version */
	private int version = -1;
	LectureListView browseList;
	LectureListView favList;
	LectureListView findList;
	TabHost tabs;

	public void tabCreate() {
		setContentView(R.layout.tablayout);
		Resources res = getResources(); // Resource object to get Drawables

		tabs = (TabHost) findViewById(R.id.TabHost);

		tabs.setup();

		TabHost.TabSpec spec1 = tabs.newTabSpec(TAB_BROWSE);
		spec1.setIndicator("Browse", res.getDrawable(R.drawable.ic_tab_browse));
		spec1.setContent(R.id.mainTab);

		TabHost.TabSpec spec2 = tabs.newTabSpec(TAB_FIND);
		spec2.setIndicator("Find", res.getDrawable(R.drawable.ic_tab_find));
		spec2.setContent(R.id.findTab);

		TabHost.TabSpec spec3 = tabs.newTabSpec(STORE_FAV);
		spec3.setIndicator("Fav", res.getDrawable(R.drawable.ic_tab_fav));
		spec3.setContent(R.id.favTab);

		tabs.addTab(spec1);
		tabs.addTab(spec2);
		tabs.addTab(spec3);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Patterns tmp = Patterns.COURSES;
		if (v.getId() == R.id.browseList || v.getId() == R.id.findList) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			ArrayList<Link> list = browseList.getList();

			if (v.getId() == R.id.findList)
				list = findList.getList();

			String name = "ERROR";
			if (info.position >= 0 && info.position < list.size()) {
				name = list.get(info.position).getName();
				tmp = list.get(info.position).getType();
			}

			menu.setHeaderTitle(name);
			menu.add(Menu.NONE,
					(v.getId() == R.id.browseList) ? ID_ADDFAV_BROWSE
							: ID_ADDFAV_FIND, 0, "Add to fav");
			if (tmp == Patterns.LECTURES)
				menu.add(Menu.NONE,
						(v.getId() == R.id.browseList) ? ID_ADDRSS_BROWSE
								: ID_ADDRSS_FIND, 1, "Open RSS");
		}

		if (v.getId() == R.id.favList) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			ArrayList<Link> list = favList.getList();
			String name = "ERROR";
			if (info.position >= 0 && info.position < list.size()) {
				name = list.get(info.position).getName();
				tmp = list.get(info.position).getType();
			}

			menu.setHeaderTitle(name);
			menu.add(Menu.NONE, ID_DELFAV, 0, R.string.delfav);
			if (tmp == Patterns.LECTURES)
				menu.add(Menu.NONE, ID_ADDRSS_FAV, 1, "Open RSS");

		}
	}

	public void addFav(Link l) {
		if (l == null) {
			shortToast(R.string.err_null_fav);
			return;
		}
		l = l.clone();

		String desc = l.getDesc();
		ArrayList<Link> fav = favList.getList();
		if ((desc == null || desc.length() == 0) && currLink != null)
			l.setDesc(currLink.getName());

		if (fav.contains(l)) {
			shortToast(R.string.allready_fav);
			return;
		}
		favList.add(l);
		favList.notifyChange();

	}

	public void delFav(Link l) {
		ArrayList<Link> f = favList.getList();
		f.remove(l);
	}

	protected void onPause() {
		super.onPause();
		ArrayList<Link> l = favList.getList();
		storeObject(l, STORE_FAV);
		storeObject(WebFetcher.linkdata,STORE_CACHE);
		//Log.d(APP_NAME, "Saved cached data: " + WebFetcher.linkdata.size());
	}

	public Link getListItem(ArrayList<Link> list, int pos) {
		if (pos < 0 || pos >= list.size()) {
			shortToast("Index out of bounds error?...");
			return null;
		}
		return list.get(pos);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		ArrayList<Link> list;
		Link l;
		switch (item.getItemId()) {
		case ID_ADDRSS_BROWSE:
			l = getListItem(browseList.getList(), info.position);
			if (l == null)
				return true;
			play_rss(l);
			return true;
		case ID_ADDRSS_FIND:
			l = getListItem(findList.getList(), info.position);
			if (l == null)
				return true;
			play_rss(l);
			return true;

		case ID_ADDRSS_FAV:
			l = getListItem(favList.getList(), info.position);
			if (l == null)
				return true;
			play_rss(l);
			return true;

		case ID_ADDFAV_FIND:
			list = findList.getList();
			if (info.position < 0 || info.position >= list.size()) {
				shortToast("Index out of bounds error?...");
				return true;
			}
			l = list.get(info.position);
			addFav(l);
			return true;
		case ID_ADDFAV_BROWSE:
			list = browseList.getList();
			if (info.position < 0 || info.position >= list.size()) {
				shortToast("Index out of bounds error?...");
				return true;
			}
			l = list.get(info.position);
			addFav(l);
			return true;
		case ID_DELFAV:
			list = favList.getList();

			if (info.position < 0 || info.position >= list.size()) {
				shortToast("Index out of bounds error?...");
				return true;
			}
			list.remove(info.position);
			favList.notifyChange();
			return true;
		}

		return false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tabCreate();
		favList = (LectureListView) this.findViewById(R.id.favList);
		favList.setOnItemClickListener(this);
		findList = (LectureListView) this.findViewById(R.id.findList);
		findList.setOnItemClickListener(this);
		browseList = (LectureListView) this.findViewById(R.id.browseList);
		browseList.setOnItemClickListener(this);
		registerForContextMenu(browseList);
		registerForContextMenu(findList);
		registerForContextMenu(favList);

		final EditText findText = (EditText) this.findViewById(R.id.findText);
		findText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(findText.getWindowToken(), 0);
					findCourses(findText.getText());

				}
				return true;
			}
		});
		final Button findButton = (Button) this.findViewById(R.id.findButton);
		findButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(findText.getWindowToken(), 0);
				findCourses(findText.getText());

			}

		});
		loadPreferences();
		versionCheck(); // Ugly, but also calls showmenu.
	}

	public void findCourses(CharSequence find) {

		Link l = new Link();
		l
				.setUrl("http://academicearth.org/lectures/search/"
						+ find
						+ "/search-within:All/search-universities:All/search-subjects:All/search-rating:-1/search-sort:Relevancy/");
		l.setType(Patterns.SEARCH);
		showMenu(l);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final Link loadUrl = (Link) parent.getItemAtPosition(position);
		// Log.d(APP_NAME, APP_NAME + " Loading url: " + loadUrl.getUrl()
		// + " id: " + loadUrl.getIntType());
		if (loadUrl.getType() != Patterns.PLAY && loadUrl.getType() != Patterns.RETRY)
			tabs.setCurrentTabByTag(TAB_BROWSE);
		showMenu(loadUrl); // Yes, check the
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
							e.putInt(STORE_VERSION, myVer);
							e.commit();
							showMenu();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			showMenu();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_PROCESS) {
			ProgressDialog loadingDialog = new ProgressDialog(this);
			loadingDialog.setTitle("Working..");
			loadingDialog.setMessage(mess);
			loadingDialog.setIndeterminate(true);
			loadingDialog.setCancelable(false);
			return loadingDialog;
		}

		return super.onCreateDialog(id);
	}

	private void showMenu() {
		showMenu(currLink);
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
		currLink = new Link();
		currLink.setType(Patterns.SUBJECTS);
		currLink.setUrl(MAIN_URL + "/subjects");
		final SharedPreferences settings = getPreferences(0);
		// TODO: FLV PLAYBACK ALWAYS ENABLED
		flvPlayback = true; // settings.getBoolean(STORE_FLV, false);
		rssPlayback = settings.getBoolean(STORE_RSS, false);
		version = settings.getInt(STORE_VERSION, -1);
		
		WebFetcher.linkdata =  webFetchFromFile(STORE_CACHE);
		//Log.d(APP_NAME, "Loaded cached data: " + WebFetcher.linkdata.size());
		Link l = linkchainFromFile(STORE_LAST);
		if (l != null)
			currLink = l;
		ArrayList<Link> f = favList.getList();
		f.clear();
		ArrayList<Link> fa;
		try {
			fa = fromFile(STORE_FAV);
			if (fa != null)
				f.addAll(fa);

		} catch (Exception e) {
			shortToast(e.getMessage());
		}
		favList.notifyChange();

	}

	@Override
	public void onBackPressed() {
		if (currLink.getPrev() == null) {
			if (!currLink.hasType(Patterns.SUBJECTS))
			{
				//This is weird, reset currlink!
				currLink = new Link();
				currLink.setType(Patterns.SUBJECTS);
				currLink.setUrl(MAIN_URL + "/subjects");
				if (!storeLink(currLink, STORE_LAST)) {
					Log.d(APP_NAME, "Fails to save last location");
				}
			}
			this.finish();
			return;
		}
		currLink = currLink.getPrev();
		showMenu(currLink);
	}


	@Override
	public boolean onSearchRequested() {
		tabs.setCurrentTabByTag(TAB_FIND);
		EditText te = (EditText) findViewById(R.id.findText);
		te.setText("");
		te.requestFocus();
		return false; // don't go ahead and show the search box
	}

	private void showMenu(Link url) {
		//WebLogger.upload_log("ShowMenu url: " +  url.getUrl() + " name: " + url.getName() + " desc: " + url.getDesc() + " type: " + url.getIntType());

		// Log.d(APP_NAME, APP_NAME + " showMenu, id: " + url.getIntType()
		// + " url: " + url.getUrl());
		url = url.clone();
		// If clicking Link entry with url "retry", try to reload stuff.
		if (url.hasType(Patterns.RETRY)) {
			url = currLink.clone();
		} else if (!currLink.equals(url)) {
			if (currLink.hasType(Patterns.SEARCH)) // We don't want searches to
				// be redone when pressing
				// back.
				url.setPrev(currLink.getPrev());
			else
				url.setPrev(currLink);
		} else // currLink.equals(url)
		{
			url.setPrev(currLink.getPrev());
		}

		if (url.hasType(Patterns.LECTURES) && rssPlayback) {
			// Log.d(APP_NAME, url.getName() + " - " + url.getUrl());
			if (play_rss(url))
				return;
		}

		currLink = url;

		int id = currLink.getIntType();

		Resources res = getResources(); // Get string array list
		String[] loadString = res.getStringArray(R.array.loadStringArray);

		mess = loadString[loadString.length - 1];

		if (loadString.length > id && id >= 0)
			mess = loadString[id];

		proccessDialog = ProgressDialog.show(LectureViewer.this, "Working...",
				mess);
		new DownloadHandler().execute();

	}

	private boolean play_rss(Link url) {
		String match = url.getUrl() + "/video.rss";
		String type = "application/rss+xml";
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(match), type);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			shortToast("Fail to launch rss, please install rss application");
			return false;
		}
		return true;

	}

	private void shortToast(String msg) {
		Context context = getApplicationContext();
		int len = Toast.LENGTH_SHORT;
		if (msg.length() > 20)
			len = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, msg, len);
		toast.show();
	}

	private void shortToast(int msg) {
		Resources res = getResources(); // Get string array list
		String msg2 = res.getString(msg);
		shortToast(msg2);
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

	// Maybe a bit ugly.
	private void playMovie(String data) {

		currLink = currLink.getPrev();
		int currPattern = PATTERN_DL;
		Matcher m = null;
		/* Find a working pattern */
		for (currPattern = 0; currPattern <= PLAY_PATTERN_END; currPattern++) {
			/* If flv pattern enabled */
			if (currPattern == PATTERN_FLV && !flvPlayback) {
				// Log.d(APP_NAME, "Skipping FLV, not enabled!");
				continue;
			}

			Pattern p = Pattern.compile(PATTERNS[currPattern]);
			m = p.matcher(data);
			if (m.find())
				break;
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
			break;
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
				postMsg = "Could not find application to play flv clip (video/x-flv) - I recommend RockPlayer Lite";
				break;

			}
			return;
		}
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
			editor.putBoolean(STORE_FLV, flvPlayback);

			shortToast(flvPlayback ? R.string.flv_enabled
					: R.string.flv_disabled);
		} else if (item.getItemId() == R.id.setasdefault) {

			if (!storeLink(currLink, STORE_LAST))
				shortToast("Fails to set default, please retry later. If error perists, email developer");
			else
				shortToast(R.string.view_default);
		} else if (item.getItemId() == R.id.enablerss) {
			rssPlayback = !rssPlayback;
			int icon = rssPlayback ? android.R.drawable.button_onoff_indicator_on
					: android.R.drawable.button_onoff_indicator_off;
			item.setIcon(icon);
			editor.putBoolean(STORE_RSS, rssPlayback);

			shortToast(rssPlayback ? R.string.rss_enabled
					: R.string.rss_disabled);

		}

		editor.commit();
		return true;
	}

	private Link linkchainFromFile(String filename) {
		ArrayList<Link> l;
		try {
			l = fromFile(filename);
		} catch (ToastException e) {
			shortToast(e.getMessage());
			l = null;
		}

		if (l == null)
			return null;

		Link a = null;
		Link b = null;
		for (int i = 0; i < l.size(); i++) {
			b = l.get(i);
			b.setPrev(a);
			a = b;
		}
		return b;
	}
	private HashMap<Link,Pair<Long,ArrayList<Link>>> webFetchFromFile(String filename) {
		try {
			ObjectInputStream ois;
			FileInputStream fos = openFileInput(filename);
			ois = new ObjectInputStream(fos);
			@SuppressWarnings("unchecked")
			HashMap<Link,Pair<Long,ArrayList<Link>>> ret = (HashMap<Link,Pair<Long,ArrayList<Link>>>) ois.readObject();
			ois.close();
			return ret;
		} catch (ClassCastException e) {
			shortToast("We fail to load stored pages");
		} catch (StreamCorruptedException e) {
			shortToast("We fail to load stored pages");
		} catch (OptionalDataException e) {
			shortToast("We fail to load stored pages");
		} catch (ClassNotFoundException e) {
			shortToast("We fail to load stored pages");
		} catch (IOException e) {
			Log.d(APP_NAME, "webFetchFromFile", e);
		}
		return new HashMap<Link,Pair<Long,ArrayList<Link>>>();
	}
	@SuppressWarnings("unchecked")
	private ArrayList<Link> fromFile(String fileName) throws ToastException {
		try {
			ObjectInputStream ois;
			FileInputStream fos = openFileInput(fileName);
			ois = new ObjectInputStream(fos);
			ArrayList<Link> ret = (ArrayList<Link>) ois.readObject();
			ois.close();
			return ret;
		} catch (ClassCastException e) {
			throw new ToastException("Stream corrupted");
		} catch (StreamCorruptedException e) {
			throw new ToastException("Stream corrupted");
		} catch (OptionalDataException e) {
			throw new ToastException("Stream corrupted");
		} catch (ClassNotFoundException e) {
			throw new ToastException("Stream corrupted");
		} catch (IOException e) {
			return null;
		}

	}

	private boolean storeLink(Link l, String filename) {
		ArrayList<Link> put = new ArrayList<Link>();
		while (l != null) {
			put.add(l);
			l = l.getPrev();
		}
		return storeObject(put, filename);
	}

	private boolean storeObject(Object o, String fileName) {
		try {
			FileOutputStream fos = openFileOutput(fileName,
					Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.close();
			return true;
		} catch (IOException e) {
			return false;
		}

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
		LectureListView currentList;
		ArrayList<Link> currentView;

		protected String doInBackground(Void... none) {

			if (currLink.getType() == Patterns.PLAY) {
				String data;
				currentList = browseList;
				currentView = browseList.getList();
				try {
					// Log.d(APP_NAME,"Playing: " + currLink.getUrl());
					data = WebFetcher.getUrlData(currLink.getUrl());
					playMovie(data);
				} catch (ToastException e) {
					return e.getMessage();
				}

				return null;
			} else if (currLink.getType() == Patterns.SEARCH)
				currentList = findList;
			else {
				currentList = browseList;
				if (!storeLink(currLink, STORE_LAST)) {
					Log.d(APP_NAME, "Fails to save last location");
				}
			}

			currentView = currentList.getList();
			try {
				WebFetcher.fetch(currentView, currLink);
				return null;
			} catch (ToastException e) {
				return e.getMessage();
			}
		}

		protected void onPostExecute(String error) {
			if (proccessDialog != null) {
				try {
					proccessDialog.hide();
					proccessDialog.dismiss();
				} catch (IllegalArgumentException e) {
					Log.d(APP_NAME,
							"Roatation caused dialog error... ignoring.");
				}
			}

			if (postMsg != null && !postMsg.equals("")) {
				shortToast(postMsg);
				postMsg = null;
			}
			if (error != null) {
				currentView.clear();
				Link retryLink = new Link(
						"Connection Fails, Press here to retry", "retry");
				retryLink.setType(Patterns.RETRY);
				currentView.add(retryLink);
			}
			if (currentView.isEmpty()) {
				Link retryLink = new Link(
						"No Results, Press here to retry", "retry");
				retryLink.setType(Patterns.RETRY);
				currentView.add(retryLink);
			}
			
			currentList.notifyChange();
		}
	}

}