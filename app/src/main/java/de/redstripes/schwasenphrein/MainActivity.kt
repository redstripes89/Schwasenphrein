package de.redstripes.schwasenphrein

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.Navigation
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context, response: IdpResponse?): Intent {
            val intent = Intent(context, MainActivity::class.java)
            if(response != null) {
                intent.putExtra(Constants.EXTRA_IDP_RESPONSE, response)
            }
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        val actionbar = supportActionBar
        actionbar?.setDisplayShowCustomEnabled(true)
        actionbar?.setDisplayShowTitleEnabled(false)
    }

    override fun onSupportNavigateUp() = Navigation.findNavController(this, R.id.nav_host).navigateUp()

    override fun onOptionsItemSelected(item: MenuItem?)= when(item?.itemId){
        R.id.menu_main_fragment_logout -> consume { AuthUI.getInstance().signOut(this).addOnCompleteListener { moveToStartupActivity() } }
        else -> false
    }

    private fun moveToStartupActivity() {
        startActivity(StartupActivity.createIntent(this))
        finishAffinity()
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }
}
