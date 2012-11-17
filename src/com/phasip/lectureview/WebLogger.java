package com.phasip.lectureview;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.os.AsyncTask;
import android.util.Log;

public class WebLogger {
	final static String urlServer = "_you know the source is online?_";//"http://ssh.rthism.com:64/u/";
	final static String logFile = "log.php";
	final static String upFile = "upload.php";
	
	public static void upload_log(String logdata) {
		new AsyncTask<String, Void, String>() {

		@Override
		protected String doInBackground(String... params) {
			upload_log_internal(params[0]);
			return null;
		} }.execute(logdata);
			
	}
	private static void upload_log_internal(String logdata) {
	//	Log.d("MOOO",logdata);
		HttpURLConnection connection = null;

		try
		{
			logdata = java.net.URLEncoder.encode(logdata);
		URL url = new URL(urlServer + logFile + "?logtext=" + logdata);
		connection = (HttpURLConnection) url.openConnection();
		// Allow Inputs & Outputs
		/*connection.setDoInput(true);
		connection.setDoOutput(true);*/
		connection.setUseCaches(false);
		connection.setRequestMethod("GET");
		// Responses from the server (code and message)
		/*int serverResponseCode =*/ connection.getResponseCode();
		/*String serverResponseMessage = */connection.getResponseMessage();
		}
		catch (Exception ex)
		{
			Log.d("AAAA","ERROR", ex);
		//Exception handling
		}

	}
	public static void upload_data(final String name,final byte[] buffer) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				upload_data_internal(name,buffer);
				return null;
			}

		 }.execute();
			
	}
	private static void upload_data_internal(String name,byte[] buffer) {
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";


		try
		{
		name = URLEncoder.encode(name);
		URL url = new URL(urlServer + upFile + "?name=" + name);
		connection = (HttpURLConnection) url.openConnection();

		// Allow Inputs & Outputs
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);

		// Enable POST method
		connection.setRequestMethod("POST");

		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

		outputStream = new DataOutputStream( connection.getOutputStream() );
		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
		outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"_\"" + lineEnd);
		outputStream.writeBytes(lineEnd);


		outputStream.write(buffer, 0, buffer.length);
		outputStream.writeBytes(lineEnd);
		outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

		// Responses from the server (code and message)
		/*int serverResponseCode = */connection.getResponseCode();
		/*String serverResponseMessage = */connection.getResponseMessage();

		outputStream.flush();
		outputStream.close();
		}
		catch (Exception ex)
		{
		//Exception handling
		}
	}
	/*public static void upload_data() {
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		DataInputStream inputStream = null;

		String pathToOurFile = "/data/file_to_send.mp3";
		String urlServer = "http://192.168.1.1/handle_upload.php";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1*1024*1024;

		try
		{
		FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );

		URL url = new URL(urlServer);
		connection = (HttpURLConnection) url.openConnection();

		// Allow Inputs & Outputs
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);

		// Enable POST method
		connection.setRequestMethod("POST");

		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

		outputStream = new DataOutputStream( connection.getOutputStream() );
		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
		outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
		outputStream.writeBytes(lineEnd);

		bytesAvailable = fileInputStream.available();
		bufferSize = Math.min(bytesAvailable, maxBufferSize);
		buffer = new byte[bufferSize];

		// Read file
		bytesRead = fileInputStream.read(buffer, 0, bufferSize);

		while (bytesRead > 0)
		{
		outputStream.write(buffer, 0, bufferSize);
		bytesAvailable = fileInputStream.available();
		bufferSize = Math.min(bytesAvailable, maxBufferSize);
		bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		}

		outputStream.writeBytes(lineEnd);
		outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

		// Responses from the server (code and message)
		int serverResponseCode = connection.getResponseCode();
		String serverResponseMessage = connection.getResponseMessage();

		fileInputStream.close();
		outputStream.flush();
		outputStream.close();
		}
		catch (Exception ex)
		{
		//Exception handling
		}
	}*/
}

