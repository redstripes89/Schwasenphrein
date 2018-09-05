package de.redstripes.schwasenphrein

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.Navigation
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_change_password.view.*
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context, response: IdpResponse?): Intent {
            val intent = Intent(context, MainActivity::class.java)
            if (response != null) {
                intent.putExtra(Constants.EXTRA_IDP_RESPONSE, response)
            }
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)

        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        val actionbar = supportActionBar
        actionbar?.setDisplayShowCustomEnabled(true)
        actionbar?.setDisplayShowTitleEnabled(false)
    }

    override fun onSupportNavigateUp() = Navigation.findNavController(this, R.id.nav_host).navigateUp()

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.menu_main_fragment_logout -> consume { AuthUI.getInstance().signOut(this).addOnCompleteListener { moveToStartupActivity() } }
        R.id.menu_main_fragment_change_password -> consume { showPasswordDialog() }
        else -> false
    }

    private fun moveToStartupActivity() {
        startActivity(StartupActivity.createIntent(this))
        finishAffinity()
    }

    private fun showPasswordDialog() {
        val parent = findViewById<CoordinatorLayout>(R.id.coordinator_layout)
        val view = layoutInflater.inflate(R.layout.fragment_change_password, parent, false)
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.title_change_password))
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.cmd_ok)) { _, _ ->
                    val user = FirebaseAuth.getInstance().currentUser
                    val email = user?.email
                    if (email == null || TextUtils.isEmpty(email))
                        return@setPositiveButton

                    val oldPassword = view.change_password_old.text.toString()
                    if(TextUtils.isEmpty(oldPassword)) {
                        view.change_password_old.error = getString(R.string.info_required)
                        return@setPositiveButton
                    }

                    val newPassword = view.change_password_new.text.toString()
                    val pattern = Pattern.compile("[A-Za-z0-9]{6,}")
                    val matcher = pattern.matcher(newPassword)
                    if (!matcher.matches()) {
                        view.change_password_new.error = getString(R.string.info_password_restrictions)
                        return@setPositiveButton
                    }

                    val credential = EmailAuthProvider.getCredential(email, oldPassword)
                    user.reauthenticate(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            user.updatePassword(newPassword).addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Toast.makeText(applicationContext, getString(R.string.error_password_update_failure), Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(applicationContext, getString(R.string.info_password_sucessfully_updated), Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(applicationContext, getString(R.string.error_authentication_failed), Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton(getString(R.string.cmd_cancel)) { _, _ -> }
                .create()
                .show()
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }
}
