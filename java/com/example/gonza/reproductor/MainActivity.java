package com.example.gonza.reproductor;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SyncAdapterType;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
		implements MyAdapter.OnItemClickListener,
		ActivityCompat.OnRequestPermissionsResultCallback {

	private RecyclerView mRecyclerView;
	private MyAdapter mAdapter;
	private List<Song> myDataset = new ArrayList<>();

	private PlayerService musicService;
	private Intent playIntent;
	private boolean musicBound = false;

	private ServiceConnection musicConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			PlayerService.MusicBinder binder = (PlayerService.MusicBinder)service;
			//get service
			musicService = binder.getService();
			//pass list
			musicService.setList(myDataset);
			musicBound = true;
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicBound = false;
		}
	};

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

		askPermission();
		// specify an adapter (see also next example)
		mAdapter = new MyAdapter(myDataset);
		mAdapter.setOnClickListener(this);
		mRecyclerView.setAdapter(mAdapter);
	}

	private void askPermission() {
		int REQUEST_WAKE_LOCK = 1;
		ActivityCompat.requestPermissions(this,
				new String[]{ Manifest.permission.WAKE_LOCK, Manifest.permission.READ_EXTERNAL_STORAGE },
				REQUEST_WAKE_LOCK);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		initSongs();
		mAdapter.notifyDataSetChanged();
	}

	public void initSongs() {
		String[] projection = { MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.YEAR };
		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				projection,
				null, null, null
		);
		int i = 0;
		while (cursor.moveToNext()) {
			myDataset.add(new Song(Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)))));
			myDataset.get(i).setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
			myDataset.get(i).setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
			myDataset.get(i).setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
			myDataset.get(i).setYear(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)));
			String[] projection2 = { MediaStore.Audio.AlbumColumns.ALBUM,
					MediaStore.Audio.AlbumColumns.ALBUM_ART };
			if (!myDataset.get(i).getAlbum().equals("<unknown>")) {
				System.out.println(myDataset.get(i).getAlbum());
				Cursor cursor2 = getContentResolver().query(
						MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
						projection2,
						MediaStore.Audio.AlbumColumns.ALBUM + " = ?", // Selección
						new String[] { myDataset.get(i).getAlbum() }, // Argumentos
						null
				);
				if (cursor2.moveToNext()) {
					myDataset.get(i).setAlbumArt(cursor2.getString(cursor2.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART)));
				}
				cursor2.close();
			}
			i++;
		}
		cursor.close();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (playIntent == null) {
			playIntent = new Intent(this, PlayerService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			//Activity com.example.gonza.reproductor.MainActivity has leaked ServiceConnection com.example.gonza.reproductor.MainActivity$1@91bbde8 that was originally bound here
			//android.app.ServiceConnectionLeaked: Activity com.example.gonza.reproductor.MainActivity has leaked ServiceConnection com.example.gonza.reproductor.MainActivity$1@91bbde8 that was originally bound here

			startService(playIntent);
		}
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
		stopService(playIntent);
		unbindService(musicConnection);
		musicService = null;
		super.onDestroy();
	}

	@Override
	public void onClick(View view, int position) {
		musicService.playSong(position);
	}
}