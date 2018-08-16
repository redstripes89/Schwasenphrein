package de.redstripes.schwasenphrein

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    // UI elements
    private var textMail: EditText? = null
    private var btnResetPassword: Button? = null

    // Firebase
    private var auth: FirebaseAuth? = null

    // Navigation
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        navController = NavHostFragment.findNavController(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textMail = view.findViewById(R.id.forgot_password_mail) as EditText
        btnResetPassword = view.findViewById(R.id.forgot_password_btn) as Button

        btnResetPassword!!.setOnClickListener { sendPasswordResetMail() }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ForgotPasswordFragment()
    }

    private fun sendPasswordResetMail() {
        val mail = textMail?.text.toString()
        if (!TextUtils.isEmpty(mail)) {
            textMail?.error = null
            auth!!.sendPasswordResetEmail(mail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email sent.")
                            Toast.makeText(activity, "Email sent.", Toast.LENGTH_SHORT).show()
                            navController?.navigate(R.id.action_forgotPasswordFragment_to_signInFragment)
                        } else {
                            Log.w(TAG, task.exception!!.message)
                            Toast.makeText(activity, "No user found with this email.", Toast.LENGTH_SHORT).show()
                        }
                    }
        } else {
            textMail?.error = "Required"
        }
    }
}
