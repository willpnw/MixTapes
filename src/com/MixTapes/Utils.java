package com.MixTapes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Utils {
	
	private static final String API_KEY = "api_key=a8b13870cc0770c576267c65e0cdaaf1177ab228";
	private static final String MIXES = "http://8tracks.com/mixes";
	private static final String GET_TOKEN = "http://8tracks.com/sets/new";
	private static final String FORMAT = "format=json";
	private static final String PLAY_TOKEN = "play_token";
	private static final String SETS = "http://8tracks.com/sets";
	private static final String NEXT_MIX = "next_mix";
	private static final String RESULTS_PER_PAGE = "per_page=12";
	public static final String OK = "200 OK";
	public static final String RECENT = "recent";
	public static final String POPULAR = "popular";
	public static final String HOT = "hot";
	public static final String RANDOM = "random";
	
	public static String getJSONValue(String jsonString, String value)
	{
		String str = "";
		try
		{
			JSONObject jObject = new JSONObject(jsonString);
			str = jObject.getString(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
		
	}
	
	public static String getPlayToken()
	{
		String cmd = GET_TOKEN + "?" + FORMAT + "&" + API_KEY;
		BufferedReader in = Utils.read8Tracks(cmd);
		String token ="";
		try
		{
			token = Utils.getJSONValue(in.readLine(), Utils.PLAY_TOKEN);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return token;
	}
	
	public static JSONArray getJSONArray(BufferedReader in, String value)
	{
		JSONArray jsonArray = null;
		try
		{
			JSONObject jObject = new JSONObject(in.readLine());
			jsonArray = jObject.getJSONArray(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonArray;
	}
	
	public static String getJSONValue(BufferedReader in, String value)
	{
		String str = "";
		try
		{
			str = getJSONValue(in.readLine(), value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
		
	}

	public static BufferedReader read8Tracks(String command) {
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet();
		BufferedReader reader =null;
        
        
		try {
			URL url = new URL( command );
			URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
			request.setURI(uri);
			HttpResponse response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				reader = new BufferedReader(
						new InputStreamReader(content));
				
			} else {
				Log.e(ListMixesActivity.class.toString(), "Failed to download file");
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reader;
	}
	
	public static String getMixesPage( int page, String searchParams )
	{
		//http://8tracks.com/mixes.xml?page=2
		//curl http://8tracks.com/mixes.xml?tag=jazz
		String cmd = MIXES + "?" + FORMAT + "&" + RESULTS_PER_PAGE;
		if( page > 0 )
		{
			cmd += "&page=" + Integer.toString(page);
		}

		if( !searchParams.isEmpty() )
		{
			boolean sort = 0 == searchParams.compareTo( Utils.HOT )
					|| 0 == searchParams.compareTo( Utils.POPULAR )
					|| 0 == searchParams.compareTo( Utils.RECENT )
					|| 0 == searchParams.compareTo( Utils.RANDOM );
			
			cmd += "&" + ( sort ? "sort=" : "tag=" ) + searchParams;
		}
		cmd += "&" + API_KEY;
		
		return cmd;
	}
	
	public static String getMixesPage( int page )
	{
		return getMixesPage( page, "");
	}
	
	public static String getMixURL(String playInfo )
	{
		//url is in set/track/url
		String val = Utils.getJSONValue(playInfo, "set");
		val = Utils.getJSONValue(val, "track");
		val = Utils.getJSONValue(val, "url");
		return val;
	}
	
	public static String getPlayInfo(String mixId, String token, boolean next, boolean skip )
	{
		String playCommand = ( next ? getNextCommand( token, mixId, skip ) : getPlayCommand( mixId, token ) );
		BufferedReader in = Utils.read8Tracks(playCommand);
		String playInfo = "";
		try
		{
			playInfo = in.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return playInfo;
	}
	
	public static String getNextMixInfo(String mixId, String token )
	{
		String nextMixCommand = getNextMixCommand( token, mixId );
		BufferedReader in = Utils.read8Tracks(nextMixCommand);
		String mixInfo = "";
		try
		{
			mixInfo = in.readLine();
			mixInfo = getJSONValue(mixInfo, "next_mix");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mixInfo;
	}
	
	
	
	public static String getNextUrl( String token, String mixId, boolean skip )
	{
		//http://8tracks.com/sets/460486803/next.xml?mix_id=2000
		String nextCommand = getNextCommand( token, mixId, skip );
		BufferedReader in = Utils.read8Tracks(nextCommand); 
		//url is in set/track/url
		String val = Utils.getJSONValue(in, "set");
		val = Utils.getJSONValue(val, "track");
		val = Utils.getJSONValue(val, "url");
		
		return val;
	}
	
	public static String getPlayCommand( String mixId, String token )
	{
		return SETS + "/" + token + "/play?" + FORMAT + "&mix_id=" + mixId + "&" + API_KEY;
	}
	
	public static String getNextCommand( String token, String mixId, boolean skip )
	{
		return SETS + "/" + token + "/" + (skip ? "skip" : "next" ) + "?" + FORMAT + "&mix_id=" + mixId + "&" + API_KEY;
	}
	
	public static String getNextMixCommand( String token, String mixId )
	{
		return SETS + "/" + token + "/" + NEXT_MIX + "?" + FORMAT + "&mix_id=" + mixId + "&" + API_KEY;
	}
	
	public static String getMixInfo( String mixId )
	{
		String mixInfo = "";
		String mixCmd = MIXES + "/" + mixId + "?" + FORMAT;
		BufferedReader in = Utils.read8Tracks(mixCmd);
		try
		{
			mixInfo = in.readLine();
			mixInfo = getJSONValue(mixInfo, "mix");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mixInfo;
	}
	
	public static String getMixImagePath( String mixInfo, String imageType )
	{
		String imagePath = "";
		
		try
		{	
			imagePath = getJSONValue( mixInfo, "cover_urls" );
			imagePath = getJSONValue( imagePath, imageType );
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return imagePath;
	}
	
	public static String getArtistName( String playInfo )
	{
		String artistName = "";
		
		try
		{	
			artistName = getJSONValue( playInfo, "set" );
			artistName = getJSONValue( artistName, "track" );
			artistName = getJSONValue( artistName, "performer" );
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return artistName;
	}
	
	public static String getDescription( String mixInfo )
	{
		String description = "";
		
		try
		{	
			description = getJSONValue( mixInfo, "description" );
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return description;
	}
	
	public static String getPlayUrl( String playInfo )
	{
		String url = "";
		
		try
		{	
			url = getJSONValue( playInfo, "set" );
			url = getJSONValue( url, "track" );
			url = getJSONValue( url, "url" );
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return url;
	}
	
	public static String getSongName( String mixInfo )
	{
		String songName = "";
		
		try
		{
			songName = getJSONValue( mixInfo, "set" );
			songName = getJSONValue( songName, "track" );
			songName = getJSONValue( songName, "name" );
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return songName;
	}
	
	public static Bitmap getImageBitmap( String imagePath )
	{
		Bitmap bm = null;
        try 
        {
                URL url = new URL(imagePath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bm = BitmapFactory.decodeStream(input);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        	
        }
        return bm;
	}
	
	public static class Mix {
        private String mixInfo = "";
        private String name = "";
        private Bitmap image = null;
        private String description = "";
        private String mixId = "";
        
        public Mix( String mixInfo, String name, Bitmap image, String description, String mixId )
        {
        	this.mixInfo = mixInfo;
        	this.name = name;
        	this.image = image;
        	this.description = description;
        	this.mixId = mixId;
        }
        
        public Mix(){
        }
        
        public String getMixInfo() {
            return mixInfo;
        }
        public void setMixInfo(String mixInfo) {
            this.mixInfo = mixInfo;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Bitmap getImage() {
            return image;
        }
        public void setImage(Bitmap image) {
            this.image = image;
        }
        
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public String getMixId() {
            return mixId;
        }
        public void setMixId(String mixId) {
            this.mixId = mixId;
        }
    }

}
