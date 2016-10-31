package com.example.gonza.reproductor;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PlayerService extends Service implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {

	MediaPlayer musicPlayer;
	private final IBinder musicBind = new MusicBinder();
	private List<Song> songs;
	int songPos;
	Song playing; // canción que se está tocando actualmente
	boolean isPause = false;
	List<Activity> observers = new ArrayList<>();

	final int NOTIFICATION_ID = 1;

	@Override
	public void onCreate() {
		super.onCreate();
		musicPlayer = new MediaPlayer();
		initMusicPlayer();
	}

	private void initMusicPlayer() {
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
		boolean newState = false;
		if (position < 0 || position >= songs.size()) {
			musicPlayer.stop();
			playing = null;
			newState = false;
		} else if (position == songPos && songs.get(position) == playing) {
			if (musicPlayer.isPlaying()) {
				musicPlayer.pause();
				isPause = true;
				newState = false;
			} else if (isPause) {
				musicPlayer.start();
				isPause = false;
				newState = true;
			}
		} else {
			songPos = position;
			playing = songs.get(songPos);
			try {
				musicPlayer.reset();
				musicPlayer.setDataSource(getApplicationContext(), playing.getUri());
				musicPlayer.prepareAsync();
				newState = true;
			} catch (Exception e) {
				Log.e("MUSIC SERVICE", "Error setting data source", e);
			}
		}

		emitStateChange(newState);
		emitSongChange(playing);
	}

	public void playPause() {
		if (musicPlayer.isPlaying()) {
			musicPlayer.pause();
			isPause = true;
			emitStateChange(false);
		} else if (isPause) {
			musicPlayer.start();
			isPause = false;
			emitStateChange(true);
		}
	}

	public boolean isPlaying() {
		return musicPlayer.isPlaying();
	}

	public void nextSong() {
		playSong(songPos + 1);
	}

	public void previousSong() {
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
		playSong(songPos + 1);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		System.err.println(String.valueOf(what) + " extra: " + extra);
		return false;
	}

	/**
	 * Setea a los que escuchan los cambios de play/pause.
	 */
	public void onPlayListener(Activity observer) {
		observers.add(observer);
	}

	/*
	 * Avisa que cambio el estado play/pause.
	 * @param play true si cambio a play, false a pause
	 */
	private void emitStateChange(boolean play) {
		for (Activity a: observers) {
			((PlayerActivity) a).onStateChange(play);
		}
	}

	private void emitSongChange(Song song) {
		// updateNotification(song);
		for (Activity a: observers) {
			((PlayerActivity) a).onSongChange(song);
		}
	}

	public Song getPlaying() {
		return playing;
	}

	public class MusicBinder extends Binder {

  		PlayerService getService() {
    		return PlayerService.this;
    	}
    }
}
