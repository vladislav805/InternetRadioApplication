package ru.vlad805.internetradio;

import android.content.Context;
import android.net.ConnectivityManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.zip.GZIPInputStream;

import org.json.JSONException;
import org.json.JSONObject;


public class Internet {
	private boolean state;
	private Context ctx;

	Internet (Context ctx) {
		this.ctx = ctx;
		this.state = isNetworkAvailable(ctx);
	}
	public JSONObject load (String url, String body, boolean isPost) throws NotAvailableInternetException {
		HttpURLConnection connection = null;
		if (!state)
			throw new NotAvailableInternetException();
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);
			connection.setUseCaches(false);
			connection.setDoOutput(isPost);
			connection.setDoInput(true);
			connection.setRequestMethod(isPost ? "POST" : "GET");
			if (isPost)
				connection.getOutputStream().write(body.getBytes("UTF-8"));
			connection.getResponseCode();
			InputStream is = new BufferedInputStream(connection.getInputStream(), 8192);
			String enc = connection.getHeaderField("Content-Encoding");
			if (enc != null && enc.equalsIgnoreCase("gzip"))
				is = new GZIPInputStream(is);
			String response = convertStreamToString(is);
			return new JSONObject(response);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		return null;
	}
	public static String convertStreamToString(InputStream is) throws IOException {
		InputStreamReader r = new InputStreamReader(is);
		StringWriter sw = new StringWriter();
		char[] buffer = new char[1024];
		try {
			for (int n; (n = r.read(buffer)) != -1;)
				sw.write(buffer, 0, n);
		}
		finally{
			try {
				is.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return sw.toString();
	}
	public boolean isNetworkAvailable () {
		return ((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
	}
	public static boolean isNetworkAvailable (Context context) {
		return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
	}
}