package com.example.soundrater1

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class TrackAdapter(private var tracks: List<TrackItem>) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(track: TrackItem)
    }
    var onItemClickListener: OnItemClickListener? = null
    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val trackNameTextView: TextView = view.findViewById(R.id.textViewTrackName)
        val artistNameTextView: TextView = view.findViewById(R.id.textViewArtistName)
        val trackImage: ImageView = view.findViewById(R.id.track_image)
        // Initialize other views here if needed
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.trackNameTextView.text = track.name
        val artistName = if (track.artists.isNotEmpty()) track.artists[0].name else "Unknown Artist" // Si fiquem nomes 1 artista dona error ? display multiple artists
        holder.artistNameTextView.text = artistName

        val imageUrl = track.album.images.firstOrNull()?.url //url imatge

        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        Log.e("TrackAdapter", "Image load failed", e)
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        return false
                    }
                })
                .into(holder.trackImage)
        } else {
            // Set a default image or leave it blank if no image URL is available
            holder.trackImage.setImageResource(R.drawable.ic_launcher_background)
        }
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(track)
        }
    }

    fun updateTracks(newTracks: List<TrackItem>) {
        this.tracks = newTracks
        notifyDataSetChanged() // Notify the adapter of the data change
    }


    override fun getItemCount() = tracks.size
}
