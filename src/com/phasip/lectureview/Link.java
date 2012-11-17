package com.phasip.lectureview;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import com.phasip.lectureview.background.Settings;
import com.phasip.lectureview.list.LectureListView;
import com.phasip.lectureview.listhandlers.ListHandler;

public class Link implements Comparable<Link>,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1069471713052847299L;
	private String name;
	private URL url;
	private String desc;
	private Patterns type;
    public Link clone()
    {
    	Link r = new Link();
    	r.setName(name);
    	r.setDesc(desc);
    	r.setType(type);
    	r.setUrl(url);
    	return r;
    }
 
    
	public void setType(Patterns t)
	{
		type = t;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Link other = (Link) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}


	public boolean hasType(Patterns t)
	{
		if (t == null)
		{
			if (type == null)
				return true;
			return false;
		}
		
		return t.equals(type);
	}

	public Patterns getType()
	{
		return type;
	}
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUrl(URL url) {
		this.url = url;
	}
	public void setUrl(URL parent,String newUrl) throws MalformedURLException {
		setUrl(new URL(parent,newUrl));
	}
	public URL getUrl() {
		return url;
	}

	public Link() {
		this(null,null,null);
	}
	
	public Link(String name, URL url)  {
		this(name,url,null);
	}

	public Link(String name, URL url,String desc)  {
		this.name = name;
		this.url = url;
		this.desc = desc;
	}

	
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(Link another) {
		URL my = getUrl();
		URL they = another.getUrl();
		if (my == null)
			return -1;
		if (they == null)
			return 1;
		String myForm = my.toExternalForm();
		if (myForm == null)
			return -1;
		String theyForm = they.toExternalForm();
		if (theyForm == null)
			return 1;
		return myForm.compareTo(theyForm);
	}


	public ListHandler launchHandler(LectureListView view, LectureViewer activity) {
		if (getType() == null)
			return Patterns.SUBJECTS.launchHandler(view, activity, this);
		else
			return getType().launchHandler(view, activity, this);
	}


	public boolean isSeen() {
		if (this.type != Patterns.PLAY)
			return false;
		
		Settings s = Settings.getInstance(null);
		if (s == null)
			return false;
		return s.isSeen(this);
	}

}