package de.redstripes.schwasenphrein.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.models.Post
import org.jetbrains.anko.textColor

class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        val background_colors: IntArray = intArrayOf(R.color.green_100, R.color.orange_100, R.color.yellow_100, R.color.red_100, R.color.blue_100)
        val foreground_colors: IntArray = intArrayOf(R.color.green_700, R.color.orange_700, R.color.yellow_700, R.color.red_700, R.color.blue_700)
    }

    private val personView: TextView = itemView.findViewById(R.id.post_person)
    var numStarsView: TextView = itemView.findViewById(R.id.post_num_stars)
    private val textView: TextView = itemView.findViewById(R.id.post_text)
    private val iconView: ImageView = itemView.findViewById(R.id.post_icon)
    private val iconTextView: TextView = itemView.findViewById(R.id.post_icon_text)

    fun bindToPost(post: Post, starClickListener: View.OnClickListener) {
        personView.text = post.person
        numStarsView.text = post.starCount.toString()
        textView.text = post.text
        iconTextView.text = post.person?.substring(0,1)

        val index = post.colorIndex ?: 0
        iconTextView.textColor = ContextCompat.getColor(itemView.context, foreground_colors[index])
        iconView.setColorFilter(ContextCompat.getColor(itemView.context, background_colors[index]))

        numStarsView.setOnClickListener(starClickListener)
    }
}
