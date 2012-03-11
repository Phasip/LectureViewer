package com.phasip.lectureview;

import java.util.regex.Matcher;

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
	COURSES(LectureViewer.MENU_COURSES,"<a class=\".*?\" href=\"(/courses/.*?)\">\\s*(.*?)\\s*</a>\\s*<div class=\".*?\">\\s*<a class=\".*?\" href=\"/universities/.*?\">\\s*(.*?)\\s*</a>\\s*\\|?\\s*<a class=\".*?\" href=\"/subjects/.*?\">\\s*(.*?)\\s*</a>\\s*</div>") {
		public void parseMatch(Link result, Matcher m) {
			result.setName(m.group(2));
			result.setType(type+1);
			result.setUrl(LectureViewer.MAIN_URL + m.group(1));
			String desc = m.group(3) + " |	 " + m.group(4);
			//Log.d(LectureViewer.APP_NAME,LectureViewer.APP_NAME + " Listing url: " + result.getUrl() + " id: " + result.getIntType());
			result.setDesc(desc);
		}
	},
	RETRY(LectureViewer.MENU_RETRY,""),
	PLAY(LectureViewer.MENU_PLAY,""),
	SUBJECTS(LectureViewer.MENU_SUBJECTS,"<a href=\"(/subjects/.+?)\"\\s+class=\"subj-links\"[^>]*>\\s*<div\\sclass=\"subj-box\"[^>]*>\\s*(.*?)\\s*</div>\\s*</a>"),
	TOPICS(LectureViewer.MENU_TOPICS,"<li><a href=\"(/subjects/.*?)\" class=\"tab-details-link tab-details-on\">(.*?)</a></li>"), 
	LECTURES(LectureViewer.MENU_LECTURES,"<h4><a href=\"(/lectures/.*?)\">(.*?)</a></h4>"), 	
	PAGES(LectureViewer.MENU_PAGES,"<span><a href=\"(/subjects/view/../../../subjects/.*?/page:[0-9]+/category:.*?)\">([0-9]+)</a></span>");
	private static String notTag = "[^<]";
	private static String noQuote = "[^\"]";
	
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

