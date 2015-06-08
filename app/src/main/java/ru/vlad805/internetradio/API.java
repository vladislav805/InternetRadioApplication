package ru.vlad805.internetradio;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Admin on 08.06.2015.
 */
public class API
{
	final public static String API_DOMAIN = "api.vlad805.ru";

	private Context context;
	private String method;
	private HashMap<String, String> params;
	private JSONObject result;

	API (Context ctx, String method, HashMap<String, String> params)
	{
		this.context = ctx;
		this.method = method;
		this.params = params;
	}

	public API addParam (String key, String value)
	{
		this.params.put(key, value);
		return this;
	}

	public String getParam (String key)
	{
		return this.params.get(key);
	}

	public API removeParam (String key)
	{
		this.params.remove(key);
		return this;
	}

	public API send () throws NotAvailableInternetException
	{
		Internet i = new Internet(this.context);
		StringBuilder p = new StringBuilder();

		for (String key: params.keySet())
			p.append(key).append("=").append(params.get(key)).append("&");

Log.i(MainActivity.LOG, p.toString());
		this.result = i.load("http://" + API_DOMAIN + "/" + method, p.toString(), true);
		return this;
	}

	public JSONObject getResult ()
	{
		return this.result;
	}
}
