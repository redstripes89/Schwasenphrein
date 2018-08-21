package de.redstripes.schwasenphrein.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.redstripes.schwasenphrein.MainActivity
import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.models.Post
import de.redstripes.schwasenphrein.viewholder.PostViewHolder
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.fragment_rating.view.*

class MainFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private var database: DatabaseReference? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: FirebaseRecyclerAdapter<Post, PostViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_main, container, false)

        database = FirebaseDatabase.getInstance().reference

        recyclerView = rootView.findViewById(R.id.main_posts)
        recyclerView?.setHasFixedSize(true)

        rootView.main_fab_add_post.setOnClickListener {
            val newPostFragment = NewPostFragment()
            val transaction = childFragmentManager?.beginTransaction()
            transaction?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction?.add(R.id.main_container, newPostFragment)?.addToBackStack(null)?.commit()
        }

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val manager = LinearLayoutManager(activity!!)
        manager.reverseLayout = true
        manager.stackFromEnd = true
        recyclerView?.layoutManager = manager

        val postsQuery = database?.child("posts")?.limitToFirst(100)
        val options = FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery!!, Post::class.java)
                .setLifecycleOwner(viewLifecycleOwner)
                .build()
        adapter = MainFragmentRecyclerAdapter(options, activity!!, database!!, ::onStarClicked)
        recyclerView?.adapter = adapter
    }

    private fun onStarClicked(postRef: DatabaseReference) {

        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val p = mutableData.getValue(Post::class.java)
                        ?: return Transaction.success(mutableData)

                val view = layoutInflater.inflate(R.layout.fragment_rating, null)

                view.ratingView.setRatingChangeListener { previousRating, newRating ->
                    view.emotionView.setRating(previousRating, newRating)
                    view.gradientBackgroundView.changeBackground(previousRating, newRating)
                }

                activity?.runOnUiThread {
                    AlertDialog.Builder(requireContext())
                            .setMessage("My Rating")
                            .setView(view)
                            .setCancelable(true)
                            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which -> })
                            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> })
                            .create()
                            .show()
                }

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.stars.remove(getUid())
                    p.starCount = p.stars.values.average()
                } else {
                    // Star the post and add self to stars
                    p.stars[getUid()] = 1
                    p.starCount = p.stars.values.average()
                }

                // Set value and report transaction success
                mutableData.value = p
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {
                // Transaction completed
                //if (!b)
                //debug("postTransaction:onComplete:$databaseError")
            }
        })
    }

    fun getUid() = FirebaseAuth.getInstance().currentUser!!.uid

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }
}