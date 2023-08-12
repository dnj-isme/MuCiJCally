import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.bluejack22_2.MuCiJCally.model.Playlist
import edu.bluejack22_2.MuCiJCally.utility.FirebaseHandler
import java.util.*

object PlaylistRepository {

    fun insertPlaylist(
        playlistName: String,
        playlistCover: Uri,
        uploaderID: String,
        musics: ArrayList<String>
    ) {
        FirebaseHandler.uploadPlaylist(playlistCover, playlistName, uploaderID, musics)
    }

    fun getPlaylists(accountID: String, callback: (List<Playlist>) -> Unit) {
        val playlistsRef = FirebaseHandler.playlistRef
        playlistsRef.orderByChild("accountID")
            .equalTo(accountID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val playlists = mutableListOf<Playlist>()
                    for (snapshot in dataSnapshot.children) {
                        val playlist = snapshot.getValue(Playlist::class.java)
                        playlist?.let {
                            playlists.add(playlist)
                        }
                    }
                    callback(playlists)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Failed to fetch data from server!")
                }
            })
    }

    fun addToPlaylist(playlistID: String, musicID: String) {
        val ref = FirebaseHandler.playlistRef.child("${playlistID}/musics")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val musicsList = snapshot.getValue<ArrayList<String>>()
                if (musicsList != null) {
                    musicsList.add(musicID)
                    ref.setValue(musicsList)
                } else {
                    val newMusicsList = ArrayList<String>()
                    newMusicsList.add(musicID)
                    ref.setValue(newMusicsList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Unknown error! Failed to add song to playlist!")
            }

        })
    }

}
