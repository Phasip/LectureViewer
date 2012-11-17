package com.phasip.lectureview.listhandlers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.phasip.lectureview.LectureViewer;
import com.phasip.lectureview.Link;
import com.phasip.lectureview.Patterns;
import com.phasip.lectureview.background.CacheHandler;
import com.phasip.lectureview.background.Settings;
import com.phasip.lectureview.list.LectureListView;

public class FavListHandler implements ListHandler, OnItemClickListener {
	private static final String CACHE_URL = "http://rthism.com/favlist";
	private static final String CACHE_NAME = "FAVLIST";
	protected static final String APP_NAME = "AbstractBrowseListHandler";
	private static Link favCache;
	LectureListView l;
	LectureListView browseList;
	public Settings settings;
	boolean doRefresh = false;
	LectureViewer p;


	public FavListHandler(LectureListView l,LectureListView b, LectureViewer p) {
		browseList = b;
		try {
			favCache = new Link(CACHE_NAME, new URL(CACHE_URL));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		this.l = l;
		this.p = p;
		settings = Settings.getInstance(p);
		l.setContextHandler(p);
		l.setOnItemClickListener(this);
		settings.setFavInstance(this);
		loadFav();
	}

	@Override
	public void launchPrev() {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Link loadUrl = (Link) parent.getItemAtPosition(position);
		if (loadUrl.getType() != Patterns.PLAY)
			p.setBrowseTab();

		loadUrl.launchHandler(browseList, p);

	}


	private void loadFav() {
		CacheHandler ch = CacheHandler.getInstance(p);
		l.getList().clear();
		Collection<? extends Link> list = ch.get(favCache, Long.MAX_VALUE);
		if (list != null)
			l.getList().addAll(list);
		l.invalidate();
		l.notifyChange();
	}

	public void addFav(Link link) {
		CacheHandler ch = CacheHandler.getInstance(p);
		if (l.getList().contains(link))
			return;
		
		l.add(link);
		ch.set(favCache, l.getList());
		l.notifyChange();

	}

	public void deleteFav(Link link) {
		CacheHandler ch = CacheHandler.getInstance(p);
		l.getList().remove(link);
		ch.set(favCache, l.getList());
		l.notifyChange();
	}

	public boolean isFav(Link l2) {
		return l.getList().contains(l2);
		
	}

}
