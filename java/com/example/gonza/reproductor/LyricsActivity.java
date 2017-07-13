package com.example.gonza.reproductor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LyricsActivity extends AppCompatActivity {

	TextView title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lyrics);

		title = (TextView) findViewById(R.id.lyricsTitle);
		String t = getIntent().getStringExtra("Track");
		if (t != null)
			title.setText(t);
	}
}
