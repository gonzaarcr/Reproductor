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

/**
 * Obtiene las letras de Internet. Todavía no sé cómo así que puede
 * tener que cambiar esto.
 */
public abstract class ProviderBase {

	private final String TAG = "ProviderBase";

	protected LyricsActivity context;

	public ProviderBase() { }

	public ProviderBase(LyricsActivity context) {
		this.context = context;
	}

	public String getLyrics(String artist, String track) {
		String requestURL = getUrl(artist, track);
		Log.d(TAG, "URL: " + requestURL);
		RequestQueue queue = Volley.newRequestQueue(context);
		StringRequest stringRequest = new StringRequest(Request.Method.GET, requestURL,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.d(TAG, response);
						context.setLyrics(parseResponse(response));
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				context.setLyrics("Error: " + error);
			}
		});
		queue.add(stringRequest);
		return null;
	}

	protected abstract String getUrl(String artist, String track);

	protected abstract String parseResponse(String response);

}
