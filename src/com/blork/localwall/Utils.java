package com.blork.localwall;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

// TODO: Auto-generated Javadoc
/**
 * The Class Utils.
 */
public class Utils {

	/** The Constant TAG. */
	public static final String TAG = "Astronomy Picture of the Day";

	/**
	 * Gets the jSON.
	 *
	 * @param url the url
	 * @return the jSON
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 */
	public static String getStringFromUrl(URL url) throws IOException, URISyntaxException {
		//InputStream instream = getStream(url);
		URLConnection connection = url.openConnection();
		InputStream instream = connection.getInputStream();
		return streamToString(instream);
	}

	/**
	 * Stream to string.
	 *
	 * @param is the is
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static String streamToString(InputStream is) throws IOException {
		try {
			BufferedReader buf = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			StringBuilder sb = new StringBuilder();
			String s;
			while(true) {
				s = buf.readLine();
				if(s==null || s.length()==0)
					break;
				sb.append(s);
			}
			buf.close();
			is.close();
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Checks if is network connected.
	 *
	 * @param context the context
	 * @return true, if successful
	 */
	public static boolean isNetworkConnected(Context context) {
		boolean result = false;
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

	public static boolean isDataEnabled(Context context){
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connManager.getBackgroundDataSetting();
	}

	/**
	 * Checks if is wi fi connected.
	 *
	 * @param context the context
	 * @return true, if successful
	 */
	public static boolean isWiFiConnected(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();

		if (info == null) {
			return false;
		}

		int netType = info.getType();
		if (netType == ConnectivityManager.TYPE_WIFI) {
			return info.isConnected();
		} else {
			return false;
		}
	}



	

	public static void copyFileToUserSpace(Context context, Uri uri) throws IOException, URISyntaxException { 
		FileInputStream from = null; 
		FileOutputStream to = null; 

		File fromFile = new File(new URI(uri.toString()));

		File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "APOD");
		dir.mkdir();

		File toFile = new File(dir + File.separator + fromFile.getName());

		try { 
			from = new FileInputStream(fromFile); 
			to = new FileOutputStream(toFile); 
			byte[] buffer = new byte[4096]; 
			int bytesRead; 
			while ((bytesRead = from.read(buffer)) != -1) 
				to.write(buffer, 0, bytesRead); // write 
		} finally { 
			if (from != null) 
				try { 
					from.close(); 
				} catch (IOException e) { 
					; 
				} 
			if (to != null) 
				try { 
					to.close(); 
				} catch (IOException e) { 
					; 
				} 
		} 
		Uri newUri = Uri.fromFile(toFile);
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
	}

	/**
	 * Checks if is honeycomb.
	 *
	 * @return true, if is honeycomb
	 */
	public static boolean isHoneycomb() {
		// Can use static final constants like HONEYCOMB, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed behavior.
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	/**
	 * Checks if is honeycomb tablet.
	 *
	 * @param context the context
	 * @return true, if is honeycomb tablet
	 */
	public static boolean isHoneycombTablet(Context context) {
		// Can use static final constants like HONEYCOMB, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed behavior.
		return isHoneycomb() && (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK)
				== Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}
	
	
	public static Bitmap decodeBitmapStream(InputStream is, int desiredWidth, int desiredHeight){
	    Bitmap b = null;
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;

	        BufferedInputStream bis = new BufferedInputStream(is);
	        bis.mark(Integer.MAX_VALUE);
	        
	        BitmapFactory.decodeStream(bis, null, o);
	        bis.reset();
	        
	        int scale = 1;
	        if (o.outHeight > desiredHeight || o.outWidth > desiredWidth) {
	            scale = (int)Math.pow(2, (int) Math.round(Math.log(Math.max(desiredHeight, desiredWidth) / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
	        }

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        o2.inDither = true;
	        o2.inPurgeable = true;
	        
	        b = BitmapFactory.decodeStream(bis, null, o2);
	        bis.close();
	        is.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    return b;
	}

}
