package com.phasip.lectureview.listhandlers;

import java.net.MalformedURLException;
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

public class TopicListHandler extends AbstractBrowseListHandler {
	public static String TYPE = "TOPICS";

	public TopicListHandler(LectureListView l, LectureViewer p, Link link) {
		super(l, p, link);
	}

	@Override
	protected boolean loadListContent(ArrayList<Link> fill, Link l) throws ToastException {
		
		DocumentDownloader d = DocumentDownloader.getInstance(getActivity());
		Document doc = d.loadDocument(SubjectsListHandler.SUBJECTS_URL);
		ToastException tx;
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression subjectMatcher;
		try {
			subjectMatcher = xPath.compile("//div[@class='subjectsIndex']/div");
			NodeList subjectNodes = (NodeList) subjectMatcher.evaluate(doc, XPathConstants.NODESET);
			for (int j = 0; j < subjectNodes.getLength(); j++) {
				Node n = subjectNodes.item(j);
				NodeList childs = n.getChildNodes();
				if (!n.hasChildNodes() || childs.getLength() < 3)
					continue;
				Node lNode = null;
				Node tNode = null;
				for (int i = 0; i < childs.getLength(); i++) {
					DocumentDownloader.logNode(childs.item(i), "NODE " + i);

					Node t = childs.item(i);
					if ("h3".equalsIgnoreCase(t.getNodeName())) {
						tNode = t;
					} else if ("a".equalsIgnoreCase(t.getNodeName())) {
						lNode = t;
					}
					if (tNode != null && lNode != null)
						break;
				}

				Link tmp = new Link();
				if (lNode != null && lNode.getAttributes() != null && lNode.getAttributes().getNamedItem("href") != null)
					tmp.setUrl(SubjectsListHandler.SUBJECTS_URL, lNode.getAttributes().getNamedItem("href").getNodeValue());

				if (tmp == null || l == null || tmp.getUrl() == null || !tmp.getUrl().equals(l.getUrl()))
					continue;
				XPathExpression linkMatcher = xPath.compile("div[@class='subs']//a");
				childs = (NodeList) linkMatcher.evaluate(n, XPathConstants.NODESET);
				for (int i = 0; i < childs.getLength(); i++) {
					Node t = childs.item(i);
					tmp = new Link();
					tmp.setType(Patterns.COURSES);
					tmp.setName(t.getTextContent());
					if (lNode != null && lNode.getAttributes() != null && lNode.getAttributes().getNamedItem("href") != null)
						tmp.setUrl(SubjectsListHandler.SUBJECTS_URL, lNode.getAttributes().getNamedItem("href").getNodeValue());

					fill.add(tmp);
				}
			}
			return true;
		} catch (XPathExpressionException e) {
			tx = new ToastException("Parsing Problems, report bug #54 to: phasip@gmail.com");
			e.printStackTrace();
		} catch (DOMException e) {
			tx = new ToastException("Parsing Problems, report bug #55 to: phasip@gmail.com");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			tx = new ToastException("Parsing Problems, report bug #56 to: phasip@gmail.com");
			e.printStackTrace();
		}
		d.invalidate(SubjectsListHandler.SUBJECTS_URL);
		throw tx;
	}

	@Override
	public String getLoadMessage() {
		return "Loading Topics";
	}

}
