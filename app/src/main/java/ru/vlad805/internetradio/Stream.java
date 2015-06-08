package ru.vlad805.internetradio;

import org.json.JSONException;
import org.json.JSONObject;

public class Stream
{
	public int bitrate;
	public String format;
	public String url;
	public int cityId;
	public String city;

	Stream (JSONObject d) throws JSONException
	{
		this.bitrate = d.getInt("bitrate");
		this.format = d.getString("format");
		this.url = d.getString("url");
		if (d.has("cityId"))
			this.cityId = d.getInt("cityId");
	}
}