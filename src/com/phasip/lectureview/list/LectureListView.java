package com.phasip.lectureview.list;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.phasip.lectureview.LectureViewer;
import com.phasip.lectureview.Link;
import com.phasip.lectureview.R;
import com.phasip.lectureview.listhandlers.ContextHandler;

public class LectureListView extends ListView implements ViewHandler<Link> {

	private ArrayList<Link> links = new ArrayList<Link>();
	private Link firstLink = new Link();
	private SpecArrayAdapter<Link> arrayAdapter;
	private ContextHandler contHand;
	public Link getFirst() {
		return firstLink;
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
	public void setContextHandler(LectureViewer a) {
		contHand = new ContextHandler(this, a);
	}
	@Override
	public void handle(View v, Link object) {
		TextView t = (TextView) v.findViewById(R.id.maintext_sa);
		t.setText(object.getName());
		TextView t2 = (TextView) v.findViewById(R.id.desctext_sa);
		if (object.isSeen()) {
			t.setTextColor(Color.GREEN);
			t2.setTextColor(Color.GREEN);
		} else {
			t.setTextColor(Color.WHITE);
			t2.setTextColor(Color.LTGRAY);
		}
		if (object.getDesc() == null)
			t2.setText("");
		else
			t2.setText(object.getDesc());
	}
	
}
