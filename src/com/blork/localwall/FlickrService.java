package com.blork.localwall;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


public class FlickrService extends Service implements Runnable {
	static final String filename = "localWall.jpg";
	static final String ACTION_NEW_LOCALWALL = "ACTION_NEW_LOCALWALL";

	private double latitude;
	private double longitude;
	
   
    /** Called when the activity is first created. */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		
    	Log.e(Flickr.TAG,"onstartcommand");
    	try {
			Bundle extras = intent.getExtras();
			latitude = extras.getDouble("latitude");
			longitude = extras.getDouble("longitude");
		} catch (Exception e) {
			e.printStackTrace();
			LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
	        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
	        criteria.setAltitudeRequired(false);
	        criteria.setBearingRequired(false);
	        criteria.setCostAllowed(false);
	        criteria.setPowerRequirement(Criteria.POWER_LOW);
	        String provider = lm.getBestProvider(criteria, true);
			Location location = lm.getLastKnownLocation(provider);
			try {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			} catch (Exception e1) {
				e1.printStackTrace();
				stopSelf();
			}
		}

    	Log.e(Flickr.TAG, latitude+"/"+longitude);
//        long firstTime = SystemClock.elapsedRealtime();
//        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
//		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, firstTime,  AlarmManager.INTERVAL_HOUR, sender);  

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);				

		boolean updatesEnabled = prefs.getBoolean("updatesEnabled", false);
		int updates = prefs.getInt("updates", 3);
		
    	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        long minTime = 3600000; //1 hours
        
        int[] updateValues = {250, 500, 1000, 5000, 10000};
        Log.i(Flickr.TAG, "Setting updates to every "+updateValues[updates]+"m");
        Log.i(Flickr.TAG, updates+"");
        float minDistance = updateValues[updates]; //only update if you've moved more than 1/2km
        
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = lm.getBestProvider(criteria, true);
        
        Log.i(Flickr.TAG, "Best location provider is: "+provider);

        Intent intent2 = new Intent("com.blork.localwall.LOCATION_CHANGED");
        PendingIntent sender = PendingIntent.getBroadcast(FlickrService.this, 0, intent2, 0); 

        lm.removeUpdates(sender);
        
        if(updatesEnabled){  
	        lm.requestLocationUpdates(provider, minTime,  minDistance, sender);
        }
        
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	
    	
    	if(!IsDataEnabled()){
    		Log.e(Main.TAG, "User has background data turned off!.");
 			int icon = android.R.drawable.stat_sys_warning;
			Notification notification = new Notification(icon, "Can't Update Wallpaper", System.currentTimeMillis());
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, null, 0);
			notification.setLatestEventInfo(this, "Can't Update Wallpaper!", "You have background data usage turned off!", contentIntent);	
			nm.notify(3, notification);
    		stopSelf();
    	}

 		if (IsNetworkConnected()) {
 			Thread thread = new Thread(this);
 	        thread.start();
 		}else{
 			Log.d(Main.TAG, "No Connection.");
 			int icon = android.R.drawable.stat_sys_warning;
			Notification notification = new Notification(icon, "Can't Update Wallpaper", System.currentTimeMillis());
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, null, 0);
			notification.setLatestEventInfo(this, "Can't Update Wallpaper", "No internet connection detected.", contentIntent);	
			nm.notify(3, notification);
 			stopSelf();
 		}
 		
		return START_STICKY;
    }

    private boolean IsDataEnabled(){
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	return connManager.getBackgroundDataSetting();
    }
    private boolean IsNetworkConnected() {
	  	boolean result = false;
	  	ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	  	NetworkInfo info = connManager.getActiveNetworkInfo();
	  	if (info == null || !info.isConnected()) {
	  		result = false;
	  	} else {
	  		if (!info.isAvailable()) {
	  			result = false;
	  		} else {
	  			result = true;
	  		}
	  	}
	  	return result;
  	}
    
	public void run() {
		
 		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		try {
			if(true){
				nm.cancelAll();
				
				int icon = android.R.drawable.stat_sys_download;
				Notification notification = new Notification(icon, "Updating Wallpaper", System.currentTimeMillis());
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, null, 0);
				notification.setLatestEventInfo(this, "Updating Wallpaper", "Downloading Wallpaper.", contentIntent);
				notification.flags = Notification.FLAG_ONGOING_EVENT^Notification.FLAG_NO_CLEAR;	
				nm.notify(3, notification);
			}
			try {

		        Flickr flickr = new Flickr();
		        flickr.getImage(latitude, longitude);
		        
		        flickr.save(this.openFileOutput(FlickrService.filename, Context.MODE_PRIVATE));
		        FileInputStream is = openFileInput(FlickrService.filename);

				WallpaperManager wm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
				wm.setStream(is);
				Log.i(Flickr.TAG, "Wallpaper set!");
				
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);	
				SharedPreferences.Editor editor = settings.edit();
	        	editor.putString("placename", flickr.placeName);
	        	editor.putString("title", flickr.title);
	        	editor.putString("id", flickr.id);
	        	editor.putString("owner", flickr.owner);
	            editor.commit();
	            
	            
	            
//				int icon = android.R.drawable.stat_sys_download;
//				Notification notification = new Notification(icon, "New Picture!", System.currentTimeMillis());
//				notification.flags = Notification.FLAG_AUTO_CANCEL;
//				Intent notificationIntent = new Intent(this, Main.class); 
//				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//				notification.setLatestEventInfo(this, "New Wallpaper", flickr.placeName, contentIntent);
//				nm.notify(1, notification);
			} catch (SaveToStorageException e1) {	
				e1.printStackTrace();
				Log.d(Flickr.TAG, "Quite possibly a server issue?");        
			            
			} catch (BadLocationException e1) {	
				e1.printStackTrace();
				int icon = android.R.drawable.stat_sys_warning;
				Notification notification = new Notification(icon, "Local Wallpaper encountered a problem.", System.currentTimeMillis());
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				Intent notificationIntent = new Intent(this, Main.class); 
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
				notification.setLatestEventInfo(this, "Local Wallpaper has encountered a problem.!", "Local Wallpaper cannot find any images near you! Try again later.", contentIntent);
				nm.notify(2, notification);
						        
			} catch(IOException e1){
					e1.printStackTrace();
					
					Log.d(Flickr.TAG, "Quite possibly a DNS issue.");
					
					
//					int icon = android.R.drawable.stat_sys_warning;
//					Notification notification = new Notification(icon, "Local Wallpaper couldn't find the server", System.currentTimeMillis());
//					notification.flags = Notification.FLAG_AUTO_CANCEL;
//									
//					Intent notificationIntent = new Intent(this, Main.class); 
//					PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//					notification.setLatestEventInfo(this, "There may be a problem with your connection", "Please try running the APOD update manually.", contentIntent);
//					nm.notify(2, notification);

			} catch (Throwable e1) {	
				e1.printStackTrace();
				int icon = android.R.drawable.stat_sys_warning;

				Notification notification = new Notification(icon, "Local Wallpaper encountered a problem.", System.currentTimeMillis());
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				
				Intent emailIntent = new Intent(Intent.ACTION_SEND); 
				emailIntent.setType("message/rfc822");
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"sam+market@blork.co.uk"}); 
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with Local Wallpaper App"); 
				String stacktrace = getStackTraceAsString(e1);
				emailIntent.putExtra(Intent.EXTRA_TEXT, stacktrace); 
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, emailIntent, 0);
				notification.setLatestEventInfo(this, "Local Wallpaper has encountered a problem. Sorry!", "Click here to email me the debug info.", contentIntent);
				nm.notify(2, notification);
				
			}
		} finally{		
			sendBroadcast(new Intent(ACTION_NEW_LOCALWALL));
			nm.cancel(3);
			stopSelf();
		}		
	}


	public static String getStackTraceAsString(Throwable t){
		StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true); 
        t.printStackTrace(pw);
        pw.flush();
        sw.flush(); 
        String stacktrace = sw.toString();
        String deviceinfo = 
        	Build.BRAND+" "+
	        Build.MANUFACTURER+" "+
	        Build.DEVICE+" "+
	        Build.MODEL+"\n"+
	        "Android version: "+Build.VERSION.SDK_INT+"\n"+
	        Build.DISPLAY;
   
        return stacktrace+"\n"+deviceinfo;
    }	
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}

