package com.example.gonza.reproductor;

import android.net.Uri;

/**
 * Created by gonza on 12/10/16.
 */

public class Song {

	private Uri uri;
	private String title;
	private String artist;
	private String album;
	private String albumArt;
	private int year;

	public Song() {
	}

	public Song(Uri uri) {
		this.uri = uri;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getAlbumArt() {
		return albumArt;
	}

	public void setAlbumArt(String albumArt) {
		this.albumArt = albumArt;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	@Override
	public String toString() {
		return "(" + uri.toString() + ", " + title +")";
	}
}
