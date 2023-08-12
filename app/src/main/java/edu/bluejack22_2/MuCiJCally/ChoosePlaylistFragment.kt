package edu.bluejack22_2.MuCiJCally

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack22_2.MuCiJCally.model.Playlist
import edu.bluejack22_2.MuCiJCally.model.adapters.ChoosePlaylistAdapter
import edu.bluejack22_2.MuCiJCally.utility.LayoutAssembler
import edu.bluejack22_2.MuCiJCally.viewmodel.AccountViewModel
import edu.bluejack22_2.MuCiJCally.viewmodel.PlaylistViewModel

class ChoosePlaylistFragment(private val onChooseListener: ((playlist: Playlist) -> Unit)) : Fragment(R.layout.playlist_list) {

    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var choosePlaylistRv: RecyclerView
    private lateinit var choosePlaylistAdapter: ChoosePlaylistAdapter
    private lateinit var accountViewModel: AccountViewModel

    private fun init() {
        playlistViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
        choosePlaylistRv = requireView().findViewById(R.id.choose_playlist_rview)
        choosePlaylistAdapter = ChoosePlaylistAdapter()
        accountViewModel = AccountViewModel()
        setAction()
    }

    private fun setAction() {
        val session = accountViewModel.getAccountSession(requireActivity() as AppCompatActivity)
        if (session?.account != null) {
            playlistViewModel.fetchPlaylists(session.account!!.id)
        }
        playlistViewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            choosePlaylistAdapter.updatePlaylists(playlists)
        }
        choosePlaylistAdapter.setItemClickedListener { playlist ->
            onChooseListener.invoke(playlist)
        }
        choosePlaylistRv.layoutManager = LinearLayoutManager(requireContext())
        choosePlaylistRv.adapter = choosePlaylistAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.playlist_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LayoutAssembler(requireActivity() as AppCompatActivity, R.id.choosePlaylist_top_logo).initHeader()
        LayoutAssembler(requireActivity() as AppCompatActivity, R.id.footer_chooseplaylist).initFooter()
        init()
    }

}