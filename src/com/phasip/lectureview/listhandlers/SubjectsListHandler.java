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

public class SubjectsListHandler extends AbstractBrowseListHandler {
	public final static URL SUBJECTS_URL;
	static {
		URL tmp = null;
		try {
			tmp = new URL("http://www.academicearth.org/subjects/");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		
		SUBJECTS_URL = tmp;
	}
	public static String TYPE = "SUBJECTS";

	public SubjectsListHandler(LectureListView l, LectureViewer p) {
		super(l, p, new Link("Subjects",SUBJECTS_URL));
	}

	@Override
	protected boolean loadListContent(ArrayList<Link> fill, Link l) throws ToastException {
		DocumentDownloader d = DocumentDownloader.getInstance(getActivity());
		ToastException tx;
		Document doc = d.loadDocument(l.getUrl());
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			XPathExpression subjectMatcher;
			subjectMatcher = xPath.compile("//div[@class='subjectsIndex']/div");
			NodeList subjectNodes = (NodeList) subjectMatcher.evaluate(doc, XPathConstants.NODESET);
			for (int j = 0; j < subjectNodes.getLength(); j++) {
				Node n = subjectNodes.item(j);
				DocumentDownloader.logNode(n, "NODEX ");
				if (!n.hasChildNodes())
					continue;
				NodeList childs = n.getChildNodes();
				Node lNode =  null;
				Node tNode = null;
				for (int i = 0; i < childs.getLength(); i++) {
					DocumentDownloader.logNode(childs.item(i), "NODE " + i);

					Node t = childs.item(i);
					Log.d("SubjectsList", "Noo" + t.getNodeName());
					Log.d("SubjectsList", "Moo" + t.getLocalName());
					if ("h3".equalsIgnoreCase(t.getNodeName())) {
						tNode = t;
					} else if ("a".equalsIgnoreCase(t.getNodeName())) {
						lNode = t;
					}
					if (tNode != null && lNode != null)
						break;
				}
				
				Link link = new Link();
				link.setType(Patterns.TOPICS);
				if (tNode == null || lNode == null || lNode.getAttributes() == null || lNode.getAttributes().getNamedItem("href") == null)
					continue;

				link.setName(tNode.getTextContent());
				link.setUrl(SUBJECTS_URL, lNode.getAttributes().getNamedItem("href").getNodeValue());
				fill.add(link);
			}
			return true;
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			tx = new ToastException("Parsing Problems, report bug #34 to: phasip@gmail.com");
		} catch (DOMException e) {
			e.printStackTrace();
			tx = new ToastException("Parsing Problems, report bug #36 to: phasip@gmail.com");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			tx = new ToastException("Parsing Problems, report bug #38 to: phasip@gmail.com");
		}
		d.invalidate(SUBJECTS_URL);
		throw tx;
	}

	@Override
	public String getLoadMessage() {
		return "Loading Subjects";
	}
}
