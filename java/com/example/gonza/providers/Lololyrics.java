package com.example.gonza.providers;

import android.net.Uri;
import android.util.Log;
import android.util.Xml;

import com.example.gonza.reproductor.LyricsActivity;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;

/**
 * Este es re facil, gratis, sin registración, pero no tiene casi ninguna
 * canción.
 *
 * http://api.lololyrics.com/0.5/getLyric?artist=[ARTIST]&track=[TRACK TITLE]
 */
public class Lololyrics extends ProviderBase {

	private final String TAG = "Lololyrics";
	private final String URL = "http://api.lololyrics.com/0.5/getLyric?artist=%s&track=%s";

	public Lololyrics(LyricsActivity context) {
		super(context);
	}

	@Override
	protected String getUrl(String artist, String track) {
		return String.format(URL, Uri.encode(artist), Uri.encode(track));
	}

	@Override
	protected String parseResponse(String response) {
		String ret = new String();
		try {
			XmlPullParser parser = Xml.newPullParser();
			StringReader in = new StringReader(response);
			parser.setInput(in);

			int eventType = parser.nextTag();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				/*
				if (eventType == XmlPullParser.START_DOCUMENT) {
					Log.d(TAG, "Start document");
				} else if (eventType == XmlPullParser.START_TAG) {
					Log.d(TAG, "Start tag " + parser.getName());
				} else if (eventType == XmlPullParser.END_TAG) {
					Log.d(TAG, "End tag " + parser.getName());
				} else if (eventType == XmlPullParser.TEXT) {
					Log.d(TAG, "Text " + parser.getText());
				}
				*/
				if (eventType == XmlPullParser.START_TAG
						&& parser.getName().equals("response")) {
					break;
				}
				eventType = parser.next();
			}
			parser.next();
			ret = parser.getText();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return ret;
	}
}
