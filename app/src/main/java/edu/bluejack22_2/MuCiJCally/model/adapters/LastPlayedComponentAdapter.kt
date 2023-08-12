package edu.bluejack22_2.MuCiJCally.model.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.bluejack22_2.MuCiJCally.R
import edu.bluejack22_2.MuCiJCally.model.Music

class LastPlayedComponentAdapter : RecyclerView.Adapter<LastPlayedComponentAdapter.LPViewHolder>() {

    class LPViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val musicCover: ImageView = itemView.findViewById(R.id.music_cover_playcomp)
        val musicTitle: TextView = itemView.findViewById(R.id.music_title_playcomp)
        val musicArtist: TextView = itemView.findViewById(R.id.music_artist_playcomp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LPViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.playing_music_item, parent, false)
        return LPViewHolder(view)
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Music>() {
        override fun areItemsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ: AsyncListDiffer<Music> = AsyncListDiffer(this, diffCallback)

    var musics: List<Music>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun getItemCount(): Int {
        return musics.size
    }

    private var onItemClickListener: (() -> Unit)? = null

    fun setItemClickListener(listener: (() -> Unit)) {
        onItemClickListener = listener
    }

    override fun onBindViewHolder(holder: LPViewHolder, position: Int) {
        // music data
        val music = musics[position]
        // Bind image
        Glide.with(holder.itemView).load(music.coverURL).into(holder.musicCover)
        // Bind title
        holder.musicTitle.text = music.title
        // Bind artist
        holder.musicArtist.text = music.uploaderID
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke()
        }
    }

}