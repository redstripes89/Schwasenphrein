package de.redstripes.schwasenphrein.fragments

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.models.Post
import de.redstripes.schwasenphrein.viewholder.PostViewHolder
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

class MainFragmentRecyclerAdapter(options: FirebaseRecyclerOptions<Post>, val context: Context, private val database: DatabaseReference) : FirebaseRecyclerAdapter<Post, PostViewHolder>(options), AnkoLogger {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PostViewHolder(inflater.inflate(R.layout.item_post, parent, false))
    }

    override fun onBindViewHolder(viewHolder: PostViewHolder, position: Int, model: Post) {
        val postRef = getRef(position)

        // Determine if the current user has liked this post and set UI accordingly
        val star: Drawable = if (model.stars.containsKey(getUid())) {
            viewHolder.itemView.context.getDrawable(R.drawable.ic_star_pink_24dp)
        } else {
            viewHolder.itemView.context.getDrawable(R.drawable.ic_star_border_pink_24dp)
        }

        viewHolder.numStarsView.setCompoundDrawablesWithIntrinsicBounds(star, null, null, null)

        // Bind Post to ViewHolder, setting OnClickListener for the star button
        viewHolder.bindToPost(model, View.OnClickListener {
            val key = postRef.key
            val uid = model.uid

            if (key == null || uid == null)
                return@OnClickListener

            val globalPostRef = database.child("posts").child(key)
            onStarClicked(globalPostRef)
        })
    }

    private fun onStarClicked(postRef: DatabaseReference) {
        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val p = mutableData.getValue(Post::class.java)
                        ?: return Transaction.success(mutableData)

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1
                    p.stars.remove(getUid())
                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1
                    p.stars[getUid()] = true
                }

                // Set value and report transaction success
                mutableData.value = p
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {
                // Transaction completed
                if (!b)
                    debug("postTransaction:onComplete:$databaseError")
            }
        })
    }

    fun getUid() = FirebaseAuth.getInstance().currentUser!!.uid
}