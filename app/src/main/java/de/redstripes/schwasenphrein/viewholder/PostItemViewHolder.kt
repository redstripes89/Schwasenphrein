package de.redstripes.schwasenphrein.viewholder

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.fastadapter.FastAdapter
import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.models.PostItem
import org.jetbrains.anko.textColor

class PostItemViewHolder(view: View, private val letterTitleColors: TypedArray?, private val letterTitleColorsDark: TypedArray?) : FastAdapter.ViewHolder<PostItem>(view) {

    private val personView: TextView = view.findViewById(R.id.post_person)
    internal var numStarsView: TextView = view.findViewById(R.id.post_num_stars)
    private val textView: TextView = view.findViewById(R.id.post_text)
    private val iconView: ImageView = view.findViewById(R.id.post_icon)
    private val iconTextView: TextView = view.findViewById(R.id.post_icon_text)
    private val dateTextView: TextView = view.findViewById(R.id.post_date)

    @SuppressLint("SetTextI18n")
    override fun bindView(item: PostItem, payloads: List<Any>) {
        personView.text = item.post.person
        textView.text = item.post.text
        dateTextView.text = item.post.date

        iconTextView.text = item.post.person?.substring(0, 1)
        iconTextView.textColor = getForegroundColor(item.post.person)
        iconTextView.alpha = 184f / 255f
        iconView.setColorFilter(getBackgroundColor(item.post.person), PorterDuff.Mode.SRC_IN)

        val uid = getUid()
        if (item.post.stars.containsKey(uid)) {
            numStarsView.text = "${Math.round(item.post.starCount * 100.0) / 100.0} (${item.post.stars[uid]})"
            numStarsView.setCompoundDrawablesWithIntrinsicBounds(itemView.context.getDrawable(R.drawable.ic_star_pink_24dp), null, null, null)
        } else {
            numStarsView.text = "${Math.round(item.post.starCount * 100.0) / 100.0}"
            numStarsView.setCompoundDrawablesWithIntrinsicBounds(itemView.context.getDrawable(R.drawable.ic_star_border_pink_24dp), null, null, null)
        }
    }

    override fun unbindView(item: PostItem) {
        personView.text = null
        textView.text = null
        dateTextView.text = null
        iconTextView.text = null
        numStarsView.text = null
    }

    private fun getForegroundColor(name: String?): Int {
        return if (TextUtils.isEmpty(name) || letterTitleColorsDark == null) {
            ContextCompat.getColor(itemView.context, R.color.google_grey200)
        } else {
            letterTitleColorsDark.getColor(Math.abs(name!!.hashCode()) % letterTitleColorsDark.length(), ContextCompat.getColor(itemView.context, R.color.google_grey200))
        }
    }

    private fun getBackgroundColor(name: String?): Int {
        return if (TextUtils.isEmpty(name) || letterTitleColors == null) {
            ContextCompat.getColor(itemView.context, R.color.google_grey200)
        } else {
            letterTitleColors.getColor(Math.abs(name!!.hashCode()) % letterTitleColors.length(), ContextCompat.getColor(itemView.context, R.color.google_grey200))
        }
    }

    private fun getUid() = FirebaseAuth.getInstance().currentUser!!.uid
}