package edu.bluejack22_2.MuCiJCally

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
// I already have it here
import androidx.fragment.app.activityViewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import edu.bluejack22_2.MuCiJCally.model.adapters.ExploreAdapter
import edu.bluejack22_2.MuCiJCally.model.factories.MainViewModelFactory
import edu.bluejack22_2.MuCiJCally.others.FirebaseMusicSource
import edu.bluejack22_2.MuCiJCally.others.Status
import edu.bluejack22_2.MuCiJCally.services.MusicService
import edu.bluejack22_2.MuCiJCally.utility.LayoutAssembler
import edu.bluejack22_2.MuCiJCally.viewmodel.MainViewModel
import edu.bluejack22_2.MuCiJCally.viewmodel.MusicViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExploreActivity : AppCompatActivity() {

    private lateinit var searchTxt: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var musicViewModel: MusicViewModel

    @Inject
    lateinit var mainViewModelFactory: MainViewModelFactory
    private val mainViewModel by lazy { ViewModelProvider(this, mainViewModelFactory)[MainViewModel::class.java]}

    private lateinit var progressBar: ProgressBar

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    @Inject
    lateinit var adapter: ExploreAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)
        LayoutAssembler(this, R.id.header_home).initHeader()
        LayoutAssembler(this, R.id.footer_home).initFooter()
        LayoutAssembler(this, R.id.lpc_explore_activity).initLastPlayedComponent()

        MusicService.serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
            mainViewModel.loadFirebaseSongs(firebaseMusicSource.songs)
            MusicService.updateQueueHandler()
        }
        musicViewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        progressBar = findViewById(R.id.progress_bar_explore)
        searchTxt = findViewById(R.id.txt_explore_search)
        recyclerView = findViewById(R.id.rv_explore_musiclist)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }

        fetchMusic("")

        handleEvent()
        println("Address : $mainViewModel")
    }

    private fun fetchMusic(query: String) {
        mainViewModel.mediaItems.observe(this, Observer { result ->
            when(result.status) {
                Status.ERROR -> {
                    progressBar.isVisible = false
                    Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show()
                    println("Handling Status Error")
                }
                Status.LOADING -> {
                    progressBar.isVisible = true
                    println("Loading is true men")
                }
                Status.SUCCESS -> {
                    progressBar.isVisible = false
                    result.data?.let {
                        adapter.musics = it
                    }
                }
                else -> Unit
            }
        })
        musicViewModel.getAllMusics(query, true) {
            adapter.musics = it
        }
    }

    private fun handleEvent() {
        searchTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val text = p0.toString()
                fetchMusic(text)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}