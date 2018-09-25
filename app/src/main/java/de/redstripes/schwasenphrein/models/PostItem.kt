package de.redstripes.schwasenphrein.models

import android.content.res.TypedArray
import android.view.View
import com.mikepenz.fastadapter.items.AbstractItem
import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.viewholder.PostItemViewHolder


class PostItem(val id: Long, var post: Post, private val letterTitleColors: TypedArray?, private val letterTitleColorsDark: TypedArray?) : AbstractItem<PostItem, PostItemViewHolder>() {

    override fun getIdentifier(): Long = id

    override fun getType(): Int = 0

    override fun getLayoutRes(): Int = R.layout.item_post

    override fun getViewHolder(v: View): PostItemViewHolder = PostItemViewHolder(v, letterTitleColors, letterTitleColorsDark)

    fun update(post: Post) {
        this.post = post
    }
}