package edu.bluejack22_2.MuCiJCally.model

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

@IgnoreExtraProperties
data class Account(
    var id: String = "",
    var email: String = "",
    var username: String = "",
    var password: String = "",
    var sessionID: String = "",
    var profilePicture: String = "",
): Serializable

@IgnoreExtraProperties
data class Music(
    var id: String = "",
    var title: String = "",
    var musicURL: String = "",
    var coverURL: String = "",
    var uploaderID: String = "",
): Serializable

@IgnoreExtraProperties
data class Playlist(
    var id: String = "",
    var playlistName: String = "",
    var accountID: String = "",
    var coverURL: String = "",
    var musics: ArrayList<String> = ArrayList(),
) : Serializable
