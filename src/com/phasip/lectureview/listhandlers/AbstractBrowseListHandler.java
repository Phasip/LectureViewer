package com.phasip.lectureview.listhandlers;

import java.util.ArrayList;
import java.util.Collection;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.phasip.lectureview.LectureViewer;
import com.phasip.lectureview.Link;
import com.phasip.lectureview.Patterns;
import com.phasip.lectureview.Stuff;
import com.phasip.lectureview.background.CacheHandler;
import com.phasip.lectureview.background.DocumentDownloader;
import com.phasip.lectureview.background.Settings;
import com.phasip.lectureview.background.ToastException;
import com.phasip.lectureview.list.LectureListView;

public abstract class AbstractBrowseListHandler implements OnItemClickListener, ListHandler {
	protected static final String APP_NAME = "AbstractBrowseListHandler";
	
	LectureListView l;
	public Settings settings;
	boolean doRefresh = false;
	LectureViewer p;
	Link myLink;
	
	public AbstractBrowseListHandler(LectureListView l,LectureViewer p,Link link) {
		this.l = l;
		myLink = link;
		this.p = p;
		settings = Settings.getInstance(p);
		settings.pushLinks(link);
		l.setContextHandler(p);
		l.setOnItemClickListener(this);
		launchContentLoader(link);
	}

	protected LectureListView getView() {
		return l;
	}
	protected LectureViewer getActivity() {
		return p;
	}
	/**
	 * Has to be called after loadListContent is done.
	 * @param success
	 * @param link 
	 */
	public void listContentLoaded(boolean success, Link link) {
		if (!success) {
			CacheHandler ch = CacheHandler.getInstance(getActivity());
			ch.invalidate(link);
			DocumentDownloader d = DocumentDownloader.getInstance(getActivity());
			d.invalidate(link.getUrl());
		}
		l.notifyChange();
		l.invalidate();
	}
	private void loadrefreshButton(String message) {
		doRefresh = true;
		l.clear();
		Link retryLink = new Link(message + ", Press here to retry", null);
		retryLink.setType(Patterns.RETRY); //Why not
		l.add(retryLink);
	}


	
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (doRefresh) {
			launchContentLoader(myLink);
			return;
		}
		final Link loadUrl = (Link) parent.getItemAtPosition(position);
		launchNext(loadUrl);
	}
	/**
	 * Should call listContentLoaded
	 * @throws ToastException 
	 */
	abstract protected boolean loadListContent(ArrayList<Link> fill,Link l) throws ToastException;
	public void launchNext(Link l) {
		l.launchHandler(getView(), getActivity());
	}
	@Override
	public void launchPrev() {
		settings.popLinks();
		Link l = settings.popLinks();
		
		if (l == null)
		{
			getActivity().finish();
			return;
		}
		l.launchHandler(getView(), getActivity());
	}
	abstract public String getLoadMessage();
	private ProgressDialog proccessDialog;
	public void launchContentLoader(Link link) {
		if (link == null || link.getUrl() == null) {
			throw new RuntimeException("We should not have null urls here!");
		}
		proccessDialog = ProgressDialog.show(p, "Working...", getLoadMessage());
		new DownloadThread().execute(link);
	}
	
	private class DownloadThread extends AsyncTask<Link, String, String> {
		Link link = null;
		ArrayList<Link> list = new ArrayList<Link>();
		protected String doInBackground(Link... link) {
			this.link = link[0];
			CacheHandler ch = CacheHandler.getInstance(getActivity());
			if (ch.has(this.link)) {
				Collection<? extends Link> ret = ch.get(this.link);
				if (ret == null)
					return null;
				list.addAll(ret);
				return null;
			}
			
			boolean err;
			try {
				err = loadListContent(list,this.link);
			} catch (ToastException e) {
				return e.getMessage();
			}
			ch.set(this.link, list);
			return null;
		}

		protected void onPostExecute(String error) {
			if (proccessDialog != null) {
				try {
					proccessDialog.hide();
					proccessDialog.dismiss();
				} catch (IllegalArgumentException e) {
					Log.d(APP_NAME, "Roatation caused dialog error... ignoring.");
				}
			}
			ArrayList<Link> viewList = l.getList();
			viewList.clear();
			viewList.addAll(list);

			if (error != null ) {
				Stuff.shortToast(getActivity(),error);
				loadrefreshButton("Connection fails");
				listContentLoaded(false,link);
			} else if (list.isEmpty()) {
				Stuff.shortToast(getActivity(),"Nothing found, Please retry");
				loadrefreshButton("Nothing Found");
				listContentLoaded(false,link);
			}else {
				listContentLoaded(true,link);
			}
		}
	}
}
