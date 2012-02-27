package com.blork.localwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(Flickr.TAG, "Broadcast Received.");
	    //Do this when the system sends the intent
    	try{
	    Bundle b = intent.getExtras();
	    Location location = (Location)b.get(LocationManager.KEY_LOCATION_CHANGED);
	
		Intent serviceIntent = new Intent(context, FlickrService.class);
		serviceIntent.putExtra("latitude", location.getLatitude());
		serviceIntent.putExtra("longitude", location.getLongitude());
		context.startService(serviceIntent);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
}