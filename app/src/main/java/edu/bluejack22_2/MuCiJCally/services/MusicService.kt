package edu.bluejack22_2.MuCiJCally.services

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack22_2.MuCiJCally.others.*
import edu.bluejack22_2.MuCiJCally.others.Constants.MEDIA_ROOT_ID
import edu.bluejack22_2.MuCiJCally.others.Constants.NETWORK_ERROR
import edu.bluejack22_2.MuCiJCally.others.Constants.SERVICE_TAG
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSource.Factory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    var isForegroundService = false
    private lateinit var musicNotificationManager: MusicNotificationManager
    private var currPlayingSong: MediaMetadataCompat? = null
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener
    private var isPlayerInitialized = false

    companion object {
        var currSongDuration = 0L
            private set
        private val serviceJob = Job()
        val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
        lateinit var preparerHandler: ((
            songs: List<MediaMetadataCompat>, itemToPlay: MediaMetadataCompat?, playNow: Boolean
        ) -> Unit)
        lateinit var updateQueueHandler: () -> Unit
    }

    override fun onCreate() {
        super.onCreate()
        //setOnPlaybackPrepared here
        preparerHandler = this::preparePlayer
        updateQueueHandler = this::updateQueueNavigator

        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { intent ->
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this, mediaSession.sessionToken, MusicPlayerNotificationListener(this)
        ) {
            currSongDuration = exoPlayer.duration
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource) { songMetadata ->
            currPlayingSong = songMetadata
            preparePlayer(
                firebaseMusicSource.songs, songMetadata, true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        updateQueueNavigator()
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }

    fun updateQueueNavigator() {
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>, itemToPlay: MediaMetadataCompat?, playNow: Boolean
    ) {
        val currSongIndex = if (currPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(currSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
        val queue = songs.mapIndexed { index, song ->
            MediaSessionCompat.QueueItem(song.description, index.toLong())
        }
        mediaSession.setQueue(queue)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onGetRoot(
        clientPackageName: String, clientUid: Int, rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        println("Oof, onLoadChildren is triggered!")
        if (parentId == MEDIA_ROOT_ID) {
            var resultsSent = false
            firebaseMusicSource.whenReady { isInitialized ->
                if (!resultsSent) {
                    resultsSent = true
                    if (isInitialized) {
                        result.sendResult(firebaseMusicSource.asMediaItems())
                        if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()) {
                            preparePlayer(
                                firebaseMusicSource.songs,
                                firebaseMusicSource.songs[0],
                                false
                            )
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
            }
            if (!resultsSent) {
                result.detach()
            }
        }
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return if(firebaseMusicSource.songs.size <= windowIndex) {
                println("Mr Dummyyyyy")
                firebaseMusicSource.generateDummy().description
            }
            else {
                println("Sayonara TPA Mobile")
                firebaseMusicSource.songs[windowIndex].description
            }
        }
    }
}