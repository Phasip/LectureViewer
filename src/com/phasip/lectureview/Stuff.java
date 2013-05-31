package com.phasip.lectureview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.phasip.lectureview.background.Settings;

public class Stuff {
	public static byte[] objectToString(Object o) {
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		objectToStream(arrayOutputStream,o);
		return arrayOutputStream.toByteArray();
	}
	public static boolean objectToStream(OutputStream outStream,Object o) {
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(outStream);
			out.writeObject(o);
			out.close();
			outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static Object stringToObject(byte[] s) {
		ByteArrayInputStream byteArray = new ByteArrayInputStream(s);
		return streamToObject(byteArray);

	}
	public static Object streamToObject(InputStream s) {
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(s);
			return in.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;

	}
	
	public static Object fileToObject(File file)  {
			FileInputStream fos;
			try {
				fos = new FileInputStream(file);
				return streamToObject(fos);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
			
	}

	public static boolean objectToFile(Object o, File file) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return objectToStream(fos, o);	
	}
	
	public static long getTime() {
		return System.currentTimeMillis() / 1000L;
	}
	public static File URLtoNewFile(Activity a,URL u,String folder) {
		return URLtoNewFile(a, u, folder,null);
	}
	public static File URLtoNewFile(Activity a,URL u,String folder,String extra) {
		if(u == null)
		{
			if (Settings.isDebugging())
				Log.d("URLtoNewFile","URL is NULL");
			return null;
		}
		
		File f = a.getCacheDir();
		File d = new File(f, folder);
		if (!d.exists())
			d.mkdir();
		if (!d.isDirectory()) {
			if (Settings.isDebugging())
				Log.d("URLtoNewFile","File is no dir: " + d.getAbsolutePath());
			return null;
		}
		String filename = URLtoFilename(u,extra);
		File in = new File(d, filename);
		return in;
	}
	public static File URLtoFile(Activity a,URL u,String folder,long maxage) {
		return URLtoFile(a,u,folder,null,maxage);
	}
	public static File URLtoFile(Activity a,URL u,String folder,String extra,long maxage) {
		if (Settings.isDebugging())
			Log.d("URLtoFile","URLtoFile");
		File in = URLtoNewFile( a, u, folder,extra);
		if (!in.exists() || !in.isFile()) {
			if (Settings.isDebugging())
				Log.d("URLtoFile","File is no file or does not exist: " + in.toString());
			return null;
		}
		
		long age = Stuff.getTime() - in.lastModified()/1000L;
		if (Settings.isDebugging())
			Log.d("URLtoFile","Age: " + age + " Maxage: " + maxage);
		if (age > maxage) {
			if (Settings.isDebugging())
				Log.d("URLtoFile","File is too old");
			return null;
		}
		
		return in;
	}
	public static String URLtoFilename(URL u,String extra) {
		if(u == null)
		{
			return null;
		}
		String url = u.toExternalForm() + "?" + extra;
		url = url.replace("http://www.academicearth.org", "");
		return Base64.encodeToString(url.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
	}
	
	public static void shortToast(Activity p,String msg) {
		Context context = p.getApplicationContext();
		int len = Toast.LENGTH_SHORT;
		if (msg.length() > 20)
			len = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, msg, len);
		toast.show();
	}
	public static void shortToast(Activity p, int i) {
		String msg = p.getString(i);
		shortToast(p,msg);
	}
}
