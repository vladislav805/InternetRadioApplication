package ru.vlad805.internetradio;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class StationsWrapper
{
	private ArrayList<Station> stations;
	private HashMap<Integer, Integer> stationIndexes;
	private HashMap<Integer, City> cities;

	StationsWrapper (JSONObject d)
	{
		stations  = new ArrayList<Station>();
		stationIndexes = new HashMap<Integer, Integer>();
		cities = new HashMap<Integer, City>();
		try
		{
			Log.i(MainActivity.LOG, d.getJSONObject("response").getJSONArray("cities").toString());
			wrapCities(d.getJSONObject("response").getJSONArray("cities"));
			wrapStations(d.getJSONObject("response").getJSONArray("items"));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	private void wrapCities (JSONArray c) throws JSONException
	{
		City item;
		for (int i = 0, l = c.length(); i < l; ++i)
		{
			item = new City(c.getJSONObject(i));
			this.cities.put(item.cityId, item);
		}
	}

	private void wrapStations (JSONArray s) throws JSONException
	{
		Station item;
		int k = 0;
		for (int i = 0, l = s.length(); i < l; ++i)
		{
			item = new Station(s.getJSONObject(i));
			item.setCity(this.cities);
			k = this.stations.size();
			this.stations.add(item);
			this.stationIndexes.put(k, item.stationId);
		}
	}

	public ArrayList<Station> getStations ()
	{
		return this.stations;
	}

	public City getCityById (int cityId)
	{
		return this.cities.get(cityId);
	}

	public Station getStationById (int stationId)
	{
		for (Station s: this.stations)
			if (s.stationId == stationId)
				return s;
		return null;
	}

	public Station getStationByPosition (int pos)
	{
		Log.i(">1", pos + "");
		Log.i(">2", this.stationIndexes.get(pos) + "");
		return this.stations.get(this.stationIndexes.get(pos));
	}

	static ArrayList<Stream> wrapStream (JSONArray d) throws JSONException
	{
		ArrayList<Stream> data = new ArrayList<Stream>(d.length());
		for (int i = 0, l = d.length(); i < l; ++i)
		{
			data.add(new Stream(d.getJSONObject(i)));
		}
		return data;
	}
}
