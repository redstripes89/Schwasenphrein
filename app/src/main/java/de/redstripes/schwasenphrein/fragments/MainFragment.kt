package de.redstripes.schwasenphrein.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.helpers.Helper
import de.redstripes.schwasenphrein.models.Post
import de.redstripes.schwasenphrein.viewholder.PostViewHolder
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.fragment_new_post.view.*
import kotlinx.android.synthetic.main.fragment_rating.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.warn
import org.threeten.bp.LocalDateTime
import java.util.*

class MainFragment : Fragment(), AnkoLogger {

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private var parent: ViewGroup? = null
    private var database: DatabaseReference? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: FirebaseRecyclerAdapter<Post, PostViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        parent = container
        val rootView = inflater.inflate(R.layout.fragment_main, parent, false)

        database = FirebaseDatabase.getInstance().reference

        recyclerView = rootView.findViewById(R.id.main_posts)
        recyclerView?.setHasFixedSize(true)

        rootView.main_fab_add_post.setOnClickListener {
            onFabClicked()
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

    private fun onFabClicked() {
        val view = layoutInflater.inflate(R.layout.fragment_new_post, parent, false)
        ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, resources.getStringArray(R.array.autocomplete_persons)).also { view.new_post_person.setAdapter(it) }

        AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.title_new_post))
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.cmd_add)) { _, _ ->
                    val person = view.new_post_person.text.toString()
                    val text = view.new_post_text.text.toString()

                    if (TextUtils.isEmpty(person)) {
                        view.new_post_person.error = getString(R.string.info_required)
                    }

                    if (TextUtils.isEmpty(text)) {
                        view.new_post_text.error = getString(R.string.info_required)
                        return@setPositiveButton
                    }

                    addNewPost(person, text)
                }
                .setNegativeButton(getString(R.string.cmd_cancel)) { _, _ -> }
                .create()
                .show()
    }

    private fun addNewPost(person: String, text: String) {
        Toast.makeText(activity, getString(R.string.status_posting), Toast.LENGTH_SHORT).show()

        val uid = getUid()
        val database = FirebaseDatabase.getInstance().reference
        database.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue<de.redstripes.schwasenphrein.models.User>(de.redstripes.schwasenphrein.models.User::class.java)

                if (user == null) {
                    Toast.makeText(activity, getString(R.string.error_could_not_get_user), Toast.LENGTH_SHORT).show()
                    error("User $uid is unexpectedly null")
                } else {
                    val key = database.child("posts").push().key
                    val post = Post(uid, person, text, Helper.dateToString(LocalDateTime.now()))
                    val postValues = post.toMap()
                    val childUpdates = HashMap<String, Any>()
                    childUpdates["/posts/$key"] = postValues
                    database.updateChildren(childUpdates)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                warn("getUser:onCancelled " + error.toException())
            }
        })
    }

    private fun onStarClicked(postRef: DatabaseReference, userRating: Float) {
        var rating = userRating
        val view = layoutInflater.inflate(R.layout.fragment_rating, parent, false)
        view.rating_bar.rating = rating

        AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.title_my_rating))
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.cmd_ok)) { _, _ ->
                    rating = view.rating_bar.rating

                    postRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(mutableData: MutableData): Transaction.Result {
                            val p = mutableData.getValue(Post::class.java)
                                    ?: return Transaction.success(mutableData)

                            p.stars[getUid()] = rating
                            p.starCount = p.stars.values.average().toFloat()

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
                .setNegativeButton(getString(R.string.cmd_cancel)) { _, _ -> }
                .create()
                .show()
    }

    fun getUid() = FirebaseAuth.getInstance().currentUser!!.uid
}
