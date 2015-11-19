package com.zertinteractive.wallpaper.searchs;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageResult {
	private String fullUrl;
	private String thumbUrl;
	
	public ImageResult(JSONObject json) {
		try{
			this.fullUrl = json.getString("url");
			this.thumbUrl = json.getString("tbUrl");
		}catch (JSONException e){
			this.fullUrl = null;
			this.thumbUrl = null;		
		}
	}
	
	public String getFullUrl() {
		return fullUrl;
	}
	public String getThumbUrl() {
		return thumbUrl;
	}
	
	public String toString(){
		return thumbUrl;
	}

	public static ArrayList<ImageResult> fromJSONArray(
			JSONArray imageJsonResults) {
		ArrayList <ImageResult> results = new ArrayList<ImageResult>();
		for (int x=0; x < imageJsonResults.length();x++){
			try{
				results.add(new ImageResult(imageJsonResults.getJSONObject(x)));
			} catch(JSONException e){
				e.printStackTrace();
			}
		}
		return results;
	}
	
	

}
