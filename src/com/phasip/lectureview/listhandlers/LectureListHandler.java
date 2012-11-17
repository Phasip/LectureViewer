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

import com.phasip.lectureview.LectureViewer;
import com.phasip.lectureview.Link;
import com.phasip.lectureview.Patterns;
import com.phasip.lectureview.background.DocumentDownloader;
import com.phasip.lectureview.background.ToastException;
import com.phasip.lectureview.list.LectureListView;

public class LectureListHandler extends AbstractBrowseListHandler {
	String appendLink = "/page:1/show:500";

	public LectureListHandler(LectureListView l, LectureViewer p, Link link) {
		super(l, p, link);
	}

	@Override
	protected boolean loadListContent(ArrayList<Link> fill, Link l) throws ToastException {
		DocumentDownloader d = DocumentDownloader.getInstance(getActivity());

		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression subjectMatcher;
		URL url;
		try {
			url = l.getUrl();
			url = new URL(url.toExternalForm() + appendLink);
			Document doc = d.loadDocument(url);

			subjectMatcher = xPath.compile("//div[@class='items']/div[@class='lecture']");
			NodeList subjectNodes = (NodeList) subjectMatcher.evaluate(doc, XPathConstants.NODESET);
			for (int j = 0; j < subjectNodes.getLength(); j++) {
				NodeList n = subjectNodes.item(j).getChildNodes();
				Link ny = getLink("Lecture",l,n);
				if (ny == null)
					continue;
				ny.setType(Patterns.PLAY);
				fill.add(ny);
			}

			return true;
		} catch (XPathExpressionException e) {
			d.invalidate(SubjectsListHandler.SUBJECTS_URL);
			throw new ToastException("Parsing Problems, try again or report error 3 to : phasip@gmail.com");
		} catch (DOMException e) {
			d.invalidate(SubjectsListHandler.SUBJECTS_URL);
			throw new ToastException("Parsing Problems, try again or report error 2 to: phasip@gmail.com");
		} catch (MalformedURLException e) {
			d.invalidate(SubjectsListHandler.SUBJECTS_URL);
			throw new ToastException("Parsing Problems, try again or report error 4 to: phasip@gmail.com");
		}
		
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
		String tch = pNode == null ? "Unknown" : pNode.getTextContent();
		
		ny.setDesc(typ + " | " + tch);
		if (lNode != null && lNode.getAttributes() != null && lNode.getAttributes().getNamedItem("href") != null)
			ny.setUrl(old.getUrl(), lNode.getAttributes().getNamedItem("href").getNodeValue());
		if (tNode != null)
			ny.setName(tNode.getTextContent());
		return ny;
	}
	@Override
	public String getLoadMessage() {
		return "Loading Lectures";
	}

}
