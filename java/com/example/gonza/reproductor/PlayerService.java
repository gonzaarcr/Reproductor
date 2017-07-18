package com.example.gonza.reproductor;

import android.app.PendingIntent;
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
import java.util.Vector;

public class PlayerService extends Service implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {

	private final String TAG = "PlayerService";

	MediaPlayer musicPlayer;
	private final IBinder musicBind = new MusicBinder();
	private Vector<Song> songs = new Vector<>();
	int songPos;
	Song playing; // canción que se está tocando actualmente
	State currentState = State.STOP;
	List<ServiceCallback> callbacks = new ArrayList<>();

	public interface ServiceCallback {
		void onStateChange(PlayerService.State newState);
		void onSongChange(Song newSong);
	}

	public enum State {
		PLAY, PAUSE, STOP
	}

	private int NOTIFICATION_ID = 1;

	@Override
	public void onCreate() {
		super.onCreate();
		musicPlayer = new MediaPlayer();
		initMusicPlayer();
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
			musicPlayer.reset();
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
		emitStateChange(newState);
		updateNotification(playing);
		emitSongChange(playing);
	}

	public void playPause() {
		if (musicPlayer.isPlaying()) {
			musicPlayer.pause();
			currentState = State.PAUSE;
			emitStateChange(currentState);
		} else if (currentState == State.PAUSE || currentState == State.STOP) {
			musicPlayer.start();
			currentState = State.PLAY;
			emitStateChange(currentState);
		} else {
			Log.e(TAG, "Busy State");
		}
	}

	public void stop() {
		musicPlayer.stop();
		playing = null;
		currentState = State.STOP;
		emitStateChange(currentState);
		emitSongChange(playing);
		updateNotification(playing);
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
	}

	private void updateNotification(Song newSong) {
		if (newSong == null) {
			stopForeground(true);
			return;
		}
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(R.mipmap.ic_launcher)
						.setContentTitle(newSong.getTitle())
						.setContentText("From "+ newSong.getAlbum() +" by "+ newSong.getArtist());
		Intent notifyIntent = new Intent(this, PlayerActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
		mBuilder.setContentIntent(pendingIntent);
		startForeground(NOTIFICATION_ID, mBuilder.build());
	}

	public class MusicBinder extends Binder {

  		PlayerService getService() {
    		return PlayerService.this;
    	}
    }
}
