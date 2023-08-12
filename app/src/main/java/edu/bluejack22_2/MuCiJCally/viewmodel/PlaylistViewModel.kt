package edu.bluejack22_2.MuCiJCally.viewmodel

import PlaylistRepository
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.bluejack22_2.MuCiJCally.model.Playlist
import java.util.Vector

class PlaylistViewModel : ViewModel() {

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> get() = _playlists

    fun createPlaylist(playlistName: String, playlistCover: Uri, uploaderID: String, musics: ArrayList<String>) {
        PlaylistRepository.insertPlaylist(playlistName, playlistCover, uploaderID, musics)
    }

    fun fetchPlaylists(accountID: String) {
        PlaylistRepository.getPlaylists(accountID) { playlists ->
            _playlists.postValue(playlists)
            Log.d("SIZE", playlists.size.toString())
        }
    }

    fun addToPlaylist(playlistID: String, musicID: String) {
        PlaylistRepository.addToPlaylist(playlistID, musicID)
    }


}
