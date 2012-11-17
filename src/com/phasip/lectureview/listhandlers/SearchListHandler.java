package com.phasip.lectureview.listhandlers;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.phasip.lectureview.LectureViewer;
import com.phasip.lectureview.Link;
import com.phasip.lectureview.Patterns;
import com.phasip.lectureview.Stuff;
import com.phasip.lectureview.background.DocumentDownloader;
import com.phasip.lectureview.background.Settings;
import com.phasip.lectureview.background.ToastException;
import com.phasip.lectureview.list.LectureListView;

public class SearchListHandler implements ListHandler, OnItemClickListener, OnClickListener, OnEditorActionListener {
	protected static final String APP_NAME = "SearchListHandler";
	LectureListView l;
	public Settings settings;
	boolean doRefresh = false;
	private EditText findText;
	LectureViewer p;
	Link myLink;

	public SearchListHandler(LectureListView l, Button b, EditText e, LectureViewer p) {
		this.l = l;
		this.p = p;
		settings = Settings.getInstance(p);
		l.setContextHandler(p);
		b.setOnClickListener(this);
		findText = e;
		findText.setOnEditorActionListener(this);
		l.setOnItemClickListener(this);
	}

	@Override
	public void launchPrev() {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Link loadUrl = (Link) parent.getItemAtPosition(position);
		if (loadUrl.getType() != Patterns.PLAY)
			p.setBrowseTab();

		loadUrl.launchHandler(l, p);

	}

	@Override
	// Buttonclick
	public void onClick(View v) {
		search();

	}

	@Override
	// Editstuff
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		search();
		return true;
	}

	private void search() {
		InputMethodManager imm = (InputMethodManager) p.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(findText.getWindowToken(), 0);
		String searchText = findText.getText().toString();

		try {
			String searchString = URLEncoder.encode(searchText, "utf-8");
			URL searchUrl = new URL("http://www.academicearth.org/lectures/search/" + searchString + "/page:1/show:100");
			Link n = new Link("Search", searchUrl);
			launchContentLoader(n);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	public void launchContentLoader(Link link) {
		proccessDialog = ProgressDialog.show(p, "Working...", "Searching");
		new DownloadThread().execute(link);
	}

	private ProgressDialog proccessDialog;

	private class DownloadThread extends AsyncTask<Link, String, String> {
		Link link = null;
		ArrayList<Link> fill;

		protected String doInBackground(Link... lk) {
			link = lk[0];
			DocumentDownloader d = DocumentDownloader.getInstance(p);
			fill = new ArrayList<Link>();
			URL url = link.getUrl();
			try {
				Document doc = d.loadDocument(url);
				XPath xPath = XPathFactory.newInstance().newXPath();
				XPathExpression lectureMatcher;
				DocumentDownloader.logNode(doc, "SEARCH");
				lectureMatcher = xPath.compile("//div[@class='description description-search-title']");
				NodeList subjectNodes = (NodeList) lectureMatcher.evaluate(doc, XPathConstants.NODESET);
				for (int j = 0; j < subjectNodes.getLength(); j++) {
					NodeList n = subjectNodes.item(j).getChildNodes();
					Link ny = new Link();
					for (int i = 0; i < n.getLength(); i++) {
						DocumentDownloader.logNode(n.item(i), "NODE " + i);
					}
					if (n != null && n.getLength() < 5)
						continue;

					Node name = n.item(0);
					Node lNode = name.getFirstChild();
					if (lNode == null)
						continue;
					Node school = n.item(1);
					Node teacher = n.item(3);

					String tch = teacher == null ? "Unknown" : teacher.getTextContent();
					String sch = school == null ? "Unknown" : school.getTextContent();
					String urlStr = "";

					if (lNode != null && lNode.getAttributes() != null && lNode.getAttributes().getNamedItem("href") != null)
						urlStr = lNode.getAttributes().getNamedItem("href").getNodeValue();
					ny.setUrl(link.getUrl(), urlStr);
					if (name != null)
						ny.setName(name.getTextContent());
					if (urlStr.startsWith("/courses")) {
						ny.setType(Patterns.LECTURES);
						ny.setDesc("Course | " + sch + " | " + tch);
					} else if (urlStr.startsWith("/lecture")) {
						ny.setType(Patterns.PLAY);
						ny.setDesc("Lecture | " + sch + " | " + tch);
					}

					fill.add(ny);
					DocumentDownloader.logNode(lNode, "lNode");
					DocumentDownloader.logNode(name, "name");

				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "MalformedURL";
			} catch (DOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "DOMException";
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "XpathExpressException";
			} catch (ToastException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return e.getMessage();
			} finally {
			}

			return null;
		}

		protected void onPostExecute(String url) {
			if (proccessDialog != null) {
				try {
					proccessDialog.hide();
					proccessDialog.dismiss();
				} catch (IllegalArgumentException e) {
					Log.d(APP_NAME, "Roatation caused dialog error... ignoring.");
				}
			}
			if (url != null) {
				Stuff.shortToast(p, "We fail to search");
				DocumentDownloader d = DocumentDownloader.getInstance(p);
				d.invalidate(link.getUrl());
				return;
			}
			l.getList().clear();
			l.getList().addAll(fill);
			l.notifyChange();
		}

	}


}
