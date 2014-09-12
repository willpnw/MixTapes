package com.MixTapes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TagSelectorActivity extends ListActivity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Create an array of Strings, that will be put to our ListActivity
		List<String> tags = Arrays.asList( 
				"indie rock", 
				"sex", 
				"study", 
				"party", 
				"dance", 
				"workout", 
				"dubstep", 
				"80s", 
				"pop", 
				"electronic", 
				"r&b", 
				"indie", 
				"chill", 
				"rap", 
				"punk", 
				"soundtrack", 
				"disney", 
				"acoustic", 
				"classical", 
				"jazz", 
				"blues", 
				"rock", 
				"alternative rock", 
				"alternative", 
				"soul", 
				"indie pop", 
				"country", 
				"love", 
				"happy", 
				"classic rock", 
				"metal", 
				"reggae", 
				"electro", 
				"singer/songwriter", 
				"house", 
				"ambient", 
				"world", 
				"electric", 
				"summer", 
				"folk rock", 
				"lo-fi", 
				"oldies", 
				"funk", 
				"techno", 
				"sad", 
				"electronica", 
				"melow", 
				"relax", 
				"punk rock", 
				"remix", 
				"fun", 
				"trip-hop", 
				"ska", 
				"awesome", 
				"bass", 
				"upbeat", 
				"emo", 
				"90s", 
				"pop punk", 
				"cover", 
				"indie-rock", 
				"j-pop",
				"trance",
				"dub",
				"mashup"
				);
		
		
		Collections.sort(tags);
		
		ArrayList<String> tagList = new ArrayList<String>();
		tagList.add( Utils.POPULAR );
		tagList.add( Utils.HOT );
		tagList.add( Utils.RECENT );
		tagList.add( Utils.RANDOM );

		for( int i = 0; i < tags.size(); i ++ ) {
			tagList.add(tags.get(i));
		}
		
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tagList));
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent= new Intent( this, ListMixesActivity.class );
		intent.putExtra("SearchParams", this.getListAdapter().getItem(position).toString() );
		startActivity( intent );
		
	}
}
