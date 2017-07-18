package com.example.gonza.widget;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.example.gonza.reproductor.PlayerActivity;
import com.example.gonza.reproductor.PlayerService;
import com.example.gonza.reproductor.R;
import com.example.gonza.reproductor.Song;

import static com.example.gonza.reproductor.PlayerService.ACTION_BACKWARD;
import static com.example.gonza.reproductor.PlayerService.ACTION_FORDWARD;
import static com.example.gonza.reproductor.PlayerService.ACTION_PLAYPAUSE;
import static com.example.gonza.reproductor.PlayerService.ACTION_STOP;

public class MyNotification implements PlayerService.ServiceCallback {
;
	private Context context;

	private NotificationCompat.Builder mBuilder;
	private int NOTIFICATION_ID = 1;

	/**
	 * Construye una notificación lista para usarse. No es necesario llamar a los otros
	 * métodos luego de construirla.
	 * @param context
	 * @param song
	 */
	public MyNotification(Context context, Song song) {
		this.context = context;
		PlayerService.State state = PlayerService.State.PLAY;

		mBuilder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle(song.getTitle())
				.setContentText("From "+ song.getAlbum() +" by "+ song.getArtist());

		Intent notifyIntent = new Intent(context, PlayerActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0);
		mBuilder.setContentIntent(pendingIntent);

		Intent iBackward = new Intent(ACTION_BACKWARD);
		Intent iPlay = new Intent(ACTION_PLAYPAUSE);
		// Intent iStop = new Intent(ACTION_STOP); // El stop no entra, solo muestra 3
		Intent iForward = new Intent(ACTION_FORDWARD);

		PendingIntent piBackward = PendingIntent.getBroadcast(context, 0, iBackward, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent piPlay = PendingIntent.getBroadcast(context, 0, iPlay, PendingIntent.FLAG_UPDATE_CURRENT);
		// PendingIntent piStop = PendingIntent.getBroadcast(context, 0, iStop, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent piForward = PendingIntent.getBroadcast(context, 0, iForward, PendingIntent.FLAG_UPDATE_CURRENT);

		setCover(song);

		mBuilder.addAction(R.drawable.ic_media_backward, "previous", piBackward);
		mBuilder.addAction(R.drawable.ic_media_pause, "pause", piPlay);
		mBuilder.addAction(R.drawable.ic_media_fordward, "next", piForward);
		// mBuilder.addAction(R.drawable.ic_media_stop, "stop", piStop);
	}

	/**
	 * Código asqueroso por cuestiones de compatibilidad. Es esto o hacer la
	 * notificación devuelta desde cero solamente por un botón.
	 * @param state
	 */
	@Override
	public void onStateChange(PlayerService.State state) {
		int i = 0;
		for (; i < mBuilder.mActions.size(); i++) {
			String title = mBuilder.mActions.get(i).title.toString();
			if (title.equals("play") || title.equals("pause"))
				break;
		}

		if (state == PlayerService.State.PLAY) {
			mBuilder.mActions.get(i).title = "pause";
			mBuilder.mActions.get(i).icon = R.drawable.ic_media_pause;
		} else if (state == PlayerService.State.PAUSE) {
			mBuilder.mActions.get(i).title = "play";
			mBuilder.mActions.get(i).icon = R.drawable.ic_media_play;
		}

		NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	@Override
	public void onSongChange(Song song) {
		if (song == null)
			return;

		mBuilder.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle(song.getTitle())
				.setContentText("From "+ song.getAlbum() +" by "+ song.getArtist());

		setCover(song);
	}

	public int getId() {
		return NOTIFICATION_ID;
	}

	public Notification build() {
		return mBuilder.build();
	}

	private void setCover(Song song) {
		if (song.getAlbumArt() != null) {
			mBuilder.setLargeIcon(BitmapFactory.decodeFile(song.getAlbumArt()));
		} else {
			mBuilder.setLargeIcon(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.default_cover_thumb));
		}
	}
}
