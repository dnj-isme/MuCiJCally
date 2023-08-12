package edu.bluejack22_2.MuCiJCally.repository

import Utility
import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import edu.bluejack22_2.MuCiJCally.model.Music
import edu.bluejack22_2.MuCiJCally.utility.FirebaseHandler

object MusicRepository {

    val firebase = FirebaseHandler.musicRef

    fun uploadMusic(musicTitle: String, musicURI: Uri, coverURI: Uri, uploaderID: String) {
        val id: String = firebase.push().key!!
        var musicURL: String? = null
        var coverURL: String? = null
        val musicExt: String? = Utility.getFileExtensionFromUri(musicURI)
        val coverExt: String? = Utility.getFileExtensionFromUri(coverURI)
        val musicRef = FirebaseHandler.getStorageReference()
            .child("musics")
            .child("$id$musicExt")
        val coverRef = FirebaseHandler.getStorageReference()
            .child("cover")
            .child("musics")
            .child("$id$coverExt")
        val uploadTask = musicRef.putFile(musicURI)
        uploadTask.addOnSuccessListener { _ ->
            musicRef.downloadUrl.addOnSuccessListener { downloadURL ->
                musicURL = downloadURL.toString()
                val uploadTask = coverRef.putFile(coverURI)
                uploadTask.addOnSuccessListener { _ ->
                    coverRef.downloadUrl.addOnSuccessListener { downloadURL ->
                        coverURL = downloadURL.toString()
                        FirebaseHandler.writeMusic(Music(id, musicTitle, musicURL!!, coverURL!!, uploaderID), id)
                    }.addOnFailureListener {
                        println("Failed to get uploaded music URL!")
                    }
                }.addOnFailureListener {
                    println("Failed to run upload task!")
                }
            }.addOnFailureListener {
                println("Failed to get uploaded music URL!")
            }
        }.addOnFailureListener {
            println("Failed to run upload task!")
        }
    }

    fun getPlaylistMusic(musics: ArrayList<String>, callback: (List<Music>) -> Unit) {
        val musicList = mutableListOf<Music>()
        println("$musics")
        for (musicID in musics) {
            firebase.child(musicID).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val music = snapshot.getValue(Music::class.java)
                    music?.let {
                        musicList.add(music)
                    }
                    if (musicList.size == musics.size) {
                        callback(musicList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Failed to get the playlist musics!")
                }
            })
        }
    }

    // query: Query Params (example: "John", will find all music containing "John" as the title)
    // strict: determine whether the search is strict (if it's strict, if not matches, the result will be removed from the
    // result set, otherwise, placed it on the bottom (lower priority)
    fun getAllSongs(query: String, strict: Boolean, callback: (List<Music>) -> Unit) {
        val musicList = mutableListOf<Music>()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (musicSnapshot in snapshot.children) {
                    val music = musicSnapshot.getValue(Music::class.java)
                    music?.let {
                        if(it.title.lowercase().contains(query.lowercase()) || !strict) {
                            musicList.add(it)
                        }
                    }
                }

                musicList.sortWith(Comparator { music, music2 ->
                    var output = 0

                    if (music.title.lowercase().contains(query.lowercase())) output -= 1
                    if (music2.title.lowercase().contains(query.lowercase())) output += 1

                    return@Comparator output
                })

                firebase.removeEventListener(this)
                callback(musicList)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to get all songs!")
            }
        }
        firebase.addListenerForSingleValueEvent(listener)
    }

    // idList: id List. If the song ID matches with any item in the idList, it willl 
    // strict: determine whether the search is strict (if it's strict, if not matches, the result will be removed from the
    // result set, otherwise, placed it on the bottom (lower priority)
    fun getSongsFromID(idList: List<String>, strict: Boolean, callback: (List<Music>) -> Unit) {
        val musicList = mutableListOf<Music>()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (musicSnapshot in snapshot.children) {
                    val music = musicSnapshot.getValue(Music::class.java)
                    music?.let {
                        if(idList.contains(it.id) || !strict) {
                            musicList.add(it)
                            Log.d("DEBUG STUFF", it.id)
                        }
                    }
                }

                if(!strict) {
                    musicList.sortWith(Comparator { music, music2 ->
                        var output = 0

                        if (idList.contains(music.id)) output -= 1
                        if (idList.contains(music2.id)) output += 1

                        return@Comparator output
                    })
                }

                firebase.removeEventListener(this)
                callback(musicList)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to get all songs!")
            }
        }
        firebase.addListenerForSingleValueEvent(listener)
    }
}