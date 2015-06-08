package ru.vlad805.internetradio;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Station
{
	public int stationId;
	public String title;
	public int cityId;
	public String city;
	public String site;
	public double frequency = 0;
	public String logo100;
	public ArrayList<Stream> streams;
	public boolean canRecognizeTrack;

	Station (JSONObject d) throws JSONException
	{
		this.stationId = d.getInt("stationId");
		this.title = d.getString("title");
		this.site = d.getString("site");
		this.cityId = d.has("cityId") ? d.getInt("cityId") : d.getInt("city");
		this.frequency = d.getDouble("frequency");
		this.streams = StationsWrapper.wrapStream(d.getJSONArray("streams"));
		this.canRecognizeTrack = d.getBoolean("canRecognizeTrack");
	}

	public Station setCity (HashMap<Integer, City> cities)
	{
		if (this.cityId > 0)
		{
			this.city = ((City) cities.get(this.cityId)).title;
		}
		return this;
	}

	public Stream getStreamByIndex (int index)
	{
		return index < 0 || index > this.streams.size() - 1 ? this.streams.get(index) : null;
	}

	public String getJSONString () {
		HashMap<String, Object> item;
		item = new HashMap<String, Object>();
		item.put("id", stationId);
		item.put("title", title);
		item.put("city", city);
		item.put("site", site);
		item.put("frequency", frequency);
		item.put("logo100", logo100);
		return new JSONObject(item).toString();
	}
}