package com.example.gonza.reproductor;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gonza on 23/10/16.
 */

public class PlayerActivity extends AppCompatActivity
		implements PlaylistAdapter.OnItemClickListener,
		ActivityCompat.OnRequestPermissionsResultCallback {

	private ImageView cover;
	private List<Song> playlist = new ArrayList<>();
	private RecyclerView playlistView;
	private PlaylistAdapter mAdapter;

	private ImageButton colectionButton;
	private ImageButton previousButton;
	private ImageButton playButton;
	private ImageButton nextButton;

	private PlayerService musicService;
	private Intent playIntent;
	private boolean musicBound = false;

	private final int PICK_ALBUM_REQUEST = 1;

	private ServiceConnection musicConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			PlayerService.MusicBinder binder = (PlayerService.MusicBinder)service;
			//get service
			musicService = binder.getService();
			//pass list
			musicService.setList(playlist);
			musicBound = true;
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicBound = false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState/*, PersistableBundle persistentState*/) {
		super.onCreate(savedInstanceState/*, persistentState*/);
		setContentView(R.layout.activity_player);

		cover = (ImageView) findViewById(R.id.coverView);
		playlistView = (RecyclerView) findViewById(R.id.playlistView);
		colectionButton = (ImageButton) findViewById(R.id.colectionButton);
		previousButton = (ImageButton) findViewById(R.id.previousButton);
		playButton = (ImageButton) findViewById(R.id.playPauseButton);
		nextButton = (ImageButton) findViewById(R.id.nextButton);
		initButtons();

		askPermission();

		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		playlistView.setLayoutManager(mLayoutManager);
		playlistView.setHasFixedSize(true);
		mAdapter = new PlaylistAdapter(playlist);
		mAdapter.setOnClickListener(this);
		playlistView.setAdapter(mAdapter);
	}

	@Override
	protected void onStart() {
		// TODO no hacer al inicio
		super.onStart();
		if (playIntent == null) {
			playIntent = new Intent(this, PlayerService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
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

	private void askPermission() {
		int REQUEST_WAKE_LOCK = 1;
		ActivityCompat.requestPermissions(this,
				new String[] { Manifest.permission.WAKE_LOCK, Manifest.permission.READ_EXTERNAL_STORAGE },
				REQUEST_WAKE_LOCK);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		// TODO poner las canciones que tenía el usuario antes
		// initSongs();
		// mAdapter.notifyDataSetChanged();
	}

	public void initButtons() {
		colectionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
				startActivityForResult(intent, PICK_ALBUM_REQUEST);
			}
		});
		previousButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				musicService.previousSong();
			}
		});
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				musicService.playPause();
			}
		});
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				musicService.nextSong();
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == PICK_ALBUM_REQUEST) {
			String[] projection = { 
					MediaStore.Audio.Media.DATA,
					MediaStore.Audio.Media.TITLE,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media.ALBUM,
					MediaStore.Audio.Media.DURATION,
					MediaStore.Audio.Media.YEAR };
			Cursor cursor = getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					projection,
					MediaStore.Audio.Media.ALBUM + " = ?", // Selection
					new String[] { data.getStringExtra("album") },
					MediaStore.Audio.Media.TRACK
			);

			cover.setImageURI(Uri.parse(data.getStringExtra("albumCover")));
			playlist.clear();
			int i = 0;
			if (cursor.moveToFirst()) {
				playlist.add(new Song(Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)))));
				playlist.get(i).setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
				playlist.get(i).setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
				i++;
			}
			cursor.close();
		}
		mAdapter.notifyDataSetChanged();
	}

}
