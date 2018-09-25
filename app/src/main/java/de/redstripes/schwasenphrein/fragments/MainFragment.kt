package de.redstripes.schwasenphrein.fragments

import android.content.res.TypedArray
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.helpers.Helper
import de.redstripes.schwasenphrein.models.Post
import de.redstripes.schwasenphrein.models.PostItem
import de.redstripes.schwasenphrein.models.User
import de.redstripes.schwasenphrein.viewholder.PostItemViewHolder
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.fragment_new_post.view.*
import kotlinx.android.synthetic.main.fragment_rating.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.warn
import org.threeten.bp.LocalDateTime
import java.util.concurrent.ThreadLocalRandom


enum class SortType {
    DateAsc {
        override fun modifyQuery(query: Query): Query = query.orderByChild("date")
    },
    DateDesc {
        override fun modifyQuery(query: Query): Query = query.orderByChild("date")
    },
    RatingAsc {
        override fun modifyQuery(query: Query): Query = query.orderByChild("starCount")
    },
    RatingDesc {
        override fun modifyQuery(query: Query): Query = query.orderByChild("starCount")
    };

    abstract fun modifyQuery(query: Query): Query
}

class MainFragment : Fragment(), AnkoLogger {

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private var parent: ViewGroup? = null
    private var database: DatabaseReference? = null
    private var recyclerView: RecyclerView? = null
    private var spinner: AppCompatSpinner? = null
    private val fastItemAdapter = FastItemAdapter<PostItem>()
    private val keyToId: MutableMap<String, Long> = HashMap()
    private var letterTitleColors: TypedArray? = null
    private var letterTitleColorsDark: TypedArray? = null

    private var selectedSortingOption: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        letterTitleColors = context!!.resources.obtainTypedArray(R.array.letter_tile_colors)
        letterTitleColorsDark = context!!.resources.obtainTypedArray(R.array.letter_tile_colors_dark)
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

        spinner = rootView.findViewById(R.id.main_spinner_order)
        ArrayAdapter.createFromResource(context, R.array.sorting_options, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner?.adapter = adapter
        }

        /*spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, id: Long) {
                if (recyclerView == null || recyclerViewManager == null || adapter == null)
                    return

                selectedSortingOption = pos
                val reverse = pos % 2 == 0
                if (reverse != recyclerViewManager!!.reverseLayout) {
                    recyclerViewManager!!.reverseLayout = reverse
                    if (reverse)
                        recyclerViewManager!!.scrollToPosition(adapter!!.itemCount-1)
                    else
                        recyclerViewManager!!.scrollToPosition(0)
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }*/

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerView?.layoutManager = LinearLayoutManager(activity!!)

        fastItemAdapter.setHasStableIds(true)
        fastItemAdapter.withEventHook(object : ClickEventHook<PostItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return if (viewHolder is PostItemViewHolder)
                    viewHolder.numStarsView
                else
                    null
            }

            override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<PostItem>, item: PostItem) {
                val key = getKeyForId(item.id)
                val uid = getUid()
                val username = getUserName()

                if (key == null || uid == null)
                    return

                if (item.post.person == username) {
                    Toast.makeText(context, context!!.getString(R.string.info_cannot_rate_own_post), Toast.LENGTH_SHORT).show()
                    return
                }

                var userRating = 0f
                if (item.post.stars.containsKey(uid))
                    userRating = item.post.stars[uid] ?: 0f

                val globalPostRef = database!!.child("posts").child(key)
                onStarClicked(globalPostRef, userRating)
            }

        })
        recyclerView?.adapter = fastItemAdapter

        val postsQuery = database?.child("posts")?.orderByChild("date")
        postsQuery?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                warn("getPosts:onCancelled " + error.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null) {
                        val id = ThreadLocalRandom.current().nextLong()
                        keyToId[postSnapshot.key!!] = id
                        fastItemAdapter.add(PostItem(id, post, letterTitleColors, letterTitleColorsDark))
                    }
                }
            }
        })

        postsQuery?.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
                warn("postsChildEvents:onCancelled " + error.toException())
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                // do nothing
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                if(!keyToId.containsKey(dataSnapshot.key))
                    return

                val id = keyToId[dataSnapshot.key]!!
                val item = getItemForId(id) ?: return
                val post = dataSnapshot.getValue(Post::class.java) ?: return

                item.update(post)
                fastItemAdapter.notifyItemChanged(fastItemAdapter.getPosition(item))
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val post = dataSnapshot.getValue(Post::class.java)
                if (post != null) {
                    val id = ThreadLocalRandom.current().nextLong()
                    keyToId[dataSnapshot.key!!] = id
                    fastItemAdapter.add(PostItem(id, post, letterTitleColors, letterTitleColorsDark))
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                if(!keyToId.containsKey(dataSnapshot.key))
                    return

                val id = keyToId[dataSnapshot.key]!!
                val item = getItemForId(id) ?: return
                fastItemAdapter.remove(fastItemAdapter.getPosition(item))
            }

        })
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
                val user = dataSnapshot.getValue<User>(User::class.java)

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

    private fun getKeyForId(id: Long): String? {
        if (!keyToId.containsValue(id))
            return null

        for (entry in keyToId) {
            if (entry.value == id)
                return entry.key
        }

        return null
    }

    private fun getItemForId(id: Long): PostItem? {
        for (item in fastItemAdapter.adapterItems) {
            if (item.id == id)
                return item
        }

        return null
    }

    private fun getUid() = FirebaseAuth.getInstance().currentUser!!.uid
    private fun getUserName() = FirebaseAuth.getInstance().currentUser!!.displayName
}
