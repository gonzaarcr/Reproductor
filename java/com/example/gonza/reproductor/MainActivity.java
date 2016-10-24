package com.example.gonza.reproductor;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
		implements ColectionAdapter.OnItemClickListener,
		ActivityCompat.OnRequestPermissionsResultCallback {

	private RecyclerView mRecyclerView;
	private ColectionAdapter mAdapter;
	private List<Song> myDataset = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

		// use this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mRecyclerView.setHasFixedSize(true);

		// use a linear layout manager
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		mRecyclerView.setLayoutManager(mLayoutManager);

		initAlbums();

		mAdapter = new ColectionAdapter(myDataset);
		mAdapter.setOnClickListener(this);
		mRecyclerView.setAdapter(mAdapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View view, int position) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("album", myDataset.get(position).getAlbum());
		returnIntent.putExtra("albumCover", myDataset.get(position).getAlbumArt());
		setResult(Activity.RESULT_OK, returnIntent);
		finish();
	}

	public void initAlbums() {
		String[] projection = { 
				MediaStore.Audio.AlbumColumns.ARTIST,
				MediaStore.Audio.AlbumColumns.FIRST_YEAR,
				MediaStore.Audio.AlbumColumns.ALBUM,
				MediaStore.Audio.AlbumColumns.ALBUM_ART };
		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				projection,
				null, null, null
		);
		myDataset = new ArrayList<>();
		int i = 0;
		while (cursor.moveToNext()) {
			myDataset.add(new Song());
			myDataset.get(i).setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST)));
			myDataset.get(i).setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM)));
			myDataset.get(i).setYear(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.FIRST_YEAR)));
			myDataset.get(i).setAlbumArt(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART)));
			i++;
		}
		cursor.close();
	}
}