package com.example.gonza.providers;

import android.net.Uri;
import android.util.Log;

import com.example.gonza.reproductor.LyricsActivity;

import org.json.JSONObject;

/**
 * Dan solamente el 30% de la canción los ratas.
 *
 * user: moviles2017
 * pass: Moviles2017yupi!
 */
public class MusixmatchProvider extends ProviderBase {

	private final String TAG = "Musixmatch";

	private final String ROOT_URL = "http://api.musixmatch.com/ws/1.1/";
	private final String API_KEY = "c0db31653047a0158d480e724c49a624";
	private final String MATCHER_GET_LYRICS = "matcher.lyrics.get";

	public MusixmatchProvider(LyricsActivity context) {
		super(context);
	}

	@Override
	protected String getUrl(String artist, String track) {
		return String.format(ROOT_URL + MATCHER_GET_LYRICS + "?"
		                         + "q_track=%s"
		                         + "&q_artist=%s"
		                         + "&apikey=" + API_KEY,
		                    Uri.encode(track),
		                    Uri.encode(artist));
	}

	@Override
	protected String parseResponse(String response) {
		String ret = new String();
		try {
		JSONObject json = new JSONObject(response);
		ret = json.optJSONObject("message")
				   .optJSONObject("body")
		           .optJSONObject("lyrics")
				   .getString("lyrics_body");
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return ret;
	}
}
