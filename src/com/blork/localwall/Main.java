package com.blork.localwall;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class Main extends Activity {
	public static final String TAG = "LocalWall";

	SharedPreferences prefs;
	Boolean updatesEnabled;
	int updates;
	TextView wallpaperPlace;
	TextView wallpaperTitle;
	TextView wallpaperLink;
	BroadcastReceiver updateReceiver;
	Spinner spinner;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);				

		updatesEnabled = prefs.getBoolean("updatesEnabled", false);
		updates = prefs.getInt("updates", 2);

		wallpaperPlace = (TextView)findViewById(R.id.place);
		wallpaperTitle = (TextView)findViewById(R.id.title);
		wallpaperLink = (TextView)findViewById(R.id.link);

		updateText();
		updateReceiver = new UpdateReceiver();
		registerReceiver(updateReceiver, new IntentFilter(FlickrService.ACTION_NEW_LOCALWALL));


		spinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.updates, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(updates);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent,
					View view, int pos, long id) {

				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("updates", pos);

				Log.d(Flickr.TAG, "Updates option selected: "+parent.getItemAtPosition(pos).toString());
				editor.commit();

				updates = pos;

				if(updatesEnabled){
					enableLocation();
				}else{
					disableLocation();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});
		spinner.setEnabled(updatesEnabled);


		CheckBox checkbox = (CheckBox) findViewById(R.id.checkbox);
		checkbox.setChecked(updatesEnabled);
		checkbox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = prefs.edit();
				updatesEnabled = !updatesEnabled;
				editor.putBoolean("updatesEnabled", updatesEnabled);
				Log.d(Flickr.TAG, "Updates enabled: "+updatesEnabled);
				editor.commit();

				spinner.setEnabled(updatesEnabled);

				if(updatesEnabled){      
					enableLocation();
				}else{
					disableLocation();
				}
			}
		});

		Button button = (Button) findViewById(R.id.update);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, FlickrService.class);
				startService(intent); 
			}
		});




	}	

	public void updateText(){
		String title = prefs.getString("title", "N/A");
		String placename = prefs.getString("placename", "Not Available");
		String id = prefs.getString("id", "null");
		String owner = prefs.getString("owner", "null");
		wallpaperPlace.setText(placename);

		if(title.equals("")){
			wallpaperTitle.setText("Untitled");
		}else{
			wallpaperTitle.setText(title);
		}
		if(!id.equals("null") || !owner.equals("null")){
			wallpaperLink.setText("flickr.com/photos/"+owner+"/"+id+"/"); 
			wallpaperLink.setVisibility(View.VISIBLE);
		}
	}

	public void enableLocation(){
		Log.i(Flickr.TAG, "Location enabled.");
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		long minTime = 3600000; //1 hours

		int[] updateValues = {250, 500, 1000, 5000, 10000};
		Log.i(Flickr.TAG, "Setting updates to every "+updateValues[updates]+"m");
		Log.i(Flickr.TAG, updates+"");
		float minDistance = updateValues[updates]; //only update if you've moved more than x

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);

		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		String provider = lm.getBestProvider(criteria, true);

		Log.i(Flickr.TAG, "Best location provider is: "+provider);

		Intent intent = new Intent("com.blork.localwall.LOCATION_CHANGED");
		PendingIntent sender = PendingIntent.getBroadcast(Main.this, 0, intent, 0); 

		lm.removeUpdates(sender);
		lm.requestLocationUpdates(provider, minTime,  minDistance, sender);
	}

	public void disableLocation(){
		Log.i(Flickr.TAG, "Location disabled.");
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Intent intent = new Intent("com.blork.localwall.LOCATION_CHANGED");
		PendingIntent sender = PendingIntent.getBroadcast(Main.this, 0, intent, 0); 
		lm.removeUpdates(sender);
	} 


	class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) { 
			Log.d(Flickr.TAG, "received broadcast");
			updateText();
		}
	}

	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(updateReceiver);
	}
}
