package com.example.gonza.reproductor;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

public class PlayerService extends Service implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {

	MediaPlayer musicPlayer;
	private final IBinder musicBind = new MusicBinder();
	private List<Song> songs;
	int songPos;
	boolean isPause = false;

	@Override
	public void onCreate() {
		super.onCreate();
		musicPlayer = new MediaPlayer();
		initMusicPlayer();
	}

	public void initMusicPlayer() {
		// The wake lock will let playback continue when the device becomes idle 
		// and we set the stream type to music
		musicPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		musicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		// Set the class as listener for 
		// (1) when the PlayerActivity instance is prepared,
		// (2) when a song has completed playback, and when 
		// (3) an error is thrown
		musicPlayer.setOnPreparedListener(this);
		musicPlayer.setOnCompletionListener(this);
		musicPlayer.setOnErrorListener(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		musicPlayer.stop();
		musicPlayer.release();
		return false;
	}

	public void playSong(int position) {
		if (position == songPos) {
			if (musicPlayer.isPlaying()) {
				musicPlayer.pause();
				isPause = true;
				return;
			} else if (isPause) {
				musicPlayer.start();
				isPause = false;
				return;
			}
		}
		songPos = position;
		Song playSong = songs.get(songPos);
		try {
			musicPlayer.reset();
			musicPlayer.setDataSource(getApplicationContext(), playSong.getUri());
			musicPlayer.prepareAsync();
		} catch (Exception e) {
			Log.e("MUSIC SERVICE", "Error setting data source", e);
		}
	}

	public void playPause() {
		if (musicPlayer.isPlaying()) {
			musicPlayer.pause();
			isPause = true;
		} else if (isPause) {
			musicPlayer.start();
			isPause = false;
		}
	}

	public void nextSong() {
		if (songPos == songs.size() - 1)
			musicPlayer.stop();
		else
			playSong(songPos + 1);
	}

	public void previousSong() {
		if (songPos == 0)
			musicPlayer.stop();
		else
			playSong(songPos - 1);
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
	}

	public void setList(List<Song> theSongs){
		songs = theSongs;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (songPos == songs.size() - 1)
			musicPlayer.stop();
		else
			playSong(songPos + 1);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		System.err.println(String.valueOf(what) + " extra: " + extra);
		return false;
	}

	public class MusicBinder extends Binder {

  		PlayerService getService() {
    		return PlayerService.this;
    	}
    }
}
