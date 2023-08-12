package edu.bluejack22_2.MuCiJCally.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.bluejack22_2.MuCiJCally.model.Music
import edu.bluejack22_2.MuCiJCally.others.*
import edu.bluejack22_2.MuCiJCally.utility.MusicPlayer
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicPlayer
) : ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<Music>>>()
    val mediaItems: LiveData<Resource<List<Music>>> = _mediaItems

    private val _playlistItems = MutableLiveData<Resource<List<Music>>>()
    val playlistItems: LiveData<Resource<List<Music>>> = _playlistItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currPlayingSong = musicServiceConnection.currPlayingSong
    val playbackState = musicServiceConnection.playbackState
    private var initFlag = true

    init {
        if (initFlag) {
            _mediaItems.postValue(Resource.loading(null))
            musicServiceConnection.subscribe(
                Constants.MEDIA_ROOT_ID,
                object : MediaBrowserCompat.SubscriptionCallback() {
                    override fun onChildrenLoaded(
                        parentId: String,
                        children: MutableList<MediaBrowserCompat.MediaItem>
                    ) {
                        super.onChildrenLoaded(parentId, children)
                        val items = ArrayList<Music>()
                        for (it in children) {
                            items.add(
                                Music(
                                    it.description.mediaId!!,
                                    it.description.title!!.toString(),
                                    it.description.mediaUri!!.toString(),
                                    it.description.iconUri!!.toString(),
                                    it.description.subtitle!!.toString()
                                )
                            )
                        }
                        _mediaItems.postValue(Resource.success(items))
                    }
                })
            println("This will trigger onLoadChildren i think?")
            initFlag = false
        }
    }

    fun loadFirebaseSongs(firebaseMusicSource: List<MediaMetadataCompat>) {
        _mediaItems.postValue(Resource.loading(null))
        if (firebaseMusicSource.isEmpty()) {
            println("Empty mamkannya masuk sini")
            _mediaItems.postValue(Resource.error("mty", null))
            return
        }
        val items = ArrayList<Music>()
        for (it in firebaseMusicSource) {
            items.add(
                Music(
                    it.description.mediaId!!,
                    it.description.title!!.toString(),
                    it.description.mediaUri!!.toString(),
                    it.description.iconUri!!.toString(),
                    it.description.subtitle!!.toString()
                )
            )
        }
        _mediaItems.postValue(Resource.success(items))
    }

    fun skipToNextSong() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem: Music, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared &&
            mediaItem.id == currPlayingSong.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
        ) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromUri(mediaItem.musicURL.toUri(), null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(
            Constants.MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }

    fun resetMediaItem() {
        _mediaItems.postValue(
            Resource(Status.LOADING, null,  "loading kah")
        )
    }

}