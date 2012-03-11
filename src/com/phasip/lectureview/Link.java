package com.phasip.lectureview;

import java.io.Serializable;

class Link implements Comparable<Link>,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1069471713052847298L;
	private String name;
	private String url;
	private String desc;
	private transient Link prev;
	private Patterns type;
	
    public Link clone()
    {
    	Link r = new Link();
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
	    	if (l == null ||l.getClass() != Link.class)
	    		return false;

	    	String hisurl = ((Link)l).getUrl();
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
	public Link getPrev()
	{
		return prev;
	}
	public void setPrev(Link l)
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Link() {
		this(null,null,null);
	}
	public Link(String name, String url) {
		this(name,url,null);
	}

	public Link(String name, String url,String desc) {
		this.name = name;
		this.url = url;
		this.desc = desc;
	}

	public String toString() {
		return name;
		//return "Url: " + url + " - " + this.getIntType();
	}

	@Override
	public int compareTo(Link another) {
		return this.getName().compareTo(another.getName());
	}
}