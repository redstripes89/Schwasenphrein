package de.redstripes.schwasenphrein

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.redstripes.schwasenphrein.models.User

class SignUpFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    // UI elements
    private var textName: EditText? = null
    private var textMail: EditText? = null
    private var textPassword: EditText? = null
    private var btnSignUp: Button? = null
    private var progressBar: ProgressBar? = null

    // Firebase
    private var databaseReference: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance()
        databaseReference = database!!.reference.child("users")
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textName = view.findViewById(R.id.sign_up_name) as EditText
        textMail = view.findViewById(R.id.sign_up_mail) as EditText
        textPassword = view.findViewById(R.id.sign_up_password) as EditText
        btnSignUp = view.findViewById(R.id.sign_up_btn) as Button

        btnSignUp!!.setOnClickListener { createNewAccount() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() = SignUpFragment()
    }

    private fun createNewAccount() {
        val name = textName?.text.toString()
        val mail = textMail?.text.toString()

        if (!validateForm(name, mail)) {
            return
        }

        showProgress()
        val password = textPassword?.text.toString()

        auth?.createUserWithEmailAndPassword(mail, password)
                ?.addOnCompleteListener(activity!!) { task: Task<AuthResult> ->
                    hideProgress()
                    verifyEmail()
                    if (task.isSuccessful) {
                        val user = User(name)
                        databaseReference?.child(auth!!.currentUser!!.uid)?.setValue(user)

                        // go to posts fragment
                        // finish() ?
                    } else {
                        Toast.makeText(activity, "Sign Up Failed", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun validateForm(name: String, mail: String): Boolean {
        var result = true

        if (TextUtils.isEmpty(name)) {
            textName?.error = "Required"
            result = false
        } else {
            textName?.error = null
        }

        if (TextUtils.isEmpty(mail)) {
            textMail?.error = "Required"
            result = false
        } else {
            textMail?.error = null
        }

        return result
    }

    private fun showProgress() {
        if (progressBar == null) {
            progressBar = ProgressBar(context)
        }

        progressBar?.visibility = View.VISIBLE
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private fun hideProgress() {
        progressBar?.visibility = View.GONE
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private fun verifyEmail() {
        val user = auth!!.currentUser;
        user!!.sendEmailVerification()
                .addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(activity, "Verification email sent to " + user.email, Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.exception)
                        Toast.makeText(activity, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                    }
                }
    }
}