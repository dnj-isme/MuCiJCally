package edu.bluejack22_2.MuCiJCally.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.bluejack22_2.MuCiJCally.model.Music
import edu.bluejack22_2.MuCiJCally.others.Constants
import edu.bluejack22_2.MuCiJCally.others.currentPlaybackPosition
import edu.bluejack22_2.MuCiJCally.repository.MusicRepository
import edu.bluejack22_2.MuCiJCally.services.MusicService
import edu.bluejack22_2.MuCiJCally.utility.MusicPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val musicServiceConnection: MusicPlayer
) : ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    private val _currSongDuration = MutableLiveData<Long>()
    val currSongDuration = _currSongDuration

    private val _currPlayerPosition = MutableLiveData<Long>()
    val currPlayerPosition = _currPlayerPosition
  
//     private val _musics = MutableLiveData<List<Music>>()
//     val musics: LiveData<List<Music>> get() = _musics

    fun uploadMusic(musicTitle: String, musicURI: Uri, coverURI: Uri, uploaderID: String) {
        MusicRepository.uploadMusic(musicTitle, musicURI, coverURI, uploaderID)
    }

//     fun getPlaylistMusic(musics: ArrayList<String>) {
//         MusicRepository.getPlaylistMusic(musics) { playlistMusics ->
//             _musics.postValue(playlistMusics)
//         }
//     }

    fun getAllMusics(query: String, strict: Boolean, result: ((List<Music>) -> Unit)) {
        MusicRepository.getAllSongs(query, strict) {
            result.invoke(it)
        }
    }

    init {
        updateCurrPlayerPosition()
    }

    private fun updateCurrPlayerPosition() {
        viewModelScope.launch {
            while (true) {
                val pos = playbackState.value?.currentPlaybackPosition
                if (currPlayerPosition.value != pos) {
                    _currSongDuration.postValue(MusicService.currSongDuration)
                    _currPlayerPosition.postValue(pos!!)
                }
                delay(Constants.UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }

}