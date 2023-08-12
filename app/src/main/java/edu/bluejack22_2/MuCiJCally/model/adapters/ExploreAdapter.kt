package edu.bluejack22_2.MuCiJCally.model.adapters

import Utility
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import edu.bluejack22_2.MuCiJCally.R
import edu.bluejack22_2.MuCiJCally.model.Music
import javax.inject.Inject

open class ExploreAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter(R.layout.music_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(
        holder: PlaylistDetailViewHolder,
        position: Int
    ) {
        // Get music value
        val music = musics[position]
        // Bind cover
        glide.load(music.coverURL).into(holder.songCover)
        // Bind title
        holder.songTitle.text = music.title
        // Bind uploader
        holder.songUploader.text = String.format(
            "%s%s",
            holder.itemView.context.getString(R.string.music_uploader_const),
            " ${music.uploaderID}"
        )

        // Set action if Play Button clicked
        holder.songPlayButton.setOnClickListener {
            onItemClickListener?.invoke(music)
        }
        // Bind duration
        Utility.getDurationFromFirebaseUrl(music.musicURL) { duration ->
            holder.songDuration.text = duration
        }
    }

}

