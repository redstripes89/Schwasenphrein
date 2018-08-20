package de.redstripes.schwasenphrein.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.models.Post

class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val titleView: TextView = itemView.findViewById(R.id.post_title)
    private val authorView: TextView = itemView.findViewById(R.id.post_author)
    var numStarsView: TextView = itemView.findViewById(R.id.post_num_stars)
    private val bodyView: TextView = itemView.findViewById(R.id.post_text)

    fun bindToPost(post: Post, starClickListener: View.OnClickListener) {
        titleView.text = post.title
        authorView.text = post.author
        numStarsView.text = post.starCount.toString()
        bodyView.text = post.body

        numStarsView.setOnClickListener(starClickListener)
    }
}
