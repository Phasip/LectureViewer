package com.phasip.lectureview;

class Link implements Comparable<Link> {
	private String name;
	private String url;
	private String desc;

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
	}

	@Override
	public int compareTo(Link another) {
		return this.getName().compareTo(another.getName());
	}
}