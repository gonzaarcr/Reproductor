package com.example.gonza.providers;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.gonza.reproductor.LyricsActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Obtiene las letras de Internet.
 */
public abstract class ProviderBase {

	private final String TAG = "ProviderBase";

	protected LyricsActivity context;
	protected ProviderBase localCache;

	public ProviderBase() { }

	public ProviderBase(LyricsActivity context) {
		this.context = context;
		localCache = new LocalProvider(context);
	}

	/**
	 *
	 * @param artist
	 * @param track
	 * @return true si fue enviada la petición, false si hubo algún problema.
	 * Puede ser true y haber fallo en el server.
	 */
	public boolean getLyrics(String artist, String track) {
		if (artist == null || track == null)
			return false;

		if (localCache.getLyrics(artist, track)) {
			Log.d(TAG, "Cache hit");
			return true;
		}
		Log.d(TAG, "Cache miss");

		// TODO pasar la cola a segundo plano
		// https://developer.android.com/training/volley/index.html
		String requestURL = getUrl(artist, track);
		Log.d(TAG, "URL: " + requestURL);
		final String a = artist;
		final String t = track;
		RequestQueue queue = Volley.newRequestQueue(context);
		StringRequest stringRequest = new StringRequest(Request.Method.GET, requestURL,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						String lyric = parseResponse(response);
						context.setLyrics(lyric);
						saveInCache(a, t, lyric);
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				context.setLyrics("Error: " + error);
			}
		});
		queue.add(stringRequest);
		return true;
	}

	protected abstract String getUrl(String artist, String track);

	protected abstract String parseResponse(String response);

	private void saveInCache(String artist, String track, String lyric) {
		File f = new File(context.getCacheDir(), artist + " - " + track);
		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(lyric.getBytes());
			out.close();
			Log.d(TAG, "saved in chace " + track);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
