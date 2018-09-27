package de.redstripes.schwasenphrein.fragments

import android.content.res.TypedArray
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter.items
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.utils.ComparableItemListImpl
import com.mikepenz.materialize.MaterializeBuilder
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.adapters.StickyHeaderAdapter
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
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class MainFragment : Fragment(), AnkoLogger {

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private var parent: ViewGroup? = null
    private var database: DatabaseReference? = null
    private var recyclerView: RecyclerView? = null
    private var spinner: AppCompatSpinner? = null

    private val itemListImpl: ComparableItemListImpl<PostItem> = ComparableItemListImpl(getComparator())
    private val itemAdapter: ItemAdapter<PostItem> = ItemAdapter(itemListImpl)
    private val headerAdapter: ItemAdapter<PostItem> = ItemAdapter()
    private val fastAdapter: FastAdapter<PostItem> = FastAdapter.with(Arrays.asList(itemAdapter, headerAdapter))

    private val keyToId: MutableMap<String, Long> = HashMap()
    private var letterTitleColors: TypedArray? = null
    private var letterTitleColorsDark: TypedArray? = null

    //@SortingStrategy
    private var sortingStrategy: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        letterTitleColors = context!!.resources.obtainTypedArray(R.array.letter_tile_colors)
        letterTitleColorsDark = context!!.resources.obtainTypedArray(R.array.letter_tile_colors_dark)

        sortingStrategy = if (savedInstanceState != null) {
            toSortingStrategy(savedInstanceState.getInt("sorting_strategy"))
        } else {
            -1
        }
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
        ArrayAdapter.createFromResource(context!!, R.array.sorting_options, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner?.adapter = adapter
        }

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, id: Long) {
                if (recyclerView == null)
                    return

                sortingStrategy = when (pos) {
                    in 0..3 -> pos
                    else -> -1
                }
                itemListImpl.withComparator(getComparator())
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerView?.layoutManager = LinearLayoutManager(activity!!)
        recyclerView?.itemAnimator = DefaultItemAnimator()

        fastAdapter.setHasStableIds(true)
        fastAdapter.withEventHook(object : ClickEventHook<PostItem>() {
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

                if (key == null)
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

        val stickyHeaderAdapter = StickyHeaderAdapter()
        recyclerView?.adapter = stickyHeaderAdapter.wrap(fastAdapter)

        val decoration = StickyRecyclerHeadersDecoration(stickyHeaderAdapter)
        recyclerView?.addItemDecoration(decoration)
        fastAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
            override fun onChanged() {
                decoration.invalidateHeaders()
            }
        })

        val postsQuery = database?.child("posts")?.orderByChild("date")
        postsQuery?.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
                warn("postsChildEvents:onCancelled " + error.toException())
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                // do nothing
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                if (!keyToId.containsKey(dataSnapshot.key))
                    return

                val id = keyToId[dataSnapshot.key]!!
                val item = getItemForId(id) ?: return
                val post = dataSnapshot.getValue(Post::class.java) ?: return

                item.update(post)
                fastAdapter.notifyItemChanged(fastAdapter.getPosition(item))
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val post = dataSnapshot.getValue(Post::class.java)
                if (post != null) {
                    val id = ThreadLocalRandom.current().nextLong()
                    keyToId[dataSnapshot.key!!] = id
                    itemAdapter.add(PostItem(id, post, letterTitleColors, letterTitleColorsDark))
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                if (!keyToId.containsKey(dataSnapshot.key))
                    return

                val id = keyToId[dataSnapshot.key]!!
                val item = getItemForId(id) ?: return
                itemAdapter.remove(itemAdapter.getAdapterPosition(item))
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("sorting_strategy", sortingStrategy)
        super.onSaveInstanceState(outState)
    }

    private fun toSortingStrategy(value: Int): Int = value

    @Nullable
    private fun getComparator(): Comparator<PostItem>? {
        when (sortingStrategy) {
            0 -> return DateComparatorDescending()
            1 -> return DateComparatorAscending()
            2 -> return RatingComparatorDescending()
            3 -> return RatingComparatorAscending()
            -1 -> return null
        }

        throw RuntimeException("This sortingStrategy is not supported.")
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
        for (item in itemAdapter.adapterItems) {
            if (item.id == id)
                return item
        }

        return null
    }

    private fun getUid() = FirebaseAuth.getInstance().currentUser!!.uid
    private fun getUserName() = FirebaseAuth.getInstance().currentUser!!.displayName

    private class DateComparatorAscending : Comparator<PostItem> {
        override fun compare(a: PostItem?, b: PostItem?): Int {
            if(a == null || b == null)
                return 0
            val aDate = a.post.date
            val bDate = b.post.date
            if(aDate == null || bDate == null)
                return 0
            return aDate.compareTo(bDate)
        }
    }

    private class DateComparatorDescending : Comparator<PostItem> {
        override fun compare(a: PostItem?, b: PostItem?): Int {
            if(a == null || b == null)
                return 0
            val aDate = a.post.date
            val bDate = b.post.date
            if(aDate == null || bDate == null)
                return 0
            return bDate.compareTo(aDate)
        }
    }

    private class RatingComparatorAscending : Comparator<PostItem> {
        override fun compare(a: PostItem?, b: PostItem?): Int {
            if(a == null || b == null)
                return 0
            return a.post.starCount.compareTo(b.post.starCount)
        }
    }

    private class RatingComparatorDescending : Comparator<PostItem> {
        override fun compare(a: PostItem?, b: PostItem?): Int {
            if(a == null || b == null)
                return 0
            return b.post.starCount.compareTo(a.post.starCount)
        }
    }
}
