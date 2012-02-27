package com.blork.localwall;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;

public class Flickr {
	public static final String TAG = "LocalWall";
    
	public static FlickrImage getImage(Double latitude, Double longitude) throws IOException, JSONException, BadLocationException, URISyntaxException{
	
		Log.i(Main.TAG, "Fetch geocode");

		URL aURL = new URL("http://api.flickr.com/services/rest/?method=" +
						"flickr.places.findByLatLon&" +
						"api_key=" + API.KEY + "&" +
						"nojsoncallback=1&" +
						"lat=" + latitude +
						"&lon=" + longitude +
						"&format=json");


		
		String response = Utils.getStringFromUrl(aURL);
		Log.d("", response);
		JSONObject obj = new JSONObject(response);
	
		JSONArray results = ((JSONObject) obj.get("places")).getJSONArray("place");

		if(results.length() == 0){
			Log.i(Main.TAG, "No places matched!");
			throw new BadLocationException();
		}

		String woeId = ((JSONObject)results.get(0)).getString("woeid");
		String placeName = ((JSONObject)results.get(0)).getString("name");
		Log.i(Flickr.TAG, "Place Name: "+placeName);

		Log.i(Main.TAG, "Fetch urls");
		String content = "";
		aURL = new URL("http://api.flickr.com/services/rest/?method=flickr.photos.search&" +
					   "api_key=" + API.KEY + "&" +
					   "format=json&" +
					   "nojsoncallback=1&" +
					   "woe_id=" + woeId + "&" +
					   "per_page=1&" +
					   "radius=1");
			
		obj = new JSONObject(Utils.getStringFromUrl(aURL));
		Log.i(Flickr.TAG, content);
		results = ((JSONObject) obj.get("photos")).getJSONArray("photo");

		if(results.length() == 0){
			Log.i(Main.TAG, "No places matched!");
			throw new BadLocationException();
		}

		
		JSONObject p = results.getJSONObject(0);
		String title = p.getString("title");
		String owner = p.getString("owner");
		String id = p.getString("id");
		Log.i(Flickr.TAG, "Image title: "+title);
		
		
		content = "";
		aURL = new URL("http://api.flickr.com/services/rest/?method=flickr.photos.getSizes&" +
					   "api_key=" + API.KEY + "&" +
					   "format=json&" +
					   "nojsoncallback=1&" +
					   "photo_id=" + id);

	
		obj = new JSONObject(Utils.getStringFromUrl(aURL));
		Log.i(Flickr.TAG, content);
		results = ((JSONObject) obj.get("sizes")).getJSONArray("size");
		Log.d(Flickr.TAG, results.toString());
		
		int x = results.length();
		
		ArrayList<String> sizes = new ArrayList<String>();
		for(int i = 0; i < x; i++){
			JSONObject size = results.getJSONObject(i);
			if(size.getString("media").equals("photo")){
				sizes.add(size.getString("source"));
				//TODO: dimension check
			}
		}		
		
		
		int count = 1;
		Bitmap wallpaper = null;
		while(wallpaper == null){		
			Log.d(Flickr.TAG, count+", "+sizes.size()+", "+sizes.toString());
			String imageUrl;
			try {
				imageUrl = sizes.get(sizes.size() - count);
			} catch (IndexOutOfBoundsException e1) {
				e1.printStackTrace();
				throw new BadLocationException();
			}
			
			Log.d(Main.TAG, imageUrl);
			URL url = new URL(imageUrl);
			try {
				wallpaper = Flickr.getBitmap(url);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
			
			count++;
		}
		
		return new FlickrImage(wallpaper, placeName, title, id, owner);

	}

		
	
	
    public static Bitmap getBitmap(URL url) throws IOException, OutOfMemoryError{
		URLConnection connection = url.openConnection();
		InputStream instream = connection.getInputStream();
		return Utils.decodeBitmapStream(instream, 480, 800);
    }
    
}
