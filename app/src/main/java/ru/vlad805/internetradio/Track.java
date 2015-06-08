package ru.vlad805.internetradio;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Track {

	private boolean success;
	private String name;
	private String title;
	private String artist;
	private int stationId;
	private String image;

	Track (JSONObject d)
	{
		try
		{
			d = d.getJSONObject("response");
			this.success = d.getBoolean("success");
			this.name = d.getString("title").trim();

			if (!this.name.equals("") && this.name.length() > 1) {
				String[] splitted = this.name.split("-");
				String title = this.name.substring(this.name.indexOf("-") + 2);
				this.artist = splitted[0];
				this.title = title;
			}
			else
			{
				this.success = false;
			}
			this.stationId = d.getInt("stationId");
			this.image = d.getString("image");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public String getTrackName ()
	{
		return this.isSuccess() ? this.artist + " - " + this.title : "< ошибка >";
	}

	public String getArtist ()
	{
		return this.isSuccess() ? this.artist : "<?>";
	}

	public String getTitle ()
	{
		return this.isSuccess() ? this.title : "<?>";
	}

	public boolean isSuccess ()
	{
		return this.success;
	}

	public String getImage ()
	{
		return this.image;
	}
}
