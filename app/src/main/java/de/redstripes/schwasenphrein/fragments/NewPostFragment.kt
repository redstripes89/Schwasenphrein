package de.redstripes.schwasenphrein.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.firebase.ui.auth.data.model.User
import de.redstripes.schwasenphrein.R
import kotlinx.android.synthetic.main.fragment_new_post.*
import kotlinx.android.synthetic.main.fragment_new_post.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.redstripes.schwasenphrein.models.Post
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn


class NewPostFragment : DialogFragment(), AnkoLogger {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_new_post, container, false)
        rootView.new_post_btn_close.setOnClickListener { dismiss() }
        rootView.new_post_btn_add.setOnClickListener {
            val person = new_post_person.text.toString()
            val text = new_post_text.text.toString()

            if (TextUtils.isEmpty(person)) {
                new_post_person.error = "Required"
            }

            if (TextUtils.isEmpty(text)) {
                new_post_text.error = "Required"
                return@setOnClickListener
            }

            setEditingEnabled(false)
            Toast.makeText(activity, "Posting...", Toast.LENGTH_SHORT).show()

            val uid = getUid()
            val database = FirebaseDatabase.getInstance().reference
            database.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue<de.redstripes.schwasenphrein.models.User>(de.redstripes.schwasenphrein.models.User::class.java)

                    if(user == null) {
                        Toast.makeText(activity, "Error: Could not get user", Toast.LENGTH_SHORT).show()
                        error("User " + uid + " is unexpectedly null")
                    } else {
                        val key = database.child("posts").push().key
                        val post = Post(uid, user.username!!, person, text)
                        val postValues = post.toMap()

                        val childUpdates = HashMap<String, Any>()
                        childUpdates.put("/posts/" + key, postValues)
                        database.updateChildren(childUpdates)
                    }

                    setEditingEnabled(true)
                    dismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                    warn("getUSer:onCancelled " + error.toException())
                    setEditingEnabled(true)
                }
            })
        }
        return rootView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun setEditingEnabled(enabled: Boolean) {
        new_post_person.isEnabled = enabled
        new_post_text.isEnabled = enabled
        new_post_btn_add.isEnabled = enabled
        new_post_btn_close.isEnabled = enabled
    }

    private fun getUid(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
}