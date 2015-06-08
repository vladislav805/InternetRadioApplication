package ru.vlad805.internetradio;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.support.v7.app.NotificationCompat.Builder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

public class PlayerService extends Service
{

	public static int stationId;
	private Station station;
	public static MediaPlayer media;


	public int onStartCommand (Intent intent, int flags, int startId) {
		stationId = intent.getIntExtra("stationId", 0);
		if (stationId == 0) return 0;

		try
		{
			station = new StationsWrapper(new JSONObject(Utils.getString(this, MainActivity.KEY_STATIONS))).getStationById(stationId);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			kill();
			return 0;
		}
		setNotification();
		initMedia(station.streams.get(0).url);
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (media != null)
			media.release();
		media = null;
		mNotifyManager.cancelAll();
	}

	private void kill ()
	{
		this.stopSelf();
	}

	private boolean isOnline ()
	{
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	private void initMedia (String url)
	{
		try
		{
			if (media != null)
			{
				media.release();
			}
			media = null;
			media = new MediaPlayer();
			media.setDataSource(url);
			media.prepareAsync();
			media.setVolume(1, 1);
			media.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
			{
				@Override
				public void onPrepared(MediaPlayer mp)
				{
					mp.start();
					startTime();
				}
			});
			media.setOnErrorListener(new MediaPlayer.OnErrorListener()
			{
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra)
				{
					return false;
				}
			});
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	private int notificationId;
	private Context context = this;
	private NotificationManager mNotifyManager;
	private Builder mBuilder;
	private NotificationCompat.BigTextStyle mNotifyText;
	private static final String EVENT_STOP = "EVENT_STOP";
	private static final String EVENT_FIND = "EVENT_FIND";

	BroadcastReceiver eventListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action)
			{
				case EVENT_STOP:
					if (media.isPlaying())
					{
						media.pause();
					}
					else
					{
						media.start();
					}
					break;
				case EVENT_FIND:
					Intent i = new Intent(context, RequestTrack.class);
					i.putExtra("stationId", station.stationId);
					i.putExtra("isDialog", true);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);
					break;
			}
		};
	};


	private void setNotification ()
	{
		notificationId++;

		mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(context);


		context.registerReceiver(eventListener, new IntentFilter(EVENT_STOP));
		context.registerReceiver(eventListener, new IntentFilter(EVENT_FIND));

		mBuilder.setContentTitle("Internet Radio")
				.setContentText("00:00")
				.setPriority(0)
				.setSmallIcon(android.R.drawable.ic_media_play);

		PendingIntent pause = PendingIntent.getBroadcast(context, 12345, new Intent().setAction(EVENT_STOP), PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent find = PendingIntent.getBroadcast(context, 12345, new Intent().setAction(EVENT_FIND), PendingIntent.FLAG_CANCEL_CURRENT);

		mNotifyText = new NotificationCompat.BigTextStyle(mBuilder);
		mBuilder.setOngoing(true);
		mBuilder.addAction(android.R.drawable.ic_media_play, "Пауза", pause);
		mBuilder.addAction(android.R.drawable.ic_search_category_default, "Найти трек", find);

		mBuilder.setStyle(mNotifyText.bigText(station.title));
		mNotifyManager.notify(notificationId, mBuilder.build());
	}

	private void setTimeNotification (String time)
	{
		mBuilder.setContentText(time);
		mNotifyText.bigText(time + " | " + station.title);
		// mBuilder.setStyle(mNotifyText.bigText(station.title));
		mNotifyManager.notify(notificationId, mBuilder.build());
	}

	private String getTime ()
	{
		int num = media.getCurrentPosition() / 1000,
			second = (num % 60),
			minute = (num / 60 % 60),
			hour = (num / 60 / 60 % 60);

		StringBuilder str = new StringBuilder();
		if (hour > 0)
			str.append(n2(hour)).append(":");
		str.append(n2(minute)).append(":").append(n2(second));
		return str.toString();
	}

	private String n2 (int n)
	{
		return String.valueOf(n < 10 ? "0" + n : n);
	}
	private void startTime ()
	{
		new Timer().scheduleAtFixedRate(new TimerTask ()
		{
			@Override
			public void run ()
			{
				setTimeNotification(getTime());
			}
		}, 0, 1000);
	}

	private MediaPlayer getPlayer ()
	{
		return this.media;
	}
}
