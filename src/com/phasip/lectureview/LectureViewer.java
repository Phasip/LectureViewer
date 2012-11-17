package com.phasip.lectureview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;

import com.phasip.lectureview.background.Settings;
import com.phasip.lectureview.list.LectureListView;
import com.phasip.lectureview.listhandlers.FavListHandler;
import com.phasip.lectureview.listhandlers.ListHandler;
import com.phasip.lectureview.listhandlers.SearchListHandler;
import com.phasip.lectureview.listhandlers.SubjectsListHandler;

/**
 * A small java application for android that indexes academicearth.org and makes
 * the lectures browsable.
 * 
 * @author Pasi Saarinen
 * @email phasip@gmail.com
 */
public class LectureViewer extends Activity {
	private static final String TAB_BROWSE = "browse";
	private static final String TAB_FIND = "find";
	private static final String STORE_FAV = "fav_v2";
	public static final String APP_NAME = "Lecture Viewer";
	/* Our fun constants... not much really */
	public final static String MAIN_URL = "http://www.academicearth.org";

	/* All regex patterns used to find the matches */
	
	LectureListView browseList;
	LectureListView favList;
	LectureListView findList;
	public void invalidateLists() {
		browseList.invalidate();
		browseList.invalidateViews();
		favList.invalidate();
		favList.invalidateViews();
		findList.invalidate();
		findList.invalidateViews();
	}
	TabHost tabs;
	private ListHandler browseHandler;
	@SuppressWarnings("unused")
	private ListHandler favHandler;
	private ListHandler findHandler;
	Settings settings;

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = Settings.getInstance(this);
		tabCreate();
		favList = (LectureListView) this.findViewById(R.id.favList);
		findList = (LectureListView) this.findViewById(R.id.findList);
		Button findButton = (Button) this.findViewById(R.id.findButton);
		EditText findText = (EditText) this.findViewById(R.id.findText);
		browseList = (LectureListView) this.findViewById(R.id.browseList);
		browseHandler = new SubjectsListHandler(browseList, this);
		Link last = settings.popLinks();
		if (last == null)
			browseHandler = new SubjectsListHandler(browseList, this);
		else
			browseHandler = last.launchHandler(browseList, this);
		
		
		findHandler = new SearchListHandler(findList, findButton, findText, this);
		favHandler = new FavListHandler(favList,browseList, this);
		versionCheck();
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

	private void versionCheck() {
		final int myVer = getVersion();
		if (myVer > settings.getVersion()) {
			/*
			 * Stolen from
			 * http://stackoverflow.com/questions/4300012/displaying-
			 * a-dialog-in-oncreate
			 */
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.first_run_version_title);
			builder.setMessage(R.string.first_run_message);
			builder.setNeutralButton(R.string.ok_menu_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					settings.setVersion(myVer);
				}
			});
			AlertDialog alert = builder.create();
			alert.show();

		}
	}

	public void setBrowseTab() {
		tabs.setCurrentTabByTag(TAB_BROWSE);
	}

	@Override
	public void onBackPressed() {
		String tag = tabs.getCurrentTabTag();

		if (tag.equals(TAB_BROWSE)) {
			browseHandler.launchPrev();
		} else if (tag.equals(TAB_FIND)) {
			findHandler.launchPrev();
		} else if (tag.equals(STORE_FAV)) {
			setBrowseTab();
		}
	}

}