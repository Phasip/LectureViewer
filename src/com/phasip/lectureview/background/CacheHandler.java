package com.phasip.lectureview.background;

import java.io.File;
import java.util.Collection;

import android.app.Activity;

import com.phasip.lectureview.Link;
import com.phasip.lectureview.Stuff;

public class CacheHandler {
	private static final String LCACHE = "lcache";
	Activity a;
	static CacheHandler h;

	private CacheHandler(Activity a) {

		this.a = a;
	}

	public static CacheHandler getInstance(Activity a) {
		if (h == null)
			h = new CacheHandler(a);
		return h;
	}

	public boolean has(Link link) {
		if (link == null || link.getUrl() == null) {
			return false;
		}
		File f = Stuff.URLtoFile(a, link.getUrl(), LCACHE, link.getName(), 2 * 24 * 60 * 60);
		return f != null;

	}

	public Collection<? extends Link> get(Link link, long timelimit) {
		File f;
		if (link == null) {
			return null;
		}

		f = Stuff.URLtoFile(a, link.getUrl(), LCACHE, link.getName(), timelimit);
		if (f == null) {
			return null;
		}
		Collection<Link> x = fromFile(f);

		return x;
	}

	public Collection<? extends Link> get(Link link) {
		return get(link, 2 * 24 * 60 * 60 + 5); //Has/Get combo s
	}

	public boolean set(Link link, Collection<? extends Link> list) {
		if (link == null || link.getUrl() == null) {

			return false;
		}
		File f;
		f = Stuff.URLtoNewFile(a, link.getUrl(), LCACHE, link.getName());

		if (f == null) {

			return false;
		}
		if (f.exists())
			f.delete();

		boolean ret = Stuff.objectToFile(list, f);
		return ret;
	}

	@SuppressWarnings("unchecked")
	private Collection<Link> fromFile(File file) {
		return (Collection<Link>) Stuff.fileToObject(file);

	}

	public void invalidate(Link link) {
		File f;
		if (link == null) {
			return;
		}

		f = Stuff.URLtoFile(a, link.getUrl(), LCACHE, link.getName(), Long.MAX_VALUE);
		if (f == null) {
			return;
		}
		f.delete();
		
		
	}

}
