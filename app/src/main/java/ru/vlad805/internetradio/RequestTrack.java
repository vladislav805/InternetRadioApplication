package ru.vlad805.internetradio;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.ThemeUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class RequestTrack extends AppCompatActivity implements View.OnClickListener
{

	private LinearLayout loader;
	private ScrollView info;
	private LinearLayout error;

	private ImageView img;

	private TextView title;
	private TextView artist;

	public Track track;
	public static int stationId;
	public static Station station;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requesttrack);

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		loader = (LinearLayout) findViewById(R.id.track_loading);
		info = (ScrollView) findViewById(R.id.track_result);
		error = (LinearLayout) findViewById(R.id.track_error);

		img = (ImageView) findViewById(R.id.track_image);
		title = (TextView) findViewById(R.id.track_title);
		artist = (TextView) findViewById(R.id.track_artist);

		loader.setVisibility(View.VISIBLE);
		info.setVisibility(View.GONE);
		error.setVisibility(View.GONE);

		if (getIntent().getExtras() != null && getIntent().hasExtra("stationId"))
		{
			stationId = getIntent().getExtras().getInt("stationId", 0);
			station = Utils.getStation(this, stationId);
		}

		new Thread(loadInfo).start();
    }

	Runnable loadInfo = new Runnable ()
	{
		@Override
		public void run ()
		{
			try
			{
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("v", "2.0");
				params.put("stationId", String.valueOf(stationId));
				API request = new API(RequestTrack.this, "radio.getCurrentBroadcastingSong", params);
				JSONObject json = request.send().getResult();
				final Track tr = new Track(json);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showInfo(tr);
					}
				});
			} catch (NotAvailableInternetException e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Utils.toast(RequestTrack.this, Utils.NOT_AVAILABLE_INTERNET_CONNECTION_EXCEPTION);
					}
				});
				loader.setVisibility(View.GONE);
				info.setVisibility(View.GONE);
				error.setVisibility(View.VISIBLE);
			}

		}
	};


	public void showInfo (Track data) {
		loader.setVisibility(View.GONE);

		if (!data.isSuccess())
		{
			info.setVisibility(View.GONE);
			error.setVisibility(View.VISIBLE);
			return;
		}
		info.setVisibility(View.VISIBLE);
		error.setVisibility(View.GONE);



		if (data.getImage() != null && !data.getImage().isEmpty() && !data.getImage().equals("null")) {
			AsyncImageLoader ail = new AsyncImageLoader(this);
			ail.loadDrawable(data.getImage(), new AsyncImageLoader.ImageCallback()
			{
				public void imageLoaded(Drawable imageDrawable, String imageUrl)
				{
					int px = 400;
					img.setImageDrawable(imageDrawable);
					img.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT, px
					));
				}
			});
		}

		title.setText(data.getTitle());
		artist.setText(data.getArtist());

		this.track = data;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
/*			case R.id.track_addfave:
				Faves.addFave(this, this.station, this.track.originalTitle);
				Utils.toast(this, "Трек добавлен в закладки");
				v.setEnabled(false);
			break;
*/
			case R.id.track_copy:
				android.text.ClipboardManager cp = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				cp.setText(track.getTrackName());
				Utils.toast(this, "Название скопировано в буфер обмена");
				break;
		}
	}
	public void clickClose (View v)
	{
		finish();
	}
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
