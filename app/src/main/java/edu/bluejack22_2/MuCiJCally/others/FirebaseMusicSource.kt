package edu.bluejack22_2.MuCiJCally.others

import Utility.toMediaMetadataCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import edu.bluejack22_2.MuCiJCally.others.State.*
import edu.bluejack22_2.MuCiJCally.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseMusicSource(
    private val musicDatabase: MusicRepository
) {
    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = STATE_INITIALIZING
        musicDatabase.getAllSongs("", false) {
            songs = it.map { song ->
                song.toMediaMetadataCompat()
            }
            state = STATE_INITIALIZED
        }
    }

    suspend fun fetchMediaData(
        musics: ArrayList<String>,
        callback: (listOfMetadataCompat: List<MediaMetadataCompat>) -> Unit
    ) = withContext(Dispatchers.IO) {
        state = STATE_INITIALIZING
        if (musics.size == 0) {
            songs = emptyList()
            state = STATE_CREATED
            callback(songs)
            println("returning songs that maybe is empty $songs")
            return@withContext
        }
        musicDatabase.getPlaylistMusic(musics) {
            songs = it.map { song ->
                song.toMediaMetadataCompat()
            }
            callback(songs)
            println("deb" + songs)
            state = STATE_INITIALIZED
        }
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(
                    MediaItem.fromUri(
                        song.getString(METADATA_KEY_MEDIA_URI).toUri()
                    )
                )
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.getString(METADATA_KEY_TITLE))
            .setSubtitle(song.getString(METADATA_KEY_ARTIST))
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()
    fun generateDummy(): MediaMetadataCompat = Builder().build()


    private val onReadyListener = mutableListOf<(Boolean) -> Unit>()

    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListener) {
                    field = value
                    onReadyListener.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun clearSongs() {
        for ((i, song) in songs.withIndex()) {
            songs.drop(i)
        }
        state = STATE_CREATED
    }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListener += action
            false
        } else {
            action(state == STATE_INITIALIZED)
            true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}