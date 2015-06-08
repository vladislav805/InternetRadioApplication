package ru.vlad805.internetradio;

import org.json.JSONException;
import org.json.JSONObject;

public class City
{
	public int cityId;
	public String title;

	City (JSONObject d) throws JSONException
	{
		this.cityId = d.getInt("cityId");
		this.title = d.getString("title");
	}

	public String getTitle ()
	{
		return this.title;
	}
}