package com.phasip.lectureview;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import android.util.Log;

class NewLink implements Comparable<NewLink>,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1069471713052847299L;
	private String name;
	private URI url;
	private String desc;
	private transient NewLink prev;
	private Patterns type;
    public NewLink clone()
    {
    	NewLink r = new NewLink();
    	r.setName(name);
    	r.setDesc(desc);
    	r.setPrev(prev);
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
	  /* @Override
	    public boolean equals(Object l)
	    {
	    	if (l == null ||l.getClass() != NewLink.class)
	    		return false;

	    	String hisurl = ((NewLink)l).getUrl();
	    	if (hisurl == null)
	    		return url == null;
	    	
	    	return hisurl.equals(url);
	    }*/

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NewLink other = (NewLink) obj;
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
	public NewLink getPrev()
	{
		return prev;
	}
	public void setPrev(NewLink l)
	{
		prev = l;
	}
	public void setType(int t)
	{
		type = Patterns.fromInt(t);
	}
	public Patterns getType()
	{
		return type;
	}
	public int getIntType()
	{
		if (type == null)
			return -1;
		return type.getType();
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

	public URI getUrl() {
		return url;
	}
	public void setUrl(URI url) {
		this.url = url;
	}

	public void setUrl(String url) throws URISyntaxException {
		this.url = fixUrl(url);
	}
	

	public void setUrl(URI orig,String url)  {
		
		if (orig == null) {
		//	Log.d(LectureViewer.APP_NAME,"SetUrl(2) - ORIG IS NULL!=!=!");
			return;
		}
		try {
			//if (!url.startsWith("/")) {
				this.url = orig.resolve(fixUrl(url));
			/*}
			else {
				this.url = new URI("http","www.academicearth.org",url);
			}*/
			
			String s = this.url.normalize().getPath();
			while (s != null && s.startsWith("/..")) {
				s = s.substring(3);
			}
			this.url = new URI(this.url.getScheme(),this.url.getHost(),s,this.url.getFragment());
			
			//Log.d(LectureViewer.APP_NAME,"setUrl2 - result: " + this.url);
		} catch (Exception e) {
			Log.d(LectureViewer.APP_NAME,"setUrl(2)",e);
		}
	}
	
	public static URI fixUrl(String url) throws URISyntaxException {
		String type;
		String domain;
		String rest;
		int slashslash = url.indexOf("://") + 3;
		if (slashslash < 4) {
			type = null;
			domain = null;
			rest = url;
		} else {
			int domainEnd = url.indexOf('/', slashslash);
			if (domainEnd == -1)
				domainEnd = url.length();
			type= url.substring(0,slashslash-3);
			domain = url.substring(slashslash, domainEnd);
			rest =url.substring(domainEnd); 
		}
		URI uri = new URI(
			    type, 
			    domain, 
			    rest,
			    null);
		return uri;
	}



	public NewLink() {
		this(null,null,null);
	}
	
	public NewLink(String name, URI url)  {
		this(name,url,null);
	}

	public NewLink(String name, URI url,String desc)  {
		this.name = name;
		this.url = url;
		this.desc = desc;
	}

	
	public String toString() {
		return name;
		//return "Url: " + url + " - " + this.getIntType();
	}

	@Override
	public int compareTo(NewLink another) {
		if (this.type.type == another.type.type)
			return this.getName().compareTo(another.getName());
		return this.type.type-another.type.type;
	}
}