package com.MixTapes;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.json.JSONArray;

import android.graphics.Bitmap;

public class MixTape {
	
	public static ArrayList<Utils.Mix> getData(int page, String searchParams) {
	    ArrayList<Utils.Mix> mixes = new ArrayList<Utils.Mix>();
		
		String last10Cmd = Utils.getMixesPage(page, searchParams);
		BufferedReader in = Utils.read8Tracks(last10Cmd); 
		JSONArray mixArray = Utils.getJSONArray( in, "mixes");
		String mixInfo;
		String name = "";
		String imagePath = "";
		String description = "";
		String mixId = "";
		for( int i = 0; i < mixArray.length(); i ++)
		{
			try
			{	
				mixInfo = Utils.getMixInfo( mixArray.getJSONObject(i).getString("id"));
				name = mixArray.getJSONObject(i).getString("name").toString();
				description = mixArray.getJSONObject(i).getString("description").toString();
				mixId = mixArray.getJSONObject(i).getString("id").toString();
				imagePath = mixArray.getJSONObject(i).getString("cover_urls").toString();
				imagePath = Utils.getJSONValue(imagePath, "sq56");
				Bitmap image = Utils.getImageBitmap( imagePath );
				
				
				mixes.add( new Utils.Mix( mixInfo, name, image, description, mixId ) );
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
       
        return mixes;
    }
}
