package com.example.gonza.providers;

import com.example.gonza.reproductor.LyricsActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

/**
 * Cache local
 */
public class LocalProvider extends ProviderBase {

	public LocalProvider() { }

	public LocalProvider(LyricsActivity context) {
		this.context = context;
	}

	public boolean getLyrics(String artist, String track) {
		boolean ret = false;
		File f = new File(context.getCacheDir(), getUrl(artist, track));
		if (!f.exists())
			return false;

		BufferedReader br;
		StringBuilder sb = new StringBuilder();
		try {
			br = new BufferedReader(new FileReader(f));
			String tmp = br.readLine();
			while (tmp != null) {
				sb.append(tmp);
				sb.append("\n");
				tmp = br.readLine();
			}
			br.close();
			context.setLyrics(sb.toString());
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	protected String getUrl(String artist, String track) {
		return artist + " - " + track;
	}

	@Override
	protected String parseResponse(String response) {
		return response;
	}
}
