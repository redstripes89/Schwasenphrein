package de.redstripes.schwasenphrein.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.models.Post
import de.redstripes.schwasenphrein.viewholder.PostViewHolder

class MainFragmentRecyclerAdapter(options: FirebaseRecyclerOptions<Post>, private val context: Context, private val database: DatabaseReference, private val onStarClicked: (DatabaseReference, Float) -> Unit) : FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PostViewHolder(inflater.inflate(R.layout.item_post, parent, false), context.resources.obtainTypedArray(R.array.letter_tile_colors), context.resources.obtainTypedArray(R.array.letter_tile_colors_dark))
    }

    override fun onBindViewHolder(viewHolder: PostViewHolder, position: Int, model: Post) {
        val postRef = getRef(position)

        // Bind Post to ViewHolder, setting OnClickListener for the star button
        viewHolder.bindToPost(model, getUid(), View.OnClickListener {
            val key = postRef.key
            val uid = model.uid
            val username = getUserName()

            if (model.person == username) {
                Toast.makeText(context, context.getString(R.string.info_cannot_rate_own_post), Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            var userRating = 0f
            if (model.stars.containsKey(getUid()))
                userRating = model.stars[getUid()] ?: 0f

            if (key == null || uid == null)
                return@OnClickListener

            val globalPostRef = database.child("posts").child(key)
            onStarClicked(globalPostRef, userRating)
        })
    }

    private fun getUserName() = FirebaseAuth.getInstance().currentUser!!.displayName
    private fun getUid() = FirebaseAuth.getInstance().currentUser!!.uid
}