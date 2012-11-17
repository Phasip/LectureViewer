package com.phasip.lectureview.background;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.util.Log;

import com.phasip.lectureview.Stuff;

public class DocumentDownloader {
	private Activity a;
	private static DocumentDownloader d;

	private DocumentDownloader(Activity a) {
		this.a = a;
	}

	public static DocumentDownloader getInstance(Activity a) {
		if (d == null)
			d = new DocumentDownloader(a);
		return d;
	}

	public Document loadDocument(URL url) throws ToastException {
		if (url == null)
			throw new ToastException("We fail to parse the url, write a bug report to phasip@gmail.com");
		
		File in = loadDocumentFile(url);
		
		return FileToDocument(in, url);
	}

	public File loadDocumentFile(URL url) throws ToastException {
		if (url == null)
			return null;
		File in = Stuff.URLtoNewFile(a, url, "dcache");
		int result = 0;
		if (!in.exists() || !in.isFile()) {
			result = WebDownloader.downloadToFile(url, in);
		} else {
			long age = Stuff.getTime() - in.lastModified();
			if (age > 60 * 60 * 24 * 2)
				result = WebDownloader.downloadToFile(url, in);
		}
		if (result == -4 || result == -1)
			throw new ToastException("Could not connect to server, check internet connection");
		if (result != 0)
			throw new ToastException("Unknown error, please retry");
		return in;
	}

	public void invalidate(URL url) {
		if (url == null)
			return;
		File f = Stuff.URLtoNewFile(a, url, "dcache");
		f.delete();

	}

	public static void logNode(Node node, String msg) {
		if (!Settings.isDebugging())
			return;
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(node), new StreamResult(buffer));
			String str = buffer.toString();
			int i = 0;
			for (i = 0; i < str.length() - 2001; i += 2000) {
				Log.d(msg, msg + ": " + str.substring(i, i + 2000));
			}
			Log.d(msg, msg + ": " + str.substring(i));
			return;
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("MOO", msg + ": LOGFAILS");
	}

	private static Document FileToDocument(final File doc, final URL url) {
		try {
			Parser p = new Parser();
			p.setFeature(Parser.namespacesFeature, false);
			p.setFeature(Parser.namespacePrefixesFeature, false);
			p.setFeature(Parser.CDATAElementsFeature, false);
			p.setFeature(Parser.ignorableWhitespaceFeature, false);
			p.setFeature(Parser.ignoreBogonsFeature, true);
			p.setFeature(Parser.restartElementsFeature, true);

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document ret = db.newDocument();
			ret.setDocumentURI(url.toExternalForm());
			DOMResult result = new DOMResult(ret);
			InputSource x = new InputSource(new FileInputStream(doc));
			transformer.transform(new SAXSource(p, x), result);
			try {

				XPathFactory xpathFactory = XPathFactory.newInstance();
				// XPath to find empty text nodes.
				XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");
				NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(ret, XPathConstants.NODESET);

				// Remove each empty text node from document.
				for (int i = 0; i < emptyTextNodes.getLength(); i++) {
					Node emptyTextNode = emptyTextNodes.item(i);
					emptyTextNode.getParentNode().removeChild(emptyTextNode);
				}
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return ret;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doc.delete();
		return null;
	}

	private static class WebDownloader {
		public static int downloadToFile(URL url, File out) {
			InputStream inputStream;
			try {
				inputStream = getUrlStream(url.toURI());

				if (inputStream == null)
					return -1;
				FileOutputStream output = new FileOutputStream(out);
				int bufferSize = 1024;
				byte[] buffer = new byte[bufferSize];
				int len = 0;
				while ((len = inputStream.read(buffer)) != -1) {
					output.write(buffer, 0, len);
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -2;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -3;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -4;
			}
			return 0;
		}

		private static InputStream getUrlStream(URI uri) {
			try {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet method = new HttpGet(uri);
				HttpResponse res = client.execute(method);
				return res.getEntity().getContent();
			} catch (ClientProtocolException e) {
				return null;
			} catch (IOException e) {
				return null;
			}
		}
	}
}
