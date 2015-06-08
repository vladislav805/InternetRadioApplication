package ru.vlad805.internetradio;

import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
	final public static String KEY_STATIONS = "stations";

	public ProgressDialog pdLoading;
	public StationsWrapper data;

	public ListView lvStations;
	public ImageButton ibButtonState;
	public TextView tvTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pdLoading = Utils.progress(this, null, "Пожалуйста, подождите");
		pdLoading.show();

		if (Utils.hasString(this, KEY_STATIONS))
		{
			try
			{
				StationsWrapper data = openResponse(new JSONObject(Utils.getString(this, KEY_STATIONS)));
				showList(data);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		else
			new Thread(loadList).start();

		lvStations = (ListView) findViewById(R.id.stationsList);
		ibButtonState = (ImageButton) findViewById(R.id.miniplayerState);
		tvTitle = (TextView) findViewById(R.id.miniplayerTitle);

		ibButtonState.setOnClickListener(this);
	}

	Runnable loadList = new Runnable ()
	{
		@Override
		public void run ()
		{
			runOnUiThread(new Runnable ()
			{
				@Override
				public void run()
				{
					if (pdLoading == null)
					{
						pdLoading = Utils.progress(MainActivity.this, null, "");
						pdLoading.show();
					}
					pdLoading.setTitle("Запрос к API");
					pdLoading.setMessage("Загрузка списка доступных радиостанций");
					pdLoading.show();
				}
			});
			try
			{
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("v", "2.0");
				params.put("count", "100");
				API request = new API(MainActivity.this, "radio.get", params);
				JSONObject json = request.send().getResult();
				Utils.setString(MainActivity.this, KEY_STATIONS, json.toString());
				data = openResponse(json);
				showList(data);
			} catch (Throwable e) {
				e.printStackTrace();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Utils.toast(MainActivity.this, Utils.NOT_AVAILABLE_INTERNET_CONNECTION_EXCEPTION);
						if (pdLoading != null)
							pdLoading.cancel();
					}
				});
			}
		}
	};



	final public static String LOG = "Vlad805Radio";

	public StationsWrapper openResponse (JSONObject d)
	{
		return new StationsWrapper(d);
	}


	public void showList (StationsWrapper data)
	{
		ArrayList<Station> stations = data.getStations();
		if (pdLoading != null)
			pdLoading.cancel();
		final ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> item;
		for (Station station : stations)
		{
			item = new HashMap<String, Object>();
			item.put("title", station.title);
			item.put("city", station.city != null ? station.city : "");
			item.put("id", station.stationId);
			items.add(item);
		}

		runOnUiThread(new Runnable() {
			public void run() {

				SimpleAdapter adapter = new SimpleAdapter(
						getApplicationContext(),
						items,
						R.layout.item_station,
						new String[]{
								"title", "city", "id"
						},
						new int[]{
								R.id.stationTitle, R.id.stationCity, R.id.stationId
						}
				);
				lvStations.setAdapter(adapter);
				lvStations.setChoiceMode(ListView.CHOICE_MODE_NONE);
				lvStations.setOnItemClickListener(onStationClick);
				lvStations.setOnItemLongClickListener(onStationLongClick);
			}
		});
	}

	AdapterView.OnItemClickListener onStationClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			int sid = Integer.valueOf(((TextView) view.findViewById(R.id.stationId)).getText().toString());
			Station s = data.getStationById(sid);
			setCurrentStation(s);
		}
	};
	AdapterView.OnItemLongClickListener onStationLongClick = new AdapterView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

			Station s = data.getStationById(position);
			//Intent intent = new Intent(MainActivity.this, StationActivity.class);
			//intent.putExtra("station", s.getJSONString());
			//startActivity(intent);
			return false;
		}
	};

	public ContextWrapper service = null;
	public Intent serviceIntent;

	public void setCurrentStation (Station station) {

		if (service != null)
		{
			stopService(serviceIntent);
			service.stopService(serviceIntent);
			serviceIntent = null;
			service = null;
		}

		Intent i = new Intent(this, PlayerService.class);
		i.putExtra("stationId", station.stationId);
		serviceIntent = i;
		startService(i);

		setTitleMiniplayer(station);
	}



	public void setTitleMiniplayer (Station s)
	{
		tvTitle.setText(s.title);
	}


	@Override
	public void onClick (View v)
	{
		switch (v.getId())
		{
			case R.id.miniplayerState:
				if (PlayerService.media != null)
					setState(!PlayerService.media.isPlaying());
				break;
		}
	}

	public void setState (boolean isState)
	{
		MediaPlayer m = PlayerService.media;
		if (m == null)
			return;
		if (isState)
			m.start();
		else
			m.pause();
		updateUI(isState);
	}

	public void updateUI (boolean isState)
	{
		ibButtonState.setImageResource(isState ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;
		switch (item.getItemId())
		{
			case R.id.action_track:
				intent = new Intent(this, RequestTrack.class);
				intent.putExtra("stationId", PlayerService.stationId);
				startActivity(intent);
				break;

			case R.id.action_about:
				intent = new Intent(this, About.class);
				startActivity(intent);
				break;

			case R.id.action_refresh:
				new Thread(loadList).start();
				break;

			case R.id.action_exit:
				finish();
				stopService(serviceIntent);
				PlayerService.media = null;
				break;
		};
		return super.onOptionsItemSelected(item);
	}
}
