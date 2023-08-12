package edu.bluejack22_2.MuCiJCally.model.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.bluejack22_2.MuCiJCally.R
import edu.bluejack22_2.MuCiJCally.model.Playlist
import edu.bluejack22_2.MuCiJCally.viewmodel.AccountViewModel

class ChoosePlaylistAdapter : RecyclerView.Adapter<ChoosePlaylistAdapter.ChoosePlaylistItem>() {

    private val playlists = mutableListOf<Playlist>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePlaylistItem {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.playlist_list_item, parent, false)
        return ChoosePlaylistItem(view)
    }

    override fun onBindViewHolder(holder: ChoosePlaylistItem, position: Int) {
        // Bind data to the views in the ViewHolder
        val playlist = playlists[position]
        holder.titleTextView.text = playlist.playlistName
        // bind account name
        val accountViewModel: AccountViewModel = AccountViewModel()
        accountViewModel.getAccountByID(playlist.accountID) { it ->
            if (it != null) {
                holder.uploaderTextView.text = String.format(
                    "%s%s",
                    holder.itemView.context.getString(R.string.playlist_uploader_const),
                    " ${it.username}"
                )
            } else {
                holder.uploaderTextView.text = String.format(
                    "%s%s",
                    holder.itemView.context.getString(R.string.playlist_uploader_const),
                    " Anonymous"
                )
            }
        }
        // Load image using Glide
        Glide.with(holder.itemView)
            .load(playlist.coverURL) // Replace with the Firebase URL for the image
            .into(holder.coverImageView)

        // set  action if clicked
        holder.itemView.setOnClickListener {
            onItemClickedListener?.invoke(playlist)
        }
    }

    var onItemClickedListener: ((playlist: Playlist) -> Unit)? = null

    fun setItemClickedListener(listener: ((playlist: Playlist) -> Unit)) {
        onItemClickedListener = listener
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    class ChoosePlaylistItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImageView: ImageView = itemView.findViewById(R.id.playlist_item_imageview)
        val titleTextView: TextView = itemView.findViewById(R.id.playlist_item_title)
        val uploaderTextView: TextView = itemView.findViewById(R.id.playlist_item_uploader)
    }

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newPlaylists)
        notifyDataSetChanged()
    }

}