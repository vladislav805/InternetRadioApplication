package ru.vlad805.internetradio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;


public class About extends AppCompatActivity
{
    @Override
    protected void onCreate (Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		};
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				break;
		}
        return super.onOptionsItemSelected(item);
    }

	public void goSite (View v)
	{
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://vlad805.ru/apps?act=show&id=1")));
	}
}
