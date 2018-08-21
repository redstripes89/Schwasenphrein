package de.redstripes.schwasenphrein.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.redstripes.schwasenphrein.R
import de.redstripes.schwasenphrein.models.Post
import de.redstripes.schwasenphrein.viewholder.PostViewHolder
import kotlinx.android.synthetic.main.fragment_main.view.*

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
        adapter = MainFragmentRecyclerAdapter(options, activity!!, database!!)
        recyclerView?.adapter = adapter
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }
}
