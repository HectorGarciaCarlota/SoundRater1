import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.example.soundrater1.R
import com.example.soundrater1.RatedSong
import com.example.soundrater1.UserProfile
import jp.wasabeef.glide.transformations.BlurTransformation


class RatedSongAdapter(
    private var userProfile: UserProfile,
    private val onItemClickListener: (RatedSong) -> Unit
) : RecyclerView.Adapter<RatedSongAdapter.RatedSongViewHolder>() {

    class RatedSongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAlbumCover: ImageView = view.findViewById(R.id.ivAlbumCover)
        val tvSongName: TextView = view.findViewById(R.id.tvSongName)
        val tvArtistName: TextView = view.findViewById(R.id.tvArtistName)
        val rbRating: RatingBar = view.findViewById(R.id.rbRating)
        val ivBlurredAlbumCover: ImageView = view.findViewById(R.id.ivBlurredAlbumCover) // Add this line
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatedSongViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.rated_song, parent, false)
        return RatedSongViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RatedSongViewHolder, position: Int) {
        val ratedSong = userProfile.ratedSongs[position]
        holder.tvSongName.text = ratedSong.trackName
        holder.tvArtistName.text = ratedSong.artistName
        holder.rbRating.rating = ratedSong.rating

        Glide.with(holder.itemView.context)
            .load(ratedSong.imageUri)
            .into(holder.ivAlbumCover)

// Load the album cover with a blur effect for the background
        Glide.with(holder.itemView.context)
            .load(ratedSong.imageUri)
            .apply(bitmapTransform(BlurTransformation(25, 3)))
            .into(holder.ivBlurredAlbumCover)

        holder.itemView.setOnClickListener {
            onItemClickListener(ratedSong)
        }
    }

    fun updateUserProfile(newUserProfile: UserProfile) {
        this.userProfile = newUserProfile
        notifyDataSetChanged()
    }

    override fun getItemCount() = userProfile.ratedSongs.size
}
