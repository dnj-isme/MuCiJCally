package edu.bluejack22_2.MuCiJCally.utility

import android.net.Uri

object Validator {
    fun validatePlaylistForm(playlistName:String?, playlistCoverImage:Uri?):Boolean {
        if (playlistName != null
            && !playlistName.isEmpty()
            && playlistCoverImage != null
            && !playlistCoverImage.toString().isEmpty()) return true
        return false
    }
    fun validateMusicForm(musicTitle:String, musicFile:Uri?, musicCover:Uri?):Boolean {
        if (musicTitle != null
            && !musicTitle.isEmpty()
            && musicFile != null
            && !musicFile.toString().isEmpty()
            && musicCover != null
            && !musicCover.toString().isEmpty()) return true
        return false
    }
}