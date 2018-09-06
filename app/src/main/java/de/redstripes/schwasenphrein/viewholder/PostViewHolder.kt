package de.redstripes.schwasenphrein.viewholder

import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.models.Post
import org.jetbrains.anko.textColor

class PostViewHolder(itemView: View, private val letterTitleColors: TypedArray, private val letterTitleColorsDark: TypedArray) : RecyclerView.ViewHolder(itemView) {

    private val personView: TextView = itemView.findViewById(R.id.post_person)
    private var numStarsView: TextView = itemView.findViewById(R.id.post_num_stars)
    private val textView: TextView = itemView.findViewById(R.id.post_text)
    private val iconView: ImageView = itemView.findViewById(R.id.post_icon)
    private val iconTextView: TextView = itemView.findViewById(R.id.post_icon_text)
    private val dateTextView: TextView = itemView.findViewById(R.id.post_date)

    fun bindToPost(post: Post, uid: String, starClickListener: View.OnClickListener) {
        personView.text = post.person
        textView.text = post.text
        dateTextView.text = post.date

        iconTextView.text = post.person?.substring(0, 1)
        iconTextView.textColor = getForegroundColor(post.person)
        iconTextView.alpha = 184f / 255f
        iconView.setColorFilter(getBackgroundColor(post.person), PorterDuff.Mode.SRC_IN)

        if (post.stars.containsKey(uid)) {
            numStarsView.text = "${Math.round(post.starCount * 100.0) / 100.0} (${post.stars[uid]})"
            numStarsView.setCompoundDrawablesWithIntrinsicBounds(itemView.context.getDrawable(R.drawable.ic_star_pink_24dp), null, null, null)
        } else {
            numStarsView.text = "${Math.round(post.starCount * 100.0) / 100.0}"
            numStarsView.setCompoundDrawablesWithIntrinsicBounds(itemView.context.getDrawable(R.drawable.ic_star_border_pink_24dp), null, null, null)
        }

        numStarsView.setOnClickListener(starClickListener)
    }

    private fun getForegroundColor(name: String?): Int {
        return if (TextUtils.isEmpty(name)) {
            ContextCompat.getColor(itemView.context, R.color.google_grey200)
        } else {
            letterTitleColorsDark.getColor(Math.abs(name!!.hashCode()) % letterTitleColorsDark.length(), ContextCompat.getColor(itemView.context, R.color.google_grey200))
        }
    }

    private fun getBackgroundColor(name: String?): Int {
        return if (TextUtils.isEmpty(name)) {
            ContextCompat.getColor(itemView.context, R.color.google_grey200)
        } else {
            letterTitleColors.getColor(Math.abs(name!!.hashCode()) % letterTitleColors.length(), ContextCompat.getColor(itemView.context, R.color.google_grey200))
        }
    }
}
