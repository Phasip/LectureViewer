package com.phasip.lectureview;

import java.io.Serializable;
import java.net.URISyntaxException;
/**
 * This is an old version that should not be used... Hate it!
 */
class Link implements Serializable {
	private static final long serialVersionUID = -1069471713052847298L;
	private String name;
	private String url;
	private String desc;
	private Patterns type;
	public NewLink getLink() throws URISyntaxException {
		NewLink l = new NewLink(name,NewLink.fixUrl(url),desc);
		l.setType(type);
		return l;
	}
	
 }