package com.phasip.lectureview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import android.util.Log;

public class WebFetcher {
	public static final boolean disable_cache = false;
	public static HashMap<NewLink,Pair<Long,ArrayList<NewLink>>> linkdata = new HashMap<NewLink,Pair<Long,ArrayList<NewLink>>> (); 
	private static final String BANNED_TOPICS[] = {
			"art-architecture/category:66", "business/category:173",
			"business/category:161", "business/category:200",
			"computer-science/category:199", "computer-science/category:159",
			"computer-science/category:168", "economics/category:186",
			"electrical-engineering/category:191",
			"electrical-engineering/category:181", "engineering/category:182",
			"environmental-studies/category:167", "history/category:189",
			"english/category:196", "medicine/category:184",
			"philosophy/category:195", "political-science/category:187",
			"psychology/category:170" };
	private static final String BANNED_OTHER[] = { "subjects/education",
			"courses/oxford-online-winston-churchill",
			"courses/maryland-online-bachelors-english",
			"subjects/online-bachelors-degrees", "subjects/courses-for-credit","subjects/CFC",
			"subjects/online-masters-degrees",
			"subjects/teachertraining",
			"subjects/online-professional-certificates", "subjects/writing",
			"courses/the-unc-kenan-flagler-mba-degree-online"};

	/*
	 * Gets data from url, throws all exceptions, Urisyntax when bad url give,
	 * clientprotocol... not sure and io when connection error.
	 */
	public static Long getLastModified(URI uri) throws ToastException {
		try {
			/* Return value */
			/* Create a http client */
			DefaultHttpClient client = new DefaultHttpClient();
			/* Load the uri (http://... ) */
			/* Get method, use HttpPost for post (eg login) */
			HttpHead method = new HttpHead(uri);
			/* Execute the query */
			HttpResponse res = client.execute(method);
			/* make data ready to read the page */
			Header[] h = res.getHeaders("Last-Modified");
			if (h.length == 0 || h[0] == null)
				return 0L;
			Date d = DateUtils.parseDate(h[0].getValue()); 
			return d.getTime()/1000L;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new ToastException(
					"Exception... Your phone doesn't support http protocol...?");
		} catch (IOException e) {
			e.printStackTrace();
			throw new ToastException("Connection error, please retry.");
		} catch (DateParseException e) {
			return 0L;
		}
	}
	
	/*
	 * Gets data from url, throws all exceptions, Urisyntax when bad url give,
	 * clientprotocol... not sure and io when connection error.
	 */
	public static String getUrlData(URI uri) throws ToastException {
		try {
			/*url = url.replaceAll("[^a-zA-Z0-9]", "_");
			url = "http://ssh.rthism.com:64/test/" + url;
			Log.d("AAAAA","Url: " + url);*/
			/* Return value */
			String websiteData = null;
			/* Create a http client */
			DefaultHttpClient client = new DefaultHttpClient();
			/* Load the uri (http://... ) */
			//Log.d(LectureViewer.APP_NAME,"WE GOT THIS URL:" + uri.toString());
			/* Get method, use HttpPost for post (eg login) */
			HttpGet method = new HttpGet(uri);
			/* Execute the query */
			HttpResponse res = client.execute(method);
			/* make data ready to read the page */
			InputStream data = res.getEntity().getContent();
			websiteData = generateString(data);
			return websiteData;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new ToastException(
					"Exception... Your phone doesn't support http protocol...?");
		} catch (IOException e) {
			e.printStackTrace();
			throw new ToastException("Connection error, please retry.");
		}
	}

