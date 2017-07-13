package com.example.gonza.providers;

/**
 * Obtiene las letras de Internet. Todavía no sé cómo así que puede
 * tener que cambiar esto.
 */
public interface ProviderBase {

	ProviderBase addArtist();
	ProviderBase addTrack();
	String getLyrics();
}
