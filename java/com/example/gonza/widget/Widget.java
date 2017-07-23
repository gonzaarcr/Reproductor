package com.example.gonza.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.example.gonza.reproductor.PlayerService;
import com.example.gonza.reproductor.R;

public class Widget extends AppWidgetProvider {

	private static final String TAG = "Widget";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		Intent iTellState = new Intent();
		iTellState.setAction(PlayerService.ACTION_TELL_STATE);
		context.sendBroadcast(iTellState);
		init(context, views);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		String event = intent.getAction();

		if (intent.getAction().equals("songChange")) {
			views.setTextViewText(R.id.artist, intent.getStringExtra("artist"));
			views.setTextViewText(R.id.songName, intent.getStringExtra("songName"));
			String uri = intent.getStringExtra("albumArt");
			if (uri != null) {
				views.setImageViewUri(R.id.cover, Uri.parse(uri));
			} else {
				views.setImageViewResource(R.id.cover, R.drawable.default_cover_thumb);
			}
		}
		if (event.equals("stateChange")) {
			PlayerService.State state = (PlayerService.State) intent.getSerializableExtra("state");
			switch (state) {
				case PLAY:
					views.setImageViewResource(R.id.play_pause, R.drawable.ic_media_pause_dark);
					break;
				case PAUSE:
					views.setImageViewResource(R.id.play_pause, R.drawable.ic_media_play_dark);
					break;
				case STOP:
					views.setTextViewText(R.id.artist, "");
					views.setTextViewText(R.id.songName, "");
					views.setImageViewResource(R.id.cover, R.drawable.default_cover_thumb);
					views.setImageViewResource(R.id.play_pause, R.drawable.ic_media_play_dark);
					break;
			}
		}

		// Los botones se tienen que actualizar siempre,
		// asique el init va siempre.
		init(context, views);
		// applyUpdate(context, views, false);
	}

	private void applyUpdate(Context context, RemoteViews views, boolean partial) {
		ComponentName provider = new ComponentName(context, this.getClass());
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		if (partial)
			manager.partiallyUpdateAppWidget(manager.getAppWidgetIds(provider), views);
		else
			manager.updateAppWidget(provider, views);
	}

	private void init(Context context, RemoteViews views) {
		Intent iBackward = new Intent(PlayerService.ACTION_BACKWARD);
		Intent iPlay = new Intent(PlayerService.ACTION_PLAYPAUSE);
		Intent iStop = new Intent(PlayerService.ACTION_STOP);
		Intent iForward = new Intent(PlayerService.ACTION_FORDWARD);

		PendingIntent piBackward = PendingIntent.getBroadcast(context, 0, iBackward, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent piPlay = PendingIntent.getBroadcast(context, 0, iPlay, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent piStop = PendingIntent.getBroadcast(context, 0, iStop, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent piForward = PendingIntent.getBroadcast(context, 0, iForward, PendingIntent.FLAG_UPDATE_CURRENT);

		views.setOnClickPendingIntent(R.id.backward, piBackward);
		views.setOnClickPendingIntent(R.id.play_pause, piPlay);
		views.setOnClickPendingIntent(R.id.stop, piStop);
		views.setOnClickPendingIntent(R.id.forward, piForward);

		applyUpdate(context, views, false);
	}

}
