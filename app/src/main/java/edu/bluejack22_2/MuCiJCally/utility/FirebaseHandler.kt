package edu.bluejack22_2.MuCiJCally.utility

import Utility
import android.net.Uri
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import edu.bluejack22_2.MuCiJCally.model.Account
import edu.bluejack22_2.MuCiJCally.model.Music
import edu.bluejack22_2.MuCiJCally.model.Playlist

object FirebaseHandler {
    private var database: DatabaseReference =
        Firebase.database.getReferenceFromUrl("https://mucijcally-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private var storage: StorageReference = Firebase.storage.getReferenceFromUrl("gs://mucijcally.appspot.com")
//    private var database:DatabaseReference = Firebase.database.getReferenceFromUrl("https://tpa-mobile-cb43d-default-rtdb.firebaseio.com/")
//    private var storage:StorageReference = Firebase.storage.getReferenceFromUrl("gs://tpa-mobile-cb43d.appspot.com")

    val accountRef = database.child("accounts")
    val musicRef = database.child("musics")
    val playlistRef = database.child("playlists")

    fun writeAccount(account: Account, index: Int) {
        database.child("accounts").child(index.toString()).setValue(account)
    }

    fun getStorageReference(): StorageReference {
        return this.storage
    }

    fun uploadPlaylist(
        uri: Uri,
        playlistName: String,
        uploaderID: String,
        musics: ArrayList<String>
    ) {
        // upload image
        val playlistID = playlistRef.push().key!!
        var url: String? = null
        val uriExtension = Utility.getFileExtensionFromUri(uri)
        val imageRef = storage.child("cover").child("playlists").child("$playlistID$uriExtension")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener { _ ->
            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val imageUrl = downloadUrl.toString()
                url = imageUrl
                // upload playlist data if upload playlist cover success
                writePlaylist(
                    Playlist(playlistID, playlistName, uploaderID, url!!, musics),
                    playlistID
                )
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }

    fun writePlaylist(playlist: Playlist, index: String) {
        database.child("playlists").child(index).setValue(playlist)
    }

    fun writeMusic(music: Music, index: String) {
        database.child("musics").child(index).setValue(music)
    }

    fun uploadProfilePic(uri: Uri, callback: (result: String) -> Unit) {
        val uriExtension = Utility.getFileExtensionFromUri(uri)
        val id = Utility.generateUUID();
        val imageRef = storage.child("profile").child("$id$uriExtension")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val link = uri.toString()
                callback(link)
            }
        }
    }

}
