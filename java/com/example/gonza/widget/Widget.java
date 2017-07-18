package com.example.gonza.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.gonza.reproductor.R;

public class Widget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		init(context, views);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		applyUpdate(context, views, false);
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
		applyUpdate(context, views, false);
	}

}
