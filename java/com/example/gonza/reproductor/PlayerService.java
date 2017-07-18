package com.example.gonza.reproductor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.gonza.widget.MyNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PlayerService extends Service implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {

	public static final String APP_PKG = "com.example.gonza";
	public static final String ACTION_BACKWARD = APP_PKG + ".Backward";
	public static final String ACTION_PLAYPAUSE = APP_PKG + ".PlayPause";
	public static final String ACTION_STOP = APP_PKG + ".Stop";
	public static final String ACTION_FORDWARD = APP_PKG + ".Forward";

	private final String TAG = "PlayerService";

	MediaPlayer musicPlayer;
	private final IBinder musicBind = new MusicBinder();
	private Vector<Song> songs = new Vector<>();
	int songPos;
	Song playing; // canción que se está tocando actualmente
	State currentState = State.STOP;
	List<ServiceCallback> callbacks = new ArrayList<>();

	MyNotification notification;

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case ACTION_BACKWARD: previousSong(); break;
				case ACTION_PLAYPAUSE: playPause(); break;
				case ACTION_STOP: stop(); break;
				case ACTION_FORDWARD: nextSong(); break;
			}
		}
	};

	public interface ServiceCallback {
		void onStateChange(PlayerService.State newState);
		void onSongChange(Song newSong);
	}

	public enum State {
		PLAY, PAUSE, STOP
	}

	@Override
	public void onCreate() {
		super.onCreate();
		musicPlayer = new MediaPlayer();
		initMusicPlayer();

		// Registra el broadcast receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_BACKWARD);
		filter.addAction(ACTION_PLAYPAUSE);
		filter.addAction(ACTION_STOP);
		filter.addAction(ACTION_FORDWARD);
		registerReceiver(broadcastReceiver, filter);
	}

	private void initMusicPlayer() {
		musicPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		musicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
		State newState = State.STOP;
		if (position < 0 || position >= songs.size()) {
			musicPlayer.stop();
			playing = null;
			newState = State.STOP;
		} else if (position == songPos && songs.get(position) == playing) {
			musicPlayer.seekTo(0);
			musicPlayer.start();
			newState = State.PLAY;
		} else {
			songPos = position;
			playing = songs.get(songPos);
			try {
				musicPlayer.reset();
				musicPlayer.setDataSource(getApplicationContext(), playing.getUri());
				musicPlayer.prepareAsync();
				newState = State.PLAY;
			} catch (Exception e) {
				Log.e(TAG, "Error setting data source", e);
			}
		}
		currentState = newState;
		emitSongChange(playing);
		emitStateChange(newState);
	}

	public void playPause() {
		if (musicPlayer.isPlaying()) {
			musicPlayer.pause();
			currentState = State.PAUSE;
			emitStateChange(currentState);
		} else if (currentState == State.PAUSE) {
			musicPlayer.start();
			currentState = State.PLAY;
			emitStateChange(currentState);
		} else if (currentState == State.STOP) {
			playSong(songPos);
		}
	}

	public void stop() {
		musicPlayer.stop();
		playing = null;
		currentState = State.STOP;
		emitSongChange(playing);
		emitStateChange(currentState);
	}

	public State getState() {
		return currentState;
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

	public void setList(Vector<Song> songs) {
		this.songs = songs;
		songPos = 0;
	}

	public void appendSongs(Vector<Song> list) {
		songs.addAll(list);
	}

	public void removeSong(int position) {
		songs.remove(position);
		if (position <= songPos)
			songPos--;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		playSong(songPos + 1);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e(TAG, String.valueOf(what) + " extra: " + extra);
		return false;
	}

	/**
	 * Setea a los que escuchan los cambios de play/pause.
	 */
	public void onPlayListener(ServiceCallback callback) {
		callbacks.add(callback);
	}

	/*
	 * Avisa que cambio el estado play/pause.
	 * @param play true si cambio a play, false a pause
	 */
	private void emitStateChange(State state) {
		for (ServiceCallback c: callbacks) {
			c.onStateChange(state);
		}
	}

	/**
	 * Avisa que cambió la canción a song. Puede que sea null,
	 * (se detuvo).
	 * @param song canción nueva
	 */
	private void emitSongChange(Song song) {
		for (ServiceCallback c: callbacks) {
			c.onSongChange(song);
		}
		updateNotification(song);
	}

	private void updateNotification(Song newSong) {
		if (newSong == null) {
			stopForeground(true);
			callbacks.remove(notification);
			notification = null;
			return;
		} else {
			if (notification == null) {
				notification = new MyNotification(this, newSong);
				callbacks.add(notification);
			}
			startForeground(notification.getId(), notification.build());
		}
	}

	public class MusicBinder extends Binder {

  		PlayerService getService() {
    		return PlayerService.this;
    	}
    }
}
