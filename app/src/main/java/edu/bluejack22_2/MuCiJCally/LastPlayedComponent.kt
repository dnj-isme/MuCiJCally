package edu.bluejack22_2.MuCiJCally

import Utility.toMusic
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack22_2.MuCiJCally.model.Music
import edu.bluejack22_2.MuCiJCally.model.adapters.LastPlayedComponentAdapter
import edu.bluejack22_2.MuCiJCally.model.factories.MainViewModelFactory
import edu.bluejack22_2.MuCiJCally.others.Status.ERROR
import edu.bluejack22_2.MuCiJCally.others.Status.SUCCESS
import edu.bluejack22_2.MuCiJCally.others.isPlaying
import edu.bluejack22_2.MuCiJCally.utility.PageNavigator
import edu.bluejack22_2.MuCiJCally.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LastPlayedComponent(
    private val activity: AppCompatActivity, private val layoutID: Int
) : Fragment(layoutID) {

    @Inject
    lateinit var mainViewModelFactory: MainViewModelFactory
    private val mainViewModel by lazy { ViewModelProvider(this, mainViewModelFactory)[MainViewModel::class.java]}

    @Inject
    lateinit var swipeSongAdapter: LastPlayedComponentAdapter

    @Inject
    lateinit var glide: RequestManager

    private var currPlayingSong: Music? = null
    private var playbackState: PlaybackStateCompat? = null

    private lateinit var lastPlayedComponent: ViewPager2
    private lateinit var controlButton: ImageView

    private fun switchViewPagerToCurrentSong(music: Music) {
        val newItemIndex = swipeSongAdapter.musics.indexOf(music)
        if (newItemIndex != -1) {
            lastPlayedComponent.currentItem = newItemIndex
            currPlayingSong = music
        }
    }

    private fun subscribeToObserver() {
        mainViewModel.mediaItems.observe(activity, Observer {
            it?.let { result ->
                when (result.status) {
                    SUCCESS -> {
                        result.data?.let { musics ->
                            swipeSongAdapter.musics = musics
                            switchViewPagerToCurrentSong(currPlayingSong ?: return@Observer)
                        }
                    }
                    else -> Unit
                }
            }
        })
        mainViewModel.currPlayingSong.observe(activity, Observer {
            if (it == null) return@Observer
            currPlayingSong = it.toMusic()
            lastPlayedComponent.currentItem = swipeSongAdapter.musics.indexOf(currPlayingSong)
            switchViewPagerToCurrentSong(currPlayingSong ?: return@Observer)
        })
        mainViewModel.playbackState.observe(activity, Observer {
            playbackState = it
            controlButton.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.pause_button
                else R.drawable.continue_button
            )
        })
        mainViewModel.isConnected.observe(activity, Observer {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> {
                        println("Failed to connect the network!")
                    }
                    else -> Unit
                }
            }
        })
    }

    private fun init(view: View) {
        lastPlayedComponent = view.findViewById(R.id.last_played_component_container)
        controlButton = view.findViewById(R.id.control_button_playcomp)
        setAction()
    }

    private fun setAction() {
        lastPlayedComponent.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.musics[position])
                } else {
                    currPlayingSong = swipeSongAdapter.musics[position]
                    lastPlayedComponent.currentItem = position
                }
            }
        })
        controlButton.setOnClickListener {
            currPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }
        swipeSongAdapter.setItemClickListener {
            PageNavigator.switchPage(activity, PlayPageActivity::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lastPlayedComponent.adapter = swipeSongAdapter
        subscribeToObserver()
        println("Address : $mainViewModel")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.playing_music_component, container, false)
        init(view)
        return view
    }

}