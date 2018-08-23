package de.redstripes.schwasenphrein

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.redstripes.schwasenphrein.models.User
import kotlinx.android.synthetic.main.activity_startup.*
import org.jetbrains.anko.AnkoLogger
import java.util.*

class StartupActivity : AppCompatActivity(), AnkoLogger {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, StartupActivity::class.java)
        }
    }

    private val responseCodeSignIn = 4711
    private val authProviders = Arrays.asList(AuthUI.IdpConfig.EmailBuilder().build())!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isUserLoggedIn()) {
            moveToMainActivity(null)
        } else {
            setContentView(R.layout.activity_startup)

            setSupportActionBar(startup_toolbar)

            val actionbar = supportActionBar
            actionbar?.setDisplayShowCustomEnabled(true)
            actionbar?.setDisplayShowTitleEnabled(false)

            startup_image_pig.setOnClickListener { startSignInProcess() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            responseCodeSignIn -> {
                val response = IdpResponse.fromResultIntent(data)
                if (resultCode == Activity.RESULT_OK) {
                    if(response?.isNewUser == true) {
                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        val user = User(firebaseUser?.displayName)
                        FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).setValue(user)
                    }
                    moveToMainActivity(response)
                } else {
                    if (response == null) {
                        Snackbar.make(startup_root, "Sign In was cancelled", Toast.LENGTH_SHORT)
                        return
                    }

                    if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                        Snackbar.make(startup_root, "No internet connection", Toast.LENGTH_SHORT)
                        return
                    }

                    Snackbar.make(startup_root, "Unknown error", Toast.LENGTH_SHORT)
                    error("Sign In error: " + response.error)
                }
            }
        }
    }

    private fun isUserLoggedIn() = FirebaseAuth.getInstance().currentUser != null

    private fun moveToMainActivity(response: IdpResponse?) {
        startActivity(MainActivity.createIntent(this, response))
        finish()
    }

    private fun startSignInProcess() {
        AuthUI.getInstance().silentSignIn(this, authProviders).addOnCompleteListener {
            if (it.isSuccessful)
                moveToMainActivity(null)
            else
                startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(authProviders)
                        .setTheme(R.style.SignInTheme)
                        .build(),
                        responseCodeSignIn)
        }

    }
}
