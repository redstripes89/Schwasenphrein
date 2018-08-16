package de.redstripes.schwasenphrein

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth

class SignInFragment : Fragment() {

    //UI elements
    private var textForgotPassword: TextView? = null
    private var textMail: EditText? = null
    private var textPassword: EditText? = null
    private var btnLogin: Button? = null
    private var btnCreateAccount: Button? = null
    private var progressBar: ProgressBar? = null

    //Firebase
    private var auth: FirebaseAuth? = null

    // Navigation
    private var navController: NavController?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        navController = NavHostFragment.findNavController(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textForgotPassword = view.findViewById<View>(R.id.sign_in_forgot_password) as TextView
        textMail = view.findViewById<View>(R.id.sign_in_mail) as EditText
        textPassword = view.findViewById<View>(R.id.sign_in_password) as EditText
        btnLogin = view.findViewById<View>(R.id.sign_in_btn_login) as Button
        btnCreateAccount = view.findViewById<View>(R.id.sign_in_btn_create_account) as Button

        textForgotPassword!!.setOnClickListener { navController?.navigate(R.id.action_signInFragment_to_forgotPasswordFragment) }
        btnCreateAccount!!.setOnClickListener { navController?.navigate(R.id.action_signInFragment_to_signUpFragment) }
        btnLogin!!.setOnClickListener { loginUser() }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SignInFragment()
    }

    private fun loginUser() {
        val email = textMail?.text.toString()
        val password = textPassword?.text.toString()

        if(!validateForm(email, password)) {
            return
        }
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            showProgress()
            Log.d(TAG, "Logging in user.")

            auth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity!!) { task ->
                        hideProgress()

                        if (task.isSuccessful) {
                            // Sign in success, update UI with signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")

                            navController?.navigate(R.id.action_signInFragment_to_mainActivity)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(activity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
        } else {
            Toast.makeText(activity, "Enter all details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateForm(mail: String, password: String): Boolean {
        var result = true

        if (TextUtils.isEmpty(mail)) {
            textMail?.error = "Required"
            result = false
        } else {
            textMail?.error = null
        }

        if (TextUtils.isEmpty(password)) {
            textPassword?.error = "Required"
            result = false
        } else {
            textPassword?.error = null
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
}
