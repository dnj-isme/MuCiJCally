package edu.bluejack22_2.MuCiJCally

import Utility.toMusic
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack22_2.MuCiJCally.model.Music
import edu.bluejack22_2.MuCiJCally.model.factories.MainViewModelFactory
import edu.bluejack22_2.MuCiJCally.others.Status.SUCCESS
import edu.bluejack22_2.MuCiJCally.others.isPlaying
import edu.bluejack22_2.MuCiJCally.utility.LayoutAssembler
import edu.bluejack22_2.MuCiJCally.viewmodel.MainViewModel
import edu.bluejack22_2.MuCiJCally.viewmodel.MusicViewModel
import edu.bluejack22_2.MuCiJCally.viewmodel.PlaylistViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class PlayPageActivity : AppCompatActivity() {

    @Inject
    lateinit var mainViewModelFactory: MainViewModelFactory
    private val mainViewModel by lazy { ViewModelProvider(this, mainViewModelFactory)[MainViewModel::class.java]}

    @Inject
    lateinit var glide: RequestManager

    private lateinit var addPlaylistButton: ImageView
    private lateinit var songCover: ImageView
    private lateinit var songTitle: TextView
    private lateinit var songArtist: TextView
    private lateinit var currDuration: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var totalDuration: TextView
    private lateinit var prevButton: ImageView
    private lateinit var toggleButton: ImageView
    private lateinit var nextButton: ImageView

    private val songViewModel: MusicViewModel by viewModels()

    private var currPlayingSong: Music? = null

    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar = true

    private lateinit var playlistViewModel: PlaylistViewModel

    private fun init() {
        addPlaylistButton = findViewById(R.id.add_playlist_button_playpage)
        songCover = findViewById(R.id.playpage_songcover)
        songTitle = findViewById(R.id.playpage_title)
        songArtist = findViewById(R.id.playpage_artist)
        currDuration = findViewById(R.id.playpage_curr_dur)
        seekBar = findViewById(R.id.playpage_seek_bar)
        totalDuration = findViewById(R.id.playpage_total_dur)
        prevButton = findViewById(R.id.prev_button_playpage)
        toggleButton = findViewById(R.id.toggle_button_playpage)
        nextButton = findViewById(R.id.next_button_playpage)
        playlistViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]

        postInit()
    }

    private fun postInit() {
        subscribeToObserver()
        toggleButton.setOnClickListener {
            currPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }
        prevButton.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }
        nextButton.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
        seekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setCurrPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                seekBar.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })
        addPlaylistButton.setOnClickListener {
            showPlaylist()
        }
    }

    private fun addSongToPlaylist(playlistID: String, playlistName: String) {
        currPlayingSong?.id?.let { playlistViewModel.addToPlaylist(playlistID, it) }
        Toast.makeText(this,  getString(R.string.add_playlist_notif)+ " $playlistName", Toast.LENGTH_LONG).show()
    }

    private fun showPlaylist() {
        val fragmentManager = supportFragmentManager
        val fragment = ChoosePlaylistFragment() { playlist ->
            fragmentManager.popBackStack()
            addSongToPlaylist(playlistID = playlist.id, playlistName = playlist.playlistName)
        }
        fragmentManager.beginTransaction()
            .replace(R.id.play_page, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun updateTitleAndSongImage(music: Music) {
        songTitle.text = music.title
        songArtist.text = music.uploaderID
        glide.load(music.coverURL).into(songCover)
    }

    private fun subscribeToObserver() {
        mainViewModel.mediaItems.observe(this, Observer {
            it.let { result ->
                when (result.status) {
                    SUCCESS -> {
                        result.data?.let { musics ->
                            if (currPlayingSong == null && musics.isNotEmpty()) {
                                currPlayingSong = musics[0]
                                updateTitleAndSongImage(musics[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        })
        mainViewModel.currPlayingSong.observe(this, Observer {
            if (it == null) return@Observer
            currPlayingSong = it.toMusic()
            updateTitleAndSongImage(currPlayingSong!!)
        })
        mainViewModel.playbackState.observe(this, Observer {
            playbackState = it
            toggleButton.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.pause_button else R.drawable.continue_button
            )
            seekBar.progress = it?.position?.toInt() ?: 0
        })
        songViewModel.currPlayerPosition.observe(this, Observer {
            if(shouldUpdateSeekbar) {
                seekBar.progress = it.toInt()
                setCurrPlayerTimeToTextView(it)
            }
        })
        songViewModel.currSongDuration.observe(this, Observer {
            seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            totalDuration.text = dateFormat.format(it)
        })
    }

    private fun setCurrPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        currDuration.text = dateFormat.format(ms)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_page)
        LayoutAssembler(this, R.id.header_playpage).initHeader()
        LayoutAssembler(this, R.id.footer_playpage).initFooter()
        init()
        println("Address : $mainViewModel")
    }

}