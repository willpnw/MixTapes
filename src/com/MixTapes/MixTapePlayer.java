package com.MixTapes;


import com.MixTapes.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MixTapePlayer extends Activity implements OnCompletionListener, OnPreparedListener {

	private ProgressDialog m_pd;
	private boolean m_configChange = false;
	
	/* state - 0 for unplugged, 1 for plugged.
	 * name - Headset type, human readable string
	 * microphone - 1 if headset has a microphone, 0 otherwise
	 */
	private int m_headSetState = 0;
	private MediaPlayerState m_playerState;
	private MediaPlayerState m_nextPlayerState = null;
	private Context m_cntx;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.media_player);
		m_cntx = this;
		
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		
		ViewState vs = (ViewState) getLastNonConfigurationInstance();
		if (vs == null || null == vs.currentPlayerState ) {
	    	String mixInfo = extras.getString("mixInfo");
	    	String mixId = Utils.getJSONValue( mixInfo, "id" );
			String token = Utils.getPlayToken();
			String playInfo = Utils.getPlayInfo( mixId, token, false, false );
			MediaPlayer mp = new MediaPlayer();
			mp.setOnPreparedListener(this);
	    	m_playerState = new MediaPlayerState( mixInfo, mixId, token, playInfo, mp);
	    	m_pd = ProgressDialog.show( m_cntx, "", "Getting muzaks...", true );
	    	initPlayer( m_playerState.player, m_playerState.playInfo );
	    }
		else
		{
			m_playerState = vs.currentPlayerState;
			if( null == vs.nextPlayerState ) {
				setupNextPlayer();
			}
			else {
				m_nextPlayerState = vs.nextPlayerState;
			}
		}

		this.registerReceiver(new HeadSetEventReceiver(), new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
		playButton.setImageResource( m_playerState.isReady && m_playerState.player.isPlaying() ? R.drawable.pause : R.drawable.play );
		playButton.setOnClickListener( onPlay );
		
		ImageButton skipButton = (ImageButton) findViewById(R.id.skipButton);
		skipButton.setOnClickListener( onSkip );
	    
		ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);
		nextButton.setOnClickListener( onNext );
	    
		setupView();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		m_configChange = true;
		ViewState vs = null;
		
		//Don't bother saving state unless the player is playing
		if( null != m_playerState ) {
			if( m_playerState.isReady ) {	
				vs = new ViewState( m_playerState, null );
			} else {
				m_playerState.player.release();
			}
		}
		
		if( null != m_nextPlayerState ) {
			if( m_nextPlayerState.isReady ) {
				vs.nextPlayerState = m_nextPlayerState;
			} else {
				m_nextPlayerState.player.release();
			}
		}
		return vs;
	}
	
	public void onDestroy() {
		if( !m_configChange ) {
			if( null != m_playerState ) m_playerState.player.release();
			if( null != m_nextPlayerState ) m_nextPlayerState.player.release();
		}
		super.onDestroy();
	}
	
	private void setupView() {	
		TextView mixName = (TextView) findViewById(R.id.mixName);
		
		mixName.setText( Utils.getJSONValue(m_playerState.mixInfo, "name") );
		
		TextView description = (TextView) findViewById(R.id.description);
		
		description.setText( Utils.getJSONValue(m_playerState.mixInfo, "description") );
		
		setSongAndArtist( m_playerState.playInfo );
		
		ImageView iv = (ImageView) findViewById(R.id.mixImage);
		String imagePath = Utils.getMixImagePath( m_playerState.mixInfo, "max200" );
		iv.setImageBitmap( Utils.getImageBitmap( imagePath ) );
	}
	
	private void initPlayer( MediaPlayer mp, String playInfo )
	{	
		
		try
		{
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mp.setOnCompletionListener(this);
			mp.reset();
			mp.setDataSource(Utils.getMixURL(playInfo));
			mp.prepareAsync();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private OnClickListener onPlay = new OnClickListener() {
		public void onClick(View v) {
			
			if( !m_playerState.isReady )
				return;
			
			boolean pausing = m_playerState.player.isPlaying();
			
			if( pausing )
			{ 
				m_playerState.player.pause();
			}
			else
			{
				m_playerState.player.start();
			}
			
			ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
			playButton.setImageResource( pausing ? R.drawable.play : R.drawable.pause);
		}
	};  
	  
	private OnClickListener onSkip = new OnClickListener() {
		public void onClick(View v) {
			playNext( true );
		}
	};
	
	private OnClickListener onNext = new OnClickListener() {
		public void onClick(View v) {
			nextMix();
		}
	};
	
	public void nextMix(){
		
		m_pd = ProgressDialog.show( m_cntx, "", "Getting muzaks...", true );
		if( null != m_playerState ) {
			if( null != m_playerState.player ) {
				m_playerState.player.release();
				m_playerState.player = null;
			}
			
			if( null != m_nextPlayerState && null != m_nextPlayerState.player ) {
				m_nextPlayerState.player.release();
				m_nextPlayerState.player = null;
				m_nextPlayerState = null;
			}
		
			String nextMixInfo = Utils.getNextMixInfo(m_playerState.mixId, m_playerState.token);
			String nextMixId = Utils.getJSONValue(nextMixInfo, "id");
			String nextPlayInfo = Utils.getPlayInfo( nextMixId, m_playerState.token, false, false );

			MediaPlayer mp = new MediaPlayer();
			mp.setOnPreparedListener(this);
			m_playerState = new MediaPlayerState( nextMixInfo, nextMixId, m_playerState.token, nextPlayInfo, mp);
			setupView();
			initPlayer( m_playerState.player, nextPlayInfo );
		}
		
	}

	@Override
	public void onCompletion(MediaPlayer mp )
	{	
		playNext( false );
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		if( null != m_pd && m_pd.isShowing() ) m_pd.dismiss();
    	
    	// if the state isn't setup, it means we skipped and set the current to next 
		// while it was still preparing and couldn't start it then
		// so start it up now
    	if( mp == m_playerState.player ) {
    		
   			m_playerState.isReady = true;
   			m_playerState.player.start();
   			ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
			playButton.setImageResource(R.drawable.pause);
			 	setupNextPlayer();
    	}
    	else{
    		m_nextPlayerState.isReady = true;
    	}
	}
	
	public void setupNextPlayer()
	{	
		String nextPlayInfo = Utils.getPlayInfo(m_playerState.mixId, m_playerState.token, true, false);
		String nextMixInfo = "";
		String nextMixId = "";
		if( 0 == Utils.getJSONValue(Utils.getJSONValue(nextPlayInfo, "set"), "at_end").compareTo( "true" )) {
			nextMixInfo = Utils.getNextMixInfo(m_playerState.mixId, m_playerState.token);
			nextMixId = Utils.getJSONValue(nextMixInfo, "id");
			nextPlayInfo = Utils.getPlayInfo( nextMixId, m_playerState.token, false, false );
		}
		else {
			nextMixInfo = m_playerState.mixInfo;
			nextMixId = m_playerState.mixId;
		}
		MediaPlayer mp = new MediaPlayer();
		mp.setOnPreparedListener(this);
		m_nextPlayerState = new MediaPlayerState( nextMixInfo, nextMixId, m_playerState.token, nextPlayInfo, mp);
		initPlayer( m_nextPlayerState.player, nextPlayInfo );
	}
	
	public void setSongAndArtist( String playInfo )
	{
		TextView artistName = (TextView) findViewById(R.id.artistName);
		artistName.setText( Utils.getArtistName( playInfo) );
		
		TextView songName = (TextView) findViewById(R.id.songName);
		songName.setText( Utils.getSongName( playInfo) );
	}
	
	public void playNext( boolean skip )
	{	
		if( skip && 0 != Utils.getJSONValue(m_nextPlayerState.playInfo, "status").compareTo( Utils.OK ) )
		{
			Toast.makeText(this, "Weak, you already used all your skips :(" , 1).show();
			return;
		}
			
		try
		{
			if( null != m_nextPlayerState ) {
				m_playerState.player.release();
				boolean sameMix = 0 == m_playerState.mixId.compareTo( m_nextPlayerState.mixId );
					
				m_playerState = m_nextPlayerState;
				m_nextPlayerState = null;
				if( sameMix ) {
					setSongAndArtist( m_playerState.playInfo );
				} else {
					setupView();
				}
				
				if( m_playerState.isReady ) {
					m_playerState.player.start();
					ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
					playButton.setImageResource(R.drawable.pause);
					setupNextPlayer();
				}
				else {
					m_pd = ProgressDialog.show( m_cntx, "", "Getting muzaks...", true );
				}
			}
				
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private class ViewState {
		
		public MediaPlayerState currentPlayerState = null;
		public MediaPlayerState nextPlayerState = null;
		
		public ViewState( MediaPlayerState currentPlayerState, MediaPlayerState nextPlayerState) {
			this.currentPlayerState = currentPlayerState;
			this.nextPlayerState = nextPlayerState;
		}
	}
	
	private class MediaPlayerState {
        
		public String mixInfo;
		public String mixId;
        public String token;
        public String playInfo;
        public MediaPlayer player;
    	private boolean isReady = false;
    	
        public MediaPlayerState( String mixInfo, String mixId, String token, String playInfo, MediaPlayer player ){	
        	this.mixInfo = mixInfo;
        	this.mixId = mixId;
        	this.token = token;
        	this.playInfo = playInfo;
        	this.player = player;
        }
    }
	
	private class HeadSetEventReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				int state = extras.getInt("state");
				
				//if we're going from headset plugged in to unplugged, pause
				if ( 1 == m_headSetState && 0 == state && m_playerState.player.isPlaying()) {
					onPlay.onClick(null);
				}
				
				m_headSetState = state;
				
			}
		}
	}
}