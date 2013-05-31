package com.phasip.lectureview.background;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Base64;

import com.phasip.lectureview.Link;
import com.phasip.lectureview.Patterns;
import com.phasip.lectureview.Stuff;
import com.phasip.lectureview.listhandlers.FavListHandler;

public class Settings {
	private static final String LINKSTACK = "LINKSTACK-";
	private static final String LINKSTACKLEN = "LINKSTACKLEN";
	private static final String PREFS_NAME = "lectureview";
	private SharedPreferences mPrefs;
	private FavListHandler fav;
	private static Settings s;

	public Settings(Activity activity) {
		mPrefs = activity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

	}

	public static Settings getInstance(Activity activity) {
		if (s == null)
			s = new Settings(activity);
		return s;
	}
	public void delFavourite(Link link) {
		fav.deleteFav(link);
	}
	public void addFavourite(Link link) {
		fav.addFav(link);
	}
	public boolean isFav(Link l) {
		return fav.isFav(l);
	}
	public void pushLinks(Link l) {
		int len = mPrefs.getInt(LINKSTACKLEN, 0);
		if (len < 0)
			len = 0;
		// Log.d("Links","Pushing: " + l.getUrl().toExternalForm() + " to " +
		// LINKSTACK+len);
		SharedPreferences.Editor ed = mPrefs.edit();
		byte[] lText = base64Encode(Stuff.objectToString(l));
		ed.putString(LINKSTACK + len, new String(lText));
		len++;
		ed.putInt(LINKSTACKLEN, len);
		ed.commit();
	}

	public Link peekLinks() {
		int len = mPrefs.getInt(LINKSTACKLEN, 0);
		if (len <= 0)
			return null;
		len--;
		byte[] lText = base64Decode(mPrefs.getString(LINKSTACK + len, null));
		if (lText == null) {
			return null;
		}
		Link n = (Link) Stuff.stringToObject(lText);
		return n;
	}

	public Link popLinks() {
		int len = mPrefs.getInt(LINKSTACKLEN, 0);
		if (len <= 0)
			return null;

		SharedPreferences.Editor ed = mPrefs.edit();
		len--;
		ed.putInt(LINKSTACKLEN, len);
		String tmp = mPrefs.getString(LINKSTACK + len, null);
		 //Log.d("Links","Popping: " + LINKSTACK+len + ": " + tmp);
		byte[] lText = base64Decode(tmp);
		// Log.d("Links","Popping: " + LINKSTACK+len + ": " + new
		// String(lText));
		ed.remove(LINKSTACK + len);

		ed.commit();
		if (lText == null) {
			return null;
		}
		Link n = (Link) Stuff.stringToObject(lText);
		return n;
	}

	private void saveSetting(String key, boolean data) {
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putBoolean(key, data);
		ed.commit();
	}
	private void deleteSetting(String key) {
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.remove(key);
		ed.commit();
	}
	private void saveSetting(String key, int data) {
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putInt(key, data);
		ed.commit();
	}

	@SuppressWarnings("unused")
	private void saveSetting(String key, String data) {
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putString(key, data);
		ed.commit();
	}

	private byte[] base64Encode(byte[] s) {
		if (s == null)
			return null;
		return Base64.encode(s, Base64.DEFAULT);
	}

	private byte[] base64Decode(String s) {
		return Base64.decode(s, Base64.DEFAULT);
	}

	public void setFavInstance(FavListHandler favListHandler) {
		fav = favListHandler;

	}	
	

	public int getVersion() {
		return mPrefs.getInt("VERSION", 0);
	}

	public void setVersion(int myVer) {
		saveSetting("VERSION", myVer);
	}

	public static boolean isDebugging() {
		return true;
	}
	public void setSeen(Link link,boolean seen) {
		if (link == null || link.getType() != Patterns.PLAY )
			return;
		if (seen == false)
			deleteSetting("SEEN_" + link.getUrl().toExternalForm());
		else
			saveSetting("SEEN_" + link.getUrl().toExternalForm(), true);	
	}
	public boolean isSeen(Link link) {
		if (link == null)
			return false;
		return mPrefs.contains("SEEN_" + link.getUrl().toExternalForm());
	}

	
}
