package com.phasip.lectureview.listhandlers;

import java.util.ArrayList;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;

import com.phasip.lectureview.LectureViewer;
import com.phasip.lectureview.Link;
import com.phasip.lectureview.Patterns;
import com.phasip.lectureview.Stuff;
import com.phasip.lectureview.background.Settings;
import com.phasip.lectureview.list.LectureListView;

public class ContextHandler implements OnCreateContextMenuListener, OnMenuItemClickListener {
	private static final int ID_TOGGLESEEN = 142;
	private static final int ID_TOGGLEFAV = 34;
	LectureListView l;
	LectureViewer a;
	Settings settings;

	public ContextHandler(LectureListView l, LectureViewer a) {
		this.l = l;
		this.a = a;
		settings = Settings.getInstance(a);
		l.setOnCreateContextMenuListener(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		ArrayList<Link> list = l.getList();

		String name = "ERROR";

		boolean seen = false;
		if (info.position < 0 || info.position >= list.size()) {
			return;
		}
		Link link = list.get(info.position);
		name = link.getName();
		seen = link.isSeen();
		boolean fav = settings.isFav(link);
		menu.setHeaderTitle(name);
		
		MenuItem i = menu.add(Menu.NONE, ID_TOGGLEFAV, 0, fav ? "Delete fav" : "Add to fav");
		i.setOnMenuItemClickListener(this);
		if (link.getType() == Patterns.PLAY) {
			i = menu.add(Menu.NONE, ID_TOGGLESEEN, 0, seen ? "Mark as Unseen" : "Mark as Seen");
			i.setOnMenuItemClickListener(this);
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		ArrayList<Link> list;
		Link link;
		switch (item.getItemId()) {
		case ID_TOGGLEFAV:
			list = l.getList();
			if (info.position < 0 || info.position >= list.size()) {
				Stuff.shortToast(a, "Index out of bounds error?...");
				return true;
			}
			link = list.get(info.position);
			if (!settings.isFav(link))
				settings.addFavourite(link);
			else
				settings.delFavourite(link);
			return true;
		case ID_TOGGLESEEN:
			list = l.getList();
			if (info.position < 0 || info.position >= list.size()) {
				Stuff.shortToast(a, "Index out of bounds error?...");
				return true;
			}
			link = list.get(info.position);
			settings.setSeen(link, !settings.isSeen(link));
			a.invalidateLists();
			return true;
		}
		return false;
	}

}
