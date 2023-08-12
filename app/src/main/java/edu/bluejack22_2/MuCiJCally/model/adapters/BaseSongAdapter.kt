package edu.bluejack22_2.MuCiJCally.model.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import edu.bluejack22_2.MuCiJCally.R
import edu.bluejack22_2.MuCiJCally.model.Music

abstract class BaseSongAdapter(
    private val layoutID: Int
) :
    RecyclerView.Adapter<BaseSongAdapter.PlaylistDetailViewHolder>() {

    class PlaylistDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songCover: ImageView = itemView.findViewById(R.id.musicItem_cover)
        val songTitle: TextView = itemView.findViewById(R.id.musicItem_title)
        val songUploader: TextView = itemView.findViewById(R.id.musicItem_uploader)
        val songPlayButton: ImageView = itemView.findViewById(R.id.musicItem_playButton)
        val songDuration: TextView = itemView.findViewById(R.id.musicItem_duration)
    }

    protected val diffCallback = object : DiffUtil.ItemCallback<Music>() {
        override fun areItemsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    protected abstract val differ: AsyncListDiffer<Music>

    var musics: List<Music>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistDetailViewHolder {
        return PlaylistDetailViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(layoutID, parent, false)
        )
    }

    protected var onItemClickListener: ((Music) -> Unit)? = null

    fun setItemClickListener(listener: ((Music) -> Unit)) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return musics.size
    }
}