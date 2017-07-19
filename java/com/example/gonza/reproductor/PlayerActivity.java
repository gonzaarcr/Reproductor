package com.example.gonza.reproductor;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.util.Vector;

public class PlayerActivity extends AppCompatActivity
		implements ActivityCompat.OnRequestPermissionsResultCallback,
		BaseElementAdapter.OnItemClickListener,
		BaseElementAdapter.ContentManager,
		PlayerService.ServiceCallback {

	private final String TAG = "PlayerActivity";

	private ImageView cover;
	private Vector<Song> playlist = new Vector<>();
	private BaseElementAdapter mAdapter;

	private ImageButton colectionButton;
	private ImageButton stopButton;
	private ImageButton previousButton;
	private ImageButton playButton;
	private ImageButton nextButton;

	private TextView playingTitle;
	private TextView playingAlbum;
	private TextView playingArtist;
	private Song currentTrack;

	private PlayerService musicService;
	private Intent playIntent;
	private boolean musicBound = false;

	private static final int PICK_ALBUM_REQUEST = 1;
	private static final int READ_STORAGE_REQUEST = 2;

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
		RecyclerView playlistView = (RecyclerView) findViewById(R.id.playlistView);
		stopButton = (ImageButton) findViewById(R.id.stopButton);
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
		mAdapter = new BaseElementAdapter(playlist);
		mAdapter.setOnClickListener(this);
		mAdapter.setContentManager(this);
		mAdapter.setRemoveButton(true);
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

	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.player_menu, menu);
		return true;
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
		stopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				musicService.stop();
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

	public void onLyricsAction(MenuItem mi) {
		Intent i = new Intent(PlayerActivity.this, LyricsActivity.class);
		if (currentTrack != null) {
			i.putExtra("Track", currentTrack.getTitle());
			i.putExtra("Artist", currentTrack.getArtist());
		}
		startActivity(i);
	}

	public void onSaveAction(MenuItem mi) {
		if (playlist.size() == 0)
			return;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		final EditText input = new EditText(this);
		builder.setMessage("Inserte un nombre para la lista de reproducción");
		builder.setView(input);
		builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String filename = input.getText().toString();
				try {
					FileOutputStream out = openFileOutput(filename + ".m3u", Context.MODE_PRIVATE);
					byte[] newLine = (new String("\n")).getBytes();
					for (int i = 0; i < playlist.size(); i++) {
						out.write(playlist.get(i).getUri().toString().getBytes());
						out.write(newLine);
						Log.d(TAG, playlist.get(i).getUri().toString());
					}
					out.close();
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}
		});
		builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	public void onStateChange(PlayerService.State state) {
		switch (state) {
			case PLAY:
				playButton.setImageResource(android.R.drawable.ic_media_pause);
				break;
			case PAUSE:
				playButton.setImageResource(android.R.drawable.ic_media_play);
				break;
			case STOP:
				playButton.setImageResource(android.R.drawable.ic_media_play);
				break;
		}
	}

	public void onSongChange(Song newSong) {
		String title = "";
		String album = "";
		String artist = "";
		currentTrack = newSong;
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
		if (resultCode != Activity.RESULT_OK)
			return;

		if (data.getBooleanExtra("clearPlaylist", false))
			playlist.clear();

		boolean isAlbum = data.getBooleanExtra("isAlbum", true);
		String[] projection = {
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.DURATION,
				MediaStore.Audio.Media.YEAR };

		Cursor cursor;
		if (isAlbum) {
			// Crea la playlist a partir del album
			String album = data.getStringExtra("album");
			cursor = getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					projection,
					MediaStore.Audio.Media.ALBUM + " = ?", // Selection
					new String[] { album },
					MediaStore.Audio.Media.TRACK
			);
		} else {
			// Crea la playlist a partir de una playlist guardada
			String[] selectionArgs = data.getStringArrayExtra("savedPlaylist");
			StringBuilder selection = new StringBuilder();
			for (int i = 0; i < selectionArgs.length; i++)
				selection.append(MediaStore.Audio.Media.DATA + "= ? OR ");
			selection.append(MediaStore.Audio.Media.DATA + "= ?");
			cursor = getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					projection,
					selection.toString(),
					selectionArgs,
					MediaStore.Audio.Media.TRACK
			);
		}

		String albumArt = data.getStringExtra("albumCover");
		while (cursor.moveToNext()) {
			Song newSong = new Song(Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))));
			newSong.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
			newSong.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
			newSong.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
			newSong.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
			if (albumArt == null) {
				Cursor c = getContentResolver().query(
						MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
						new String[] { MediaStore.Audio.AlbumColumns.ALBUM_ART },
						MediaStore.Audio.AlbumColumns.ALBUM + " = ?",
						new String[] { newSong.getAlbum() },
						null);
				if (c.moveToFirst())
					newSong.setAlbumArt(c.getString(c.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART)));
				c.close();
			} else {
				newSong.setAlbumArt(albumArt);
			}
			playlist.add(newSong);
		}
		cursor.close();

		mAdapter.notifyDataSetChanged();
		if (data.getBooleanExtra("clearPlaylist", false)
				&& musicService.getState() != PlayerService.State.PLAY) {
			musicService.playSong(0);
		}
	}

	/**
	 * De BaseElementAdapter.OnClickListener. Actua como play/pausa.
	 */
	@Override
	public void onClick(View view, int position) {
		musicService.playSong(position);
	}

	/**
	 * De BaseElementAdapter.OnClickListener. Saca la canción de la lista.
	 */
	@Override
	public void onButtonClick(View view, int position) {
		musicService.removeSong(position);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * De BaseElementAdapter.ContentManager.
	 */
	@Override
	public String getTitle(int position) {
		return playlist.get(position).getTitle();
	}

	/**
	 * De BaseElementAdapter.ContentManager.
	 * @return "Album - m:ss"
	 */
	@Override
	public String getSubtitle(int position) {
		long d = playlist.get(position).getDuration() / 1000;
		String m = String.valueOf(d / 60);
		String s = String.valueOf(d % 60);
		s = s.length() == 1? "0" + s : s;
		String album = playlist.get(position).getAlbum();
		return album + " - " + m + ":" + s;
	}

	/**
	 * De BaseElementAdapter.ContentManager.
	 */
	@Override
	public String getAlbumArt(int position) {
		return null;
	}
}
