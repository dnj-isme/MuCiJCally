package edu.bluejack22_2.MuCiJCally.others

object Constants {
    const val REQUEST_CODE_IMAGE_CHOOSER_PLAYLIST:Int = 1001
    const val REQUEST_CODE_IMAGE_CHOOSER_MUSIC:Int = 1002
    const val REQUEST_CODE_MUSIC_CHOOSER_MUSIC:Int = 1003
    const val REQUEST_CODE_IMAGE_CHOOSER_PROFILE_PIC: Int = 1004
    const val SERVICE_TAG = "MusicService"
    const val NOTIFICATION_CHANNEL_ID = "music"
    const val NOTIFICATION_ID = 1
    const val MEDIA_ROOT_ID = "root_id"
    const val NETWORK_ERROR = "network_error"
    const val UPDATE_PLAYER_POSITION_INTERVAL = 100L
}

enum class Type {
    PLAYLIST,
    MUSIC,
    PROFILE_PIC
}