package com.phasip.lectureview;
import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class LectureListView extends ListView implements ViewHandler<Link> {

	private ArrayList<Link> links = new ArrayList<Link>();
	private Link firstLink = new Link();
	private SpecArrayAdapter<Link> arrayAdapter;

	/*
	 * TODO private boolean showHiddenFiles = false; public boolean showHidden()
	 * { return showHiddenFiles; } public boolean showHidden(boolean sw) {
	 * showHiddenFiles = sw; return sw; }
	 */
	public void updateLinkList() {
		updateLinkList(firstLink);
	}

	public Link getFirst() {
		return firstLink;
	}

	public void updateLinkList(Link f) {
		firstLink = f;
		links.clear();
		while (f != null) {
			links.add(f);
			f = f.getPrev();
		}
		arrayAdapter.notifyDataSetChanged();
	}
	public ArrayList<Link> getList()
	{
		return links;
	}
	public void add(Link f)
	{
		links.add(f);
	}
	public void notifyChange()
	{
		arrayAdapter.notifyDataSetChanged();
	}
	public void clear()
	{
		links.clear();
	}

	private void initMe(Context c) {
		arrayAdapter = new SpecArrayAdapter<Link>(c, R.layout.listitem, links,
				this);
		this.setAdapter(arrayAdapter);
		this.setTextFilterEnabled(true);
	}

	public LectureListView(Context context) {
		super(context);
		initMe(context);
	}

	public LectureListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMe(context);
	}

	public LectureListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMe(context);
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
