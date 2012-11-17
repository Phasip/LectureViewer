package com.phasip.lectureview.listhandlers;

import java.net.MalformedURLException;
import java.net.URL;
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

import android.util.Log;

import com.phasip.lectureview.LectureViewer;
import com.phasip.lectureview.Link;
import com.phasip.lectureview.Patterns;
import com.phasip.lectureview.background.DocumentDownloader;
import com.phasip.lectureview.background.ToastException;
import com.phasip.lectureview.list.LectureListView;

public class CourseListHandler extends AbstractBrowseListHandler {
	String appendLink = "/page:1/sort:title/direction:asc/show:500";

	public CourseListHandler(LectureListView l, LectureViewer p, Link link) {
		super(l, p, link);
	}

	@Override
	protected boolean loadListContent(ArrayList<Link> fill, Link l) throws ToastException {
		DocumentDownloader d = null;
		ToastException t;
		URL url = null;
		try {
			url = l.getUrl();
			d = DocumentDownloader.getInstance(getActivity());
			url = new URL(url.toExternalForm() + appendLink);
			Document doc = d.loadDocument(url);

			XPath xPath = XPathFactory.newInstance().newXPath();
			XPathExpression lectureMatcher;
			Log.d("Loading Courses", "URL:" + url.toExternalForm());
			lectureMatcher = xPath.compile("//div[@class='lecture']");
			NodeList subjectNodes = (NodeList) lectureMatcher.evaluate(doc, XPathConstants.NODESET);
			for (int j = 0; j < subjectNodes.getLength(); j++) {
				NodeList n = subjectNodes.item(j).getChildNodes();
				Link ny = getLink("Lecture",l, n);
				if (ny == null)
					continue;

				ny.setType(Patterns.PLAY);
				fill.add(ny);

			}

			XPathExpression courseMatcher;
			Log.d("URL", "URL:" + url.toExternalForm());
			courseMatcher = xPath.compile("//div[@class='course']");
			NodeList courseNodes = (NodeList) courseMatcher.evaluate(doc, XPathConstants.NODESET);
			for (int j = 0; j < courseNodes.getLength(); j++) {
				NodeList n = courseNodes.item(j).getChildNodes();
				Link ny = getLink("Course",l, n);
				if (ny == null)
					continue;

				ny.setType(Patterns.LECTURES);
				fill.add(ny);
			}
			return true;
		} catch (XPathExpressionException e) {
			t = new ToastException("Parsing Problems, report bug #27 to: phasip@gmail.com");
			e.printStackTrace();
		} catch (DOMException e) {
			t = new ToastException("Parsing Problems, report bug #24 to: phasip@gmail.com");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			t = new ToastException("Parsing Problems, report bug #28 to: phasip@gmail.com");
		}
		d.invalidate(url);
		throw t;
	}

	private Link getLink(String typ, Link old, NodeList n) throws MalformedURLException, DOMException {
		Link ny = new Link();
		if (n == null || n.getLength() < 2)
			return null;
		Node lNode = null;
		Node tNode = null;
		Node pNode = null;
		for (int i = 0; i < n.getLength(); i++) {
			Node t = n.item(i);
			DocumentDownloader.logNode(t, "Courses, " + typ + ", " + i);
			if ("h3".equalsIgnoreCase(t.getNodeName())) {
				tNode = t;
			} else if ("a".equalsIgnoreCase(t.getNodeName())) {
				lNode = t;
			} else if ("p".equalsIgnoreCase(t.getNodeName())) {
				pNode = t;
			}
			if (tNode != null && lNode != null && pNode != null)
				break;
		}
		String tch = "Unknown";
		String sch = "Unknown";
		if (pNode != null && pNode.hasChildNodes()) {
			NodeList p = pNode.getChildNodes();
			int i = 0;
			Node school = null;
			Node teacher = null;
			for (; i < p.getLength(); i++) {
				Node t = p.item(i);
				if ("a".equalsIgnoreCase(t.getNodeName())) {
					school = t;
					break;
				}
			}
			i++;
			for (; i < p.getLength(); i++) {
				Node t = p.item(i);
				if ("a".equalsIgnoreCase(t.getNodeName())) {
					teacher = t;
					break;
				}
			}
			tch = teacher == null ? "Unknown" : teacher.getTextContent();
			sch = school == null ? "Unknown" : school.getTextContent();
			
		}
		ny.setDesc(typ + " | " + sch + " | " + tch);
		if (lNode != null && lNode.getAttributes() != null && lNode.getAttributes().getNamedItem("href") != null)
			ny.setUrl(old.getUrl(), lNode.getAttributes().getNamedItem("href").getNodeValue());
		if (tNode != null)
			ny.setName(tNode.getTextContent());
		return ny;
	}

	@Override
	public String getLoadMessage() {
		return "Loading Courses";
	}

}
