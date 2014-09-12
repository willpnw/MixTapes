package com.MixTapes;


import java.util.ArrayList;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;

import com.MixTapes.R;

public class ListMixesActivity extends ListActivity {
	/** Called when the activity is first created. */
	
	private ArrayList<Utils.Mix> m_mixes = null;
	private MixAdapter m_adapter = null;
	private String m_searchParams = "";
	private boolean m_loading = false;
	private int m_visibleThreshold = 20;
	private Context m_cntx;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_cntx = this;
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		
	    m_searchParams = extras.getString("SearchParams");
		
	    m_mixes = new ArrayList<Utils.Mix>();
	    m_adapter = new MixAdapter(m_cntx, R.layout.list_item, m_mixes);
		setListAdapter(m_adapter);
		ListView lv = ( (ListActivity) m_cntx).getListView();
		lv.setOnScrollListener( new EndlessScrollListener() );
		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
		    public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
		    	Toast.makeText(getBaseContext(), m_mixes.get(pos).getDescription(), 1).show();
		    	return true;
		    }
		});
	    LoadNewMixes();
	}
	
	private void LoadNewMixes()
	{
		if( !m_loading )
		{
			m_loading = true;
			new LoadItemsTask().execute( new PageTag( (int)Math.floor(m_mixes.size()/12) + 1, m_searchParams ) );
		}
	}
	
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if( position >= m_mixes.size() ) return;

		Intent myIntent = new Intent( this, MixTapePlayer.class );
		myIntent.putExtra("mixInfo", m_mixes.get(position).getMixInfo());
		startActivity(myIntent);
	}
	
	private class MixAdapter extends ArrayAdapter<Utils.Mix> {
		
		private View m_loadingView = null;
        public MixAdapter(Context context, int textViewResourceId, ArrayList<Utils.Mix> items) {
                super(context, textViewResourceId, items);
        }
        
        public int getCount(){
        	return super.getCount() + 1;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
            
        	if( position >= super.getCount() ) {
        		if( null == m_loadingView ) {
        			convertView = vi.inflate(R.layout.loading, null);
        			
        			String loadText = "Getting some ";
        			loadText += m_searchParams.isEmpty() ? "some mix tapes..." : m_searchParams + " mixes..." ;
        			TextView loadingText = (TextView) convertView.findViewById(R.id.loadingText);
	        		loadingText.setText( loadText );
        			
        			ImageView loadingSpinner = (ImageView) convertView.findViewById(R.id.loadingSpinner);
	        		Animation a = AnimationUtils.loadAnimation(convertView.getContext(), R.anim.spinner);
	        	    a.reset();
	        	    loadingSpinner.clearAnimation();
	        	    loadingSpinner.setAnimation(a);
	        	    m_loadingView = convertView;
        		}
        		convertView = m_loadingView;
        	} else {
        		convertView = vi.inflate(R.layout.list_item, null);
        		
        		TextView mixName = (TextView) convertView.findViewById(R.id.mixName);
            	ImageView thumbNail = (ImageView) convertView.findViewById(R.id.thumbNail);
            	TextView description = (TextView) convertView.findViewById(R.id.mixDescription);
            	
        		thumbNail.setImageBitmap(getItem( position ).getImage());
	            mixName.setText(getItem( position ).getName());
	            description.setText(getItem( position ).getDescription());
        	}
        	
            return convertView;
        }
	}
	
	private class LoadItemsTask extends AsyncTask<PageTag, Void, Void> {
        
		private ArrayList<Utils.Mix> newMixes = new ArrayList<Utils.Mix>();
        
		@Override
		protected Void doInBackground(PageTag... params) {
			try
			{
				newMixes.addAll( MixTape.getData( params[0].page, params[0].searchParams ) );
			}
			catch(Exception e) 
			{
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
        protected void onPostExecute(Void result) {
			m_mixes.addAll( newMixes );
            m_adapter.notifyDataSetChanged();
            m_loading = false;
        }
    }
	
	private class PageTag
	{
		public int page = 0;
		public String searchParams = "";
		public PageTag( int page, String searchParams )
		{
			this.page = page;
			this.searchParams = searchParams;			
		}
	}
	
	private class EndlessScrollListener implements OnScrollListener {
		
	    public void onScroll(AbsListView view, int firstVisibleItem,
	            int visibleItemCount, int totalItemCount) {
	        
	        if ( firstVisibleItem >= totalItemCount - m_visibleThreshold ) {
	        	LoadNewMixes();
	        }
	    }
	 
	    public void onScrollStateChanged(AbsListView view, int scrollState) {
	    }
	}  
}