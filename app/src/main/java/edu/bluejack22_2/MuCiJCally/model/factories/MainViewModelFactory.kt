package edu.bluejack22_2.MuCiJCally.model.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.bluejack22_2.MuCiJCally.utility.MusicPlayer
import edu.bluejack22_2.MuCiJCally.viewmodel.MainViewModel

class MainViewModelFactory (
    private val musicServiceConnection: MusicPlayer
) : ViewModelProvider.Factory {

    private var instance: MainViewModel? = null

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (instance != null) {
            return instance as T
        }
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            instance = MainViewModel(musicServiceConnection)
            return instance as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}