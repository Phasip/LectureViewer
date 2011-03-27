package com.phasip.lectureview;

import java.util.LinkedList;
import java.util.StringTokenizer;

import android.util.Log;

public class StorableStringList extends LinkedList<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9179077988799719198L;
	public static StorableStringList loadString(String str,String separator)
	{
		StorableStringList ret = new StorableStringList();
		StringTokenizer st = new StringTokenizer(str,separator);
		while (st.hasMoreTokens())
		{
			ret.addLast(st.nextToken());
			Log.i("Moo","Loading  - " + ret.getLast());
		}
		return ret;
	}
	public String saveString(String separator)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size(); i++)
		{
			Log.i("Moo","Saving " + i + " - " + get(i).toString());
			sb.append(get(i).toString() + separator);
		}
		return sb.toString();
	}
}
