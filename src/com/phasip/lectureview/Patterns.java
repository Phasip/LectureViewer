package com.phasip.lectureview;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;


public enum Patterns {

	SEARCH(LectureViewer.MENU_SEARCH,"<h4><a href=\"/(lectures|courses)(/.*?)\">(.*?)</a></h4>\\s*<span class=\"org\"><a href=[\"'].*?[\"']>(.*?)</a> / <a href=[\"'].*?[\"']>(.*?)</a></span><br />\\s*<span class=\"author\">\\s*<a href=\".*?\">\\s*(.*?)\\s*</a>\\s*</span><br />") {
		public void parseMatch(Link result, Matcher m) {
			Log.d(LectureViewer.APP_NAME,"PATTERNS - SEARCH ParseMatch()");
			if (m.group(1).equals("lectures")) //I know it seems backwards, but it's the way it is.
				result.setType(PLAY);
			else
				result.setType(LECTURES);
			
			result.setName(m.group(3));
			result.setUrl(LectureViewer.MAIN_URL + "/" + m.group(1) +  m.group(2));
			String desc = m.group(4) + " / " + m.group(5) + " - " + m.group(6);
			result.setDesc(desc);
		}
	},
	
	COURSES(LectureViewer.MENU_COURSES,"<h3><a href=\"(/courses/.*?)\">(.*?)</a></h3>\\s*<h4><a href=\".*?\">(.*?)</a> / <a href=\".*?\">(.*?)</a></h4>\\s*(<h5 class=\"speakers-long\">\\s*<a href=\".*?\">\\s*.*?\\s*</a><br/>\\s*</h5>)?") {
		public void parseMatch(Link result, Matcher m) {
			result.setName(m.group(2));
			result.setType(type+1);
			result.setUrl(LectureViewer.MAIN_URL + m.group(1));
			String desc = m.group(3) + " / " + m.group(4);
			if (m.groupCount() == 5 && m.group(5) != null) {
				String n = m.group(5);
				// Log.d(APP_NAME,n);
				Pattern p = Pattern
						.compile("<h5 class=\"speakers-long\">\\s*<a href=\".*?\">\\s*(.*?)\\s*</a><br/>\\s*</h5>");
				Matcher nm = p.matcher(n);
				if (nm.find())
					desc += " - " + nm.group(1);
			}
			Log.d(LectureViewer.APP_NAME,LectureViewer.APP_NAME + " Listing url: " + result.getUrl() + " id: " + result.getIntType());
			result.setDesc(desc);
		}
	},
	RETRY(LectureViewer.MENU_RETRY,""),
	PLAY(LectureViewer.MENU_PLAY,""),
	SUBJECTS(LectureViewer.MENU_SUBJECTS,"<li><a href=\"(/subjects/.+?)\">(.*?)</a></li>"), 
	TOPICS(LectureViewer.MENU_TOPICS,"<li><a href=\"(/subjects/.*?)\" class='o?n? ?clearfix'>(.*?)</a></li>"), 
	LECTURES(LectureViewer.MENU_LECTURES,"<h4><a href=\"(/lectures/.*?)\">(.*?)</a></h4>"), 
	PAGES(LectureViewer.MENU_PAGES,"<li><span><a href=\"(/subjects/view/.*?/../../../subjects/.*?/page:[0-9]+/category:.*?)\">([0-9]+)</a></span>");

	public void parseMatch(Link result, Matcher m) {
		result.setName(m.group(2));
		//result.setType(this);
		result.setUrl(LectureViewer.MAIN_URL + m.group(1));
		if (type < 7)
			result.setType(type+1);
		
	}

	String pattern;
	int type;
	public String getPattern() {
		return pattern;
	}
	public int getType()
	{
		return type;
	}
	Patterns(int type,String s) {
		pattern = s;
		this.type = type;
	}
	
	public static Patterns fromInt(int a) {
		switch (a) {
		case LectureViewer.MENU_SUBJECTS:
			return SUBJECTS;
		case LectureViewer.MENU_TOPICS:
			return TOPICS;
		case LectureViewer.MENU_COURSES:
			return COURSES;
		case LectureViewer.MENU_LECTURES:
			return LECTURES;
		case LectureViewer.MENU_SEARCH:
			return SEARCH;
		case LectureViewer.MENU_PLAY:
			return PLAY;
		case LectureViewer.MENU_RETRY:
			return RETRY;
		case LectureViewer.MENU_PAGES:
			return PAGES;
		}
		throw new RuntimeException("Cannot convert from int: " + a);
	}
	
	
}

