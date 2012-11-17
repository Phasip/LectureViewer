package com.phasip.lectureview.listhandlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.phasip.lectureview.LectureViewer;
import com.phasip.lectureview.Link;
import com.phasip.lectureview.R;
import com.phasip.lectureview.Stuff;
import com.phasip.lectureview.background.DocumentDownloader;
import com.phasip.lectureview.background.Settings;
import com.phasip.lectureview.background.ToastException;
import com.phasip.lectureview.list.LectureListView;

public class PlayListHandler implements ListHandler {
	private static final String APP_NAME = "PlayListHandler";
	LectureViewer p;
	LectureListView list;

	public PlayListHandler(LectureListView list, LectureViewer owner, Link link) {
		p = owner;
		this.list = list;
		launchContentLoader(link);
	}

	@Override
	public void launchPrev() {
		// Not Implemented
	}

	private ProgressDialog proccessDialog;

	public void launchContentLoader(Link link) {
		proccessDialog = ProgressDialog.show(p, "Working...", "Loading Lecture");
		new DownloadThread().execute(link);
	}

	private static String generateString(File f) throws IOException {
		Reader stream = new InputStreamReader(new FileInputStream(f));
		BufferedReader buffer = new BufferedReader(stream);

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

	private class DownloadThread extends AsyncTask<Link, Integer, Integer> {
		Link l = null;
		String url;
		String error;

		protected Integer doInBackground(Link... link) {
			l = link[0];
			DocumentDownloader d = DocumentDownloader.getInstance(p);

			try {
				File f = d.loadDocumentFile(l.getUrl());

				Log.d(APP_NAME, "Loading url" + l.getUrl().toExternalForm());
				String data = generateString(f);
				Pattern patt = Pattern.compile("youtube.com/v/([^'\"]+)['\"]");
				// "http://www.youtube.com/v/eg6WnD86IAk?autoplay=1"
				Matcher matcher = patt.matcher(data);
				if (!matcher.find()) {
					return -1;
				}
				url = matcher.group(1);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error = "Parsing Problems, report bug #77 to: phasip@gmail.com";
				return -2;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -3;
			} catch (IllegalStateException e) {
				e.printStackTrace();
				error = "Parsing Problems, report bug #74 to: phasip@gmail.com";
				return -4;
			} catch (ToastException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error = e.getMessage();
				return -5;
			}
			return 0;
		}

		protected void onPostExecute(Integer err) {
			if (proccessDialog != null) {
				try {
					proccessDialog.hide();
					proccessDialog.dismiss();
				} catch (IllegalArgumentException e) {
					Log.d(APP_NAME, "Roatation caused dialog error... ignoring.");
				}
			}
			if (url == null || err != 0) {
				switch (err) {
				case -4:
					Stuff.shortToast(p, error);
				case -3:
					Stuff.shortToast(p, R.string.connerror);
				default:
					if (error != null)
						Stuff.shortToast(p, error);
					else
						Stuff.shortToast(p, "Parsing Problems, report bug #79 to: phasip@gmail.com");
				}
				DocumentDownloader d = DocumentDownloader.getInstance(p);
				d.invalidate(l.getUrl());

			} else {
				if (launchPlayer(url)) {
					Settings.getInstance(p).setSeen(l, true);
					p.invalidateLists();
				}
			}
		}

	}

	public boolean launchPlayer(String url) {
		Log.d(APP_NAME, "Playing: " + url);
		url = "vnd.youtube:" + url;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(url), null);
		try {
			p.startActivity(intent);
			return true;
		} catch (ActivityNotFoundException ex) {
			Stuff.shortToast(p, "Could not launch youtube player, please try to reinstall it");
		}
		return false;
	}

}
