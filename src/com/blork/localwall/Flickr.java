package com.blork.localwall;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class Flickr {
	public static final String TAG = "LocalWall";

	public Bitmap wallpaper;
	public String placeName;

	public String title;

	public String owner;

	public String id;


    
	public void getImage(Double latitude, Double longitude) throws IOException, JSONException, BadLocationException{
	
		URL aURL;
		BufferedReader r;
		String data, content = "";
		JSONObject obj;
		JSONArray results;
		String woeId;
		JSONObject p;
		String imageUrl;
		InputStream is;
		
		Log.i(Main.TAG, "Fetch geocode");

		aURL = new URL("http://api.flickr.com/services/rest/?method=" +
						"flickr.places.findByLatLon&" +
						"api_key=" + API.KEY + "+&" +
						"nojsoncallback=1&" +
						"lat=" + latitude +
						"&lon=" + longitude +
						"&format=json");
		is = Flickr.getStream(aURL);
		r = new BufferedReader(new InputStreamReader(is));
		while( (data = r.readLine()) != null) { content += data; }
		is.close();

		Log.i(Main.TAG, "Extract woeid");

		obj = new JSONObject(content);
	
		results = ((JSONObject) obj.get("places")).getJSONArray("place");

		if(results.length() == 0){
			Log.i(Main.TAG, "No places matched!");
			throw new BadLocationException();
		}

		woeId = ((JSONObject)results.get(0)).getString("woeid");
		placeName = ((JSONObject)results.get(0)).getString("name");
		Log.i(Flickr.TAG, "Place Name: "+placeName);

		Log.i(Main.TAG, "Fetch urls");
		content = "";
		aURL = new URL("http://api.flickr.com/services/rest/?method=flickr.photos.search&" +
					   "api_key=e085ffa6331b69126b3890683ded0681&" +
					   "format=json&" +
					   "nojsoncallback=1&" +
					   "woe_id=" + woeId + "&" +
					   "per_page=1&" +
					   "radius=1");
		
		try {
			is = Flickr.getStream(aURL);
		} catch (Exception e2) {
			System.gc();
			is = Flickr.getStream(aURL);
		}
		
		r = new BufferedReader(new InputStreamReader(is));
		while( (data = r.readLine()) != null) { content += data; }
		is.close();

		Log.i(Main.TAG, "JSON urls");
	
		obj = new JSONObject(content);
		Log.i(Flickr.TAG, content);
		results = ((JSONObject) obj.get("photos")).getJSONArray("photo");

		if(results.length() == 0){
			Log.i(Main.TAG, "No places matched!");
			throw new BadLocationException();
		}

		
		p = results.getJSONObject(0);
		title = p.getString("title");
		owner = p.getString("owner");
		id = p.getString("id");
		Log.i(Flickr.TAG, "Image title: "+title);
		
		
		//getsizes
		//http://api.flickr.com/services/rest/?method=flickr.photos.getSizes&api_key=e085ffa6331b69126b3890683ded0681&format=json&nojsoncallback=1&photo_id=4911402982_ffa2168ff8
		content = "";
		aURL = new URL("http://api.flickr.com/services/rest/?method=flickr.photos.getSizes&" +
					   "api_key=e085ffa6331b69126b3890683ded0681&" +
					   "format=json&" +
					   "nojsoncallback=1&" +
					   "photo_id=" + id);
		is = Flickr.getStream(aURL);
		r = new BufferedReader(new InputStreamReader(is));
		while( (data = r.readLine()) != null) { content += data; }
		is.close();

		Log.i(Main.TAG, "JSON urls");
	
		obj = new JSONObject(content);
		Log.i(Flickr.TAG, content);
		results = ((JSONObject) obj.get("sizes")).getJSONArray("size");
		Log.e(Flickr.TAG, results.toString());
		
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
		while(this.wallpaper == null){		
			Log.e(Flickr.TAG, count+", "+sizes.size()+", "+sizes.toString());
			
			try {
				imageUrl = sizes.get(sizes.size() - count);
			} catch (IndexOutOfBoundsException e1) {
				e1.printStackTrace();
				throw new BadLocationException();
			}
			
			Log.d(Main.TAG, imageUrl);
			URL url = new URL(imageUrl);
			try {
				this.wallpaper = Flickr.getBitmap(url);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
			
			count++;
		}

	}

		
	
	
    public static Bitmap getBitmap(URL url) throws IOException, OutOfMemoryError{
    	    	
     	InputStream instream = Flickr.getStream(url);
		
     	Bitmap bitmap;
     	
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = true;
		
		try {
			bitmap = BitmapFactory.decodeStream(instream, null, options);
			return bitmap;
		} catch (OutOfMemoryError t) {
			t.printStackTrace();
			instream = url.openStream();
			options.inSampleSize = 4;
			bitmap = BitmapFactory.decodeStream(instream, null, options);
			return bitmap;
		}finally{
			instream.close();
		}

    }
    
	public void save(FileOutputStream fileOutputStream) throws IOException, SaveToStorageException, OutOfMemoryError{
		Log.d(Flickr.TAG, "Saving info.");
	
		Log.i(Flickr.TAG, "Saving image to internal storage");
				
		try {
			BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
			this.wallpaper.compress(CompressFormat.JPEG, 75, bos);
			bos.flush();
			bos.close();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(Flickr.TAG, "Problem saving image");
			throw new SaveToStorageException();
		}	
	}
	
    private static InputStream getStream(URL url) throws ClientProtocolException, IOException{
   	 HttpGet httpRequest = null;

        try {
        	httpRequest = new HttpGet(url.toURI());
        	httpRequest.removeHeaders("User-Agent");
        	httpRequest.setHeader("Accept-Encoding", "gzip");
        	httpRequest.setHeader( "Pragma", "no-cache" );
        	httpRequest.setHeader( "Cache-Control", "no-cache" );
        	httpRequest.setHeader( "Expires", "0" );
        } catch (URISyntaxException e) {
        	e.printStackTrace();
        }

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
		
		HttpEntity entity = response.getEntity();
		BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity); 
		InputStream instream = bufHttpEntity.getContent();
		
		Header contentEncoding = response.getFirstHeader("Content-Encoding");
		
		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
		    instream = new GZIPInputStream(instream);
		    Log.d(Flickr.TAG, "Gzipped");
		}
		
		return instream;
   }
}
