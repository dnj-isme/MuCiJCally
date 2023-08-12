package edu.bluejack22_2.MuCiJCally

import Utility.toMusic
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack22_2.MuCiJCally.model.Music
import edu.bluejack22_2.MuCiJCally.model.Playlist
import edu.bluejack22_2.MuCiJCally.model.adapters.PlaylistDetailAdapter
import edu.bluejack22_2.MuCiJCally.model.factories.MainViewModelFactory
import edu.bluejack22_2.MuCiJCally.others.FirebaseMusicSource
import edu.bluejack22_2.MuCiJCally.others.Resource
import edu.bluejack22_2.MuCiJCally.others.Status
import edu.bluejack22_2.MuCiJCally.services.MusicService
import edu.bluejack22_2.MuCiJCally.utility.LayoutAssembler
import edu.bluejack22_2.MuCiJCally.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaylistDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private lateinit var playlist: Playlist
    private lateinit var playlistTitle: TextView
    private lateinit var playlistDetailPLayallButton: ImageView
    private lateinit var playlistDetailPlayallLbl: TextView
    private lateinit var playlistDetailRecyclerView: RecyclerView

    @Inject
    lateinit var mainViewModelFactory: MainViewModelFactory
    private val mainViewModel by lazy {
        ViewModelProvider(
            this,
            mainViewModelFactory
        )[MainViewModel::class.java]
    }
    private lateinit var progressBar: ProgressBar

    @Inject
    lateinit var playlistDetailAdapter: PlaylistDetailAdapter

    private fun init() {
        playlist = this.intent.getSerializableExtra("extras") as Playlist
        mainViewModel.resetMediaItem()

        MusicService.serviceScope.launch {
            firebaseMusicSource.fetchMediaData(playlist.musics) {
                println("it adalah : $it")
                it.forEach { music ->
                    println("each title : ${music.toMusic()?.title}")
                }
                mainViewModel.loadFirebaseSongs(it)
            }
        }

        playlistTitle = findViewById(R.id.playlistDetail_nameLbl)
        playlistDetailPlayallLbl = findViewById(R.id.playlistDetail_playAllLbl)
        playlistDetailPLayallButton = findViewById(R.id.playlistDetail_playAllbutton)
        playlistDetailRecyclerView = findViewById(R.id.playlistdetail_recyclerView)
        progressBar = findViewById(R.id.progress_bar_playlist_detail)
        bindValue()
        subscribeToObservers()

    }

    private fun bindValue() {
        playlistTitle.text = playlist.playlistName
        playlistDetailRecyclerView.layoutManager = LinearLayoutManager(this)
        playlistDetailRecyclerView.adapter = playlistDetailAdapter
        setAction()
    }

    private fun setAction() {
        playlistDetailAdapter.setItemClickListener { music ->
            mainViewModel.playOrToggleSong(music)
        }
        playlistDetailPLayallButton.setOnClickListener {
            mainViewModel.mediaItems.observe(this) {
                it.data?.let { musics ->
                    mainViewModel.playOrToggleSong(musics[0], true)
                }
            }
        }
    }

    private fun subscribeToObservers() {
        val observer = Observer { playlistMusic: Resource<List<Music>> ->
            when (playlistMusic.status) {
                Status.SUCCESS -> {
                    println("finished")
                    progressBar.isVisible = false;
                    playlistDetailPLayallButton.isVisible = true
                    playlistDetailPlayallLbl.isVisible = true
                    playlistDetailRecyclerView.isVisible = true
                    playlistMusic.data?.let { musics ->
                        if (musics.isEmpty()) {
                            Toast.makeText(this, R.string.empty_playlist, Toast.LENGTH_LONG).show()
                            playlistDetailAdapter.musics = emptyList()
                        } else {
                            playlistDetailAdapter.musics = musics
                            LayoutAssembler(this, R.id.lpc_playlist_detail).initLastPlayedComponent()
                        }
                    }
                }
                Status.ERROR -> {
                    println(playlistMusic.message)
                    playlistDetailPLayallButton.isVisible = false
                    playlistDetailPlayallLbl.isVisible = false
                    progressBar.isVisible = false
                    playlistDetailRecyclerView.isVisible = false
                    if (playlistMusic.message == "mty") {
                        Toast.makeText(this, R.string.empty_playlist, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show()
                    }
                    mainViewModel.resetMediaItem()
                }
                Status.LOADING -> {
                    println("Loading is true men")
                    playlistDetailPLayallButton.isVisible = false
                    playlistDetailPlayallLbl.isVisible = false
                    progressBar.isVisible = false;
                }
            }
        }
        mainViewModel.mediaItems.observe(this, observer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)
        LayoutAssembler(this, R.id.header_playlistdetail).initHeader()
        LayoutAssembler(this, R.id.footer_playlistdetail).initFooter()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.mediaItems.removeObservers(this)
    }

}