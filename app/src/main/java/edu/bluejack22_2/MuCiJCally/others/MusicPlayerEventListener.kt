package edu.bluejack22_2.MuCiJCally.others

import android.widget.Toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import edu.bluejack22_2.MuCiJCally.R
import edu.bluejack22_2.MuCiJCally.services.MusicService

class MusicPlayerEventListener(
    val musicService: MusicService
) : Player.Listener {

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, R.string.unknown_error, Toast.LENGTH_LONG).show()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if(playbackState == Player.STATE_READY && !playWhenReady) {
            musicService.stopForeground(false)
        }
    }

}