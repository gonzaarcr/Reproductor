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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Vector;

public class CollectionActivity extends AppCompatActivity
		implements ActivityCompat.OnRequestPermissionsResultCallback,
		BaseElementAdapter.ContentManager,
		BaseElementAdapter.OnItemClickListener {

	final String TAG = "CollectionActivity";

	private RecyclerView mRecyclerView;

	// Lista de albumes
	private BaseElementAdapter mAdapter;
	private final Vector<Song> myDataset = new Vector<>();

	// Listas guardadas
	private BaseElementAdapter mPlaylistAdapter;
	private final Vector<Song> mPlaylistDataset = new Vector<>();

	private boolean isInPlaylistView = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collection);
		mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

		mRecyclerView.setHasFixedSize(true);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		mRecyclerView.setLayoutManager(mLayoutManager);

		setTitle("Collection");
		initAlbums();
		initSavedPlaylist();

		mPlaylistAdapter = new BaseElementAdapter(mPlaylistDataset);
		mPlaylistAdapter.setOnClickListener(this);
		mPlaylistAdapter.setContentManager(new BaseElementAdapter.ContentManager() {
			@Override
			public String getTitle(int position) {
				return mPlaylistDataset.get(position).getAlbum();
			}

			@Override
			public String getSubtitle(int position) {
				return null;
			}

			@Override
			public String getAlbumArt(int position) {
				return null;
			}
		});
		mPlaylistAdapter.setRemoveButton(true);

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

	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.collection_menu, menu);
		return true;
	}

	/**
	 * Reemplaa la lista actual por el álbum seleccionado
	 * @param view
	 * @param position
	 */
	@Override
	public void onClick(View view, int position) {
		Intent r = createReturnIntent(position);
		r.putExtra("clearPlaylist", true);
		finish();
	}

	/**
	 * Agrega a la lista actual el album seleccionado (al final), o
	 * elimina la lista guardada, según corresponda.
	 * @param view
	 * @param position
	 */
	@Override
	public void onButtonClick(View view, int position) {
		if (!isInPlaylistView) {
			Intent r = createReturnIntent(position);
			r.putExtra("clearPlaylist", false);
			finish();
		} else {
			removePlaylist(position);
		}
	}

	private void removePlaylist(int position) {
		String filename = myDataset.get(position).getAlbum() + ".m3u";
		mPlaylistDataset.remove(position);
		mPlaylistAdapter.notifyDataSetChanged();
		File file = new File(getFilesDir(), filename);
		file.delete();
	}

	public void onSavedPlaylistAction(MenuItem mi) {
		if (!isInPlaylistView) {
			isInPlaylistView = true;
			mRecyclerView.setAdapter(mPlaylistAdapter);
			setTitle("Saved");
		} else {
			isInPlaylistView = false;
			mRecyclerView.setAdapter(mAdapter);
			setTitle("Collection");
		}
	}

	public Intent createReturnIntent(int position) {
		Intent returnIntent = new Intent();

		returnIntent.putExtra("isAlbum", !isInPlaylistView);
		if (!isInPlaylistView) {
			returnIntent.putExtra("album", myDataset.get(position).getAlbum());
			returnIntent.putExtra("albumCover", myDataset.get(position).getAlbumArt());
		} else {
			Vector<String> tmp = new Vector<>();
			try {
				File f = new File(getFilesDir(), mPlaylistDataset.get(position).getAlbum() + ".m3u");
				BufferedReader br;
				br = new BufferedReader(new FileReader(f));
				String line = br.readLine();
				while (line != null) {
					tmp.add(line);
					line = br.readLine();
				}
			} catch (Exception e) {}

			String[] savedPlaylist = new String[tmp.size()];
			tmp.toArray(savedPlaylist);
			Log.d(TAG, "savedPlalist: " + Arrays.toString(savedPlaylist));
			returnIntent.putExtra("savedPlaylist", savedPlaylist);
		}

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

	private void initSavedPlaylist() {
		File[] files = getFilesDir().listFiles();
		for (File f: files) {
			if (f.getName().endsWith(".m3u")) {
				Song s = new Song();
				s.setAlbum(f.getName().substring(0, f.getName().length() - 4));
				mPlaylistDataset.add(s);
			}
		}
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
