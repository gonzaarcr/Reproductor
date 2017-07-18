package com.example.gonza.providers;

import android.net.Uri;

import com.example.gonza.reproductor.LyricsActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Este es el más costoso (primitivo) computacionalmente, pero
 * es facil, no necesita autenticación, tiene muchas letras.
 * Parsea el HTML.
 */
public class LyricsWikia extends ProviderBase {

	private final String TAG = "LyricsWikia";
	private final String URL = "http://lyrics.wikia.com/%s:%s";

	public LyricsWikia(LyricsActivity context) {
		super(context);
	}

	@Override
	protected String getUrl(String artist, String track) {
		artist = artist.replace(" ", "_");
		track = track.replace(" ", "_");
		return String.format(URL, Uri.encode(artist), Uri.encode(track));
	}

	@Override
	protected String parseResponse(String response) {
		String ret = response;
		Document doc = Jsoup.parse(response, URL);
		Elements divElements = doc.select("div");
		for (int i = 0; i < divElements.size(); i++) {
			Element e = divElements.get(i);
			if (e.attr("class").equals("lyricbox")) {
				ret = e.html();
			}
		}
		ret = ret.replace("<br>", "");
		ret = ret.replaceAll("<.*?>", "");
		return ret;
	}
}
