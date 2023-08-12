import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.widget.Toast
import edu.bluejack22_2.MuCiJCally.R
import edu.bluejack22_2.MuCiJCally.model.Music
import edu.bluejack22_2.MuCiJCally.viewmodel.AccountViewModel
import org.mindrot.jbcrypt.BCrypt
import java.net.URI
import java.security.MessageDigest
import java.util.*
import java.util.prefs.Preferences

object Utility {
    fun hashPassword(password: String): String {
        val salt = BCrypt.gensalt()
        return BCrypt.hashpw(password, salt)
    }

    // Checks a password against a hashed password
    fun checkPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(password, hashedPassword)
    }

    fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    fun generateSessionID(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..20)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun getFileExtensionFromUri(uri: Uri): String? {
        val uriString = uri.toString()
        val lastDotIndex = uriString.lastIndexOf('.')
        if (lastDotIndex != -1) {
            return uriString.substring(lastDotIndex)
        }
        return null
    }

    fun getDurationFromFirebaseUrl(firebaseUrl: String, callback: (String) -> Unit) {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(firebaseUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            val duration = mediaPlayer.duration
            callback(formatDuration(duration))
        }
    }

    private fun formatDuration(durationInMillis: Int): String {
        val seconds = durationInMillis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun Music.toMediaMetadataCompat(): MediaMetadataCompat {
        val mediaMetadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.id)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.title)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.musicURL)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, this.title)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, this.coverURL)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, this.coverURL)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.uploaderID)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, this.uploaderID)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, this.uploaderID)
        return mediaMetadata.build()
    }

    fun MediaMetadataCompat.toMusic(): Music? {
        return description?.let {
            Music(
                it.mediaId ?: "",
                it.title.toString() ?: "",
                it.mediaUri.toString() ?: "",
                it.iconUri.toString() ?: "",
                it.subtitle.toString() ?: ""
            )
        }
    }

}