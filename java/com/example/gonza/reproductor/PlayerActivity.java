package com.example.gonza.reproductor;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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

	private TextView playingTitle;
	private TextView playingAlbum;
	private TextView playingArtist;

	private PlayerService musicService;
	private Intent playIntent;
	private boolean musicBound = false;

	private final int PICK_ALBUM_REQUEST = 1;
	private final int READ_STORAGE_REQUEST = 2;

	private ServiceConnection musicConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			PlayerService.MusicBinder binder = (PlayerService.MusicBinder)service;
			musicService = binder.getService();
			musicService.setList(playlist);
			musicService.onPlayListener(PlayerActivity.this);
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
		playingTitle = (TextView) findViewById(R.id.playingTitle);
		playingAlbum = (TextView) findViewById(R.id.playingAlbum);
		playingArtist = (TextView) findViewById(R.id.playingArtist);
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
		int REQUEST_ALL = 1;
		ActivityCompat.requestPermissions(this,
				new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WAKE_LOCK },
				REQUEST_ALL);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == READ_STORAGE_REQUEST
				&& grantResults.length > 0
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			colectionButton.performClick();
		}
	}

	public void initButtons() {
		colectionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int permissionCheck = ContextCompat.checkSelfPermission(PlayerActivity.this,
						Manifest.permission.READ_EXTERNAL_STORAGE);
				if (permissionCheck == PackageManager.PERMISSION_DENIED) {
					ActivityCompat.requestPermissions(PlayerActivity.this,
							new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
							READ_STORAGE_REQUEST);
					return;
				}
				Intent intent = new Intent(PlayerActivity.this, CollectionActivity.class);
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

	public void onStateChange(boolean play) {
		if (play)
			playButton.setImageResource(android.R.drawable.ic_media_pause);
		else
			playButton.setImageResource(android.R.drawable.ic_media_play);
	}

	public void onSongChange(Song newSong) {
		String title = "";
		String album = "";
		String artist = "";
		if (newSong != null) {
			title = newSong.getTitle();
			album = newSong.getAlbum();
			artist = newSong.getArtist();
			String tmp = newSong.getAlbumArt();
			if (tmp != null)
				cover.setImageURI(Uri.parse(tmp));
			else
				cover.setImageResource(R.drawable.default_cover);
		} else {
			cover.setImageResource(R.drawable.default_cover);
		}
		playingTitle.setText(title);
		playingAlbum.setText(album);
		playingArtist.setText(artist);
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

			playlist.clear();
			while (cursor.moveToNext()) {
				Song newSong = new Song(Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))));
				newSong.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
				newSong.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
				newSong.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
				newSong.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
				newSong.setAlbumArt(data.getStringExtra("albumCover"));
				playlist.add(newSong);
			}
			cursor.close();
			mAdapter.notifyDataSetChanged();
			if (!musicService.isPlaying()) {
				musicService.playSong(0);
			}
		}
	}

}
