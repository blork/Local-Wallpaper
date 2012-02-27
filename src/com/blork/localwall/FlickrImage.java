package com.blork.localwall;

import android.graphics.Bitmap;

public class FlickrImage {
	private Bitmap bitmap;
	private String placename;
	private String title;
	private String id;
	private String owner;
	
	public FlickrImage(Bitmap bitmap, String placename, String title,
			String id, String owner) {
		super();
		this.bitmap = bitmap;
		this.placename = placename;
		this.title = title;
		this.id = id;
		this.owner = owner;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public String getPlacename() {
		return placename;
	}

	public String getTitle() {
		return title;
	}

	public String getId() {
		return id;
	}

	public String getOwner() {
		return owner;
	}
	
	
}
