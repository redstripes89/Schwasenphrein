package de.redstripes.schwasenphrein

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.redstripes.schwasenphrein.models.Post
import de.redstripes.schwasenphrein.viewholder.PostViewHolder

class MainFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private var database: DatabaseReference?=null
    private var recyclerView: RecyclerView?=null
    private var adapter: FirebaseRecyclerAdapter<Post, PostViewHolder>?=null

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

        return rootView
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.menu_main_fragment_refresh -> consume { refresh() }
        R.id.menu_main_fragment_logout -> consume { logout() }
        else -> false
    }

    private fun refresh(){

    }

    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        //Navigation.findNavController(activity!!, R.id.nav_host).navigate(R.id.signInFragment)
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }
}
