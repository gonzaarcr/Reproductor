package com.example.gonza.reproductor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.example.gonza.providers.Lololyrics;
import com.example.gonza.providers.LyricsWikia;
import com.example.gonza.providers.MusixmatchProvider;
import com.example.gonza.providers.ProviderBase;

public class LyricsActivity extends AppCompatActivity {

	TextView title;
	TextView body;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lyrics);

		title = (TextView) findViewById(R.id.lyricsTitle);
		body = (TextView) findViewById(R.id.lyrics);
		String t = getIntent().getStringExtra("Track");
		if (t != null) {
			title.setText(t);
		}
		ProviderBase mProv = new LyricsWikia(this);
		mProv.getLyrics(getIntent().getStringExtra("Artist"), t);
	}

	public void setLyrics(String lyrics) {
		body.setText(lyrics);
	}
}
