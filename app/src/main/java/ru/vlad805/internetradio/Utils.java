package ru.vlad805.internetradio;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class Utils
{

	final public static String NOT_AVAILABLE_INTERNET_CONNECTION_EXCEPTION = "Нет доступа к Интернету. Проверьте параметры подключения";

	public static SharedPreferences getSettings (Context ctx)
	{
		return ctx.getSharedPreferences("vlad805radio", Context.MODE_PRIVATE);
	}

	public static String getString (Context ctx, String key)
	{
		return getSettings(ctx).getString(key, "");
	}

	public static boolean hasString (Context ctx, String key)
	{
		return getSettings(ctx).contains(key);
	}

	public static void setString (Context ctx, String key, String value)
	{
		getSettings(ctx).edit().putString(key, value).apply();
	}

	public static void setInt (Context ctx, String key, int value)
	{
		getSettings(ctx).edit().putInt(key, value);
	}

	public static int getInt (Context ctx, String key)
	{
		return getSettings(ctx).getInt(key, 0);
	}

	public static Toast toast (Context ctx, String text)
	{
		Toast toast = Toast.makeText(ctx, text, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.TOP, 0, 75);
		toast.show();
		return toast;
	}

	public static AlertDialog.Builder alert (Context ctx, String title, String text)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
		if (title != null)
			alert.setTitle(title);
		if (text != null)
			alert.setMessage(text);
		return alert;
	}

	public static ProgressDialog progress (Context ctx, String title, String text)
	{
		ProgressDialog dialog = new ProgressDialog(ctx);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		if (title != null)
			dialog.setTitle(title);
		dialog.setMessage(text);
		dialog.show();
		return dialog;
	}

	public static JSONObject API (Context ctx, String method, String params) throws NotAvailableInternetException
	{
		Internet i = new Internet(ctx);
		return i.load("http://api.vlad805.ru/" + method, params, true);
	}

	static Station getStation (Context ctx, int id)
	{
		try
		{
			return new StationsWrapper(new JSONObject(Utils.getString(ctx, MainActivity.KEY_STATIONS))).getStationById(id);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