	/**
	 * Read string from InputStream
	 * 
	 * @param stream
	 *            Ready to read InputStream.
	 * @return String.
	 * @throws IOException
	 *             when read error occurs.
	 */
	public static String generateString(InputStream stream) throws IOException {
		BufferedReader buffer = new BufferedReader(
				new InputStreamReader(stream));

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

	public static void loadPattern(NewLink prev, String data,ArrayList<NewLink> result) {
		if (data == null)
			return;
		if (prev == null || prev.getUrl() == null) {
		//	Exception e = new Exception();
		//	Log.d(LectureViewer.APP_NAME,"loadPattern(3) - prevNull",e);
			return;
		}
		// If prev is null we should throw an error!
		Patterns pattern = prev.getType();
		Pattern p = Pattern.compile(pattern.getPattern(), Pattern.MULTILINE);
		Matcher m = p.matcher(data);
		String arr[] = (pattern == Patterns.TOPICS) ? BANNED_TOPICS
				: BANNED_OTHER;
		//WebLogger.upload_log("Parsing pattern: " + pattern.name() + " : " + pattern.getPattern());
		Log.d(LectureViewer.APP_NAME, "Parsing pattern: " + pattern.name() + " : " + pattern.getPattern());
		while (m.find()) {
			Log.d(LectureViewer.APP_NAME, "Found a match!");
			//WebLogger.upload_log("Found Match!");
			if (m.group(1).length() == 0)
				continue;
			NewLink l = new NewLink();
			pattern.parseMatch(prev.getUrl(),l, m);
			//WebLogger.upload_log("Match: " + l.getName() + ", " + l.getUrl());
			Log.d(LectureViewer.APP_NAME, l.getName() + ", " + l.getUrl());
			boolean add = true;
			for (String s : arr)
				if (l.getUrl().toString().endsWith(s)) {
					add = false;
					break;
				}

			if (add) {
				if ((l.getDesc() == null || l.getDesc().length() == 0))
					l.setDesc(prev.getName());

				result.add(l);
			}
		}
	}

	public static void parseCourses(NewLink parent, String data,
			ArrayList<NewLink> list) throws ToastException {
		ArrayList<NewLink> page = new ArrayList<NewLink>();
		NewLink l = parent.clone();
		l.setType(Patterns.NEXTPAGE);
		loadPattern(l, data, page);
		loadPattern(parent, data, list);
		while (!page.isEmpty()) {
			//Log.d(LectureViewer.APP_NAME,"Loading: " + page.get(0).getUrl());
			String data2 = WebFetcher.getUrlData(page.get(0).getUrl());
			//Log.d(LectureViewer.APP_NAME,"Reading patterns");
			//Log.d(LectureViewer.APP_NAME,data2);
			
			loadPattern(parent, data2, list);

			page.clear();
			loadPattern(l, data2, page);
		}

	}
	
	public static Long getTime() {
		return System.currentTimeMillis() / 1000L;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	public static void fetch(ArrayList<NewLink> currentView, NewLink currLink) throws ToastException
	{
		if (disable_cache == true) {
			fetchPage(currentView,currLink);
			return;
		}
		
		Pair<Long,ArrayList<NewLink>> linkinfo = linkdata.get(currLink);
		
		if (linkinfo == null) {
			fetchPage(currentView,currLink);
			if (!currentView.isEmpty()) {
				linkinfo = new Pair<Long,ArrayList<NewLink>>(getTime(),(ArrayList<NewLink>)currentView.clone());
				linkdata.put(currLink, linkinfo);
			}
			return;
		}
		Long time = getTime();
		Long lastFetch = linkinfo.getLeft();
		

		//If time has changed or last fetch was more than five minutes ago
		if (lastFetch > time || lastFetch+60*60*24 < time) {	
			if (getLastModified(currLink.getUrl()) < lastFetch)  {
				//Log.d(LectureViewer.APP_NAME,"Last fetch more current than last, resetting fetch time and returning old data");
				//Reset list, and reset lastFetch on data
				currentView.clear();
				currentView.addAll(linkinfo.getRight());
				linkinfo = new Pair<Long,ArrayList<NewLink>>(getTime(),(ArrayList<NewLink>)linkinfo.getRight().clone());
				linkdata.put(currLink, linkinfo);
				return;
			}
			else {
				fetchPage(currentView,currLink);
			}
			
			if (!currentView.isEmpty()) {
				linkinfo = new Pair<Long,ArrayList<NewLink>>(getTime(),(ArrayList<NewLink>)currentView.clone());
				linkdata.put(currLink, linkinfo);
			}
			return;
		}
		
		//Time hasn't changed
		currentView.clear();
		currentView.addAll(linkinfo.getRight());
		return;
	}
	public static void fetchPage(ArrayList<NewLink> currentView, NewLink currLink)
			throws ToastException {
		String data = WebFetcher.getUrlData(currLink.getUrl());
		
		//WebLogger.upload_data(currLink.getUrl(),  data.getBytes());
		switch (currLink.getType()) {
		case COURSES:
			currentView.clear();
			WebFetcher.parseCourses(currLink, data, currentView);
			Collections.sort(currentView); // This can have multiple
			break;
		case SEARCH:
		case SUBJECTS:
		case TOPICS:
		case LECTURES:
			currentView.clear();
			WebFetcher.loadPattern(currLink, data, currentView);
			break;
		default:
			throw new RuntimeException("fetch cannot be called for "
					+ currLink.getType());
		}
		return;
	}
}
