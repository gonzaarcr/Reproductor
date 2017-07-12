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
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CollectionActivity extends AppCompatActivity
		implements ActivityCompat.OnRequestPermissionsResultCallback,
		BaseElementAdapter.ContentManager,
		BaseElementAdapter.OnItemClickListener {

	final String TAG = "CollectionActivity";

	private RecyclerView mRecyclerView;
	private BaseElementAdapter mAdapter;
	private List<Song> myDataset = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collection);
		mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

		// use this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mRecyclerView.setHasFixedSize(true);

		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		mRecyclerView.setLayoutManager(mLayoutManager);

		initAlbums();

		mAdapter = new BaseElementAdapter(myDataset);
		mAdapter.setOnClickListener(this);
		mAdapter.setContentManager(this);
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
		Intent r = createReturnIntent(position);
		r.putExtra("clearPlaylist", true);
		Log.d(TAG, "onClick");
		finish();
	}

	@Override
	public void onButtonClick(View view, int position) {
		Intent r = createReturnIntent(position);
		r.putExtra("clearPlaylist", false);
		Log.d(TAG, "onButtonClick");
		finish();
	}

	public Intent createReturnIntent(int position) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("album", myDataset.get(position).getAlbum());
		returnIntent.putExtra("albumCover", myDataset.get(position).getAlbumArt());
		setResult(Activity.RESULT_OK, returnIntent);
		return returnIntent;
	}

	public void initAlbums() {
		String[] projection = {
				MediaStore.Audio.AlbumColumns.ALBUM,
				MediaStore.Audio.AlbumColumns.ARTIST,
				MediaStore.Audio.AlbumColumns.LAST_YEAR,
				MediaStore.Audio.AlbumColumns.ALBUM_ART };
		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
				projection,
				null, null, null
		);
		myDataset = new ArrayList<>();
		int i = 0;
		while (cursor.moveToNext()) {
			myDataset.add(new Song());
			myDataset.get(i).setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST)));
			myDataset.get(i).setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM)));
			myDataset.get(i).setYear(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.LAST_YEAR)));
			myDataset.get(i).setAlbumArt(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART)));
			i++;
		}
		cursor.close();
	}

	/**
	 * De BaseElementAdapter.ContentManager.
	 */
	@Override
	public String getTitle(int position) {
		return myDataset.get(position).getAlbum();
	}

	/**
	 * De BaseElementAdapter.ContentManager.
	 */
	@Override
	public String getSubtitle(int position) {
		return myDataset.get(position).getArtist();
	}

	/**
	 * De BaseElementAdapter.ContentManager.
	 */
	@Override
	public String getAlbumArt(int position) {
		return myDataset.get(position).getAlbumArt();
	}
}