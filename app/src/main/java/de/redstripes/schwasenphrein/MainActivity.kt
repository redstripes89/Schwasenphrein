package de.redstripes.schwasenphrein

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        var actionbar = supportActionBar
        actionbar?.setDisplayShowCustomEnabled(true)
        actionbar?.setDisplayShowTitleEnabled(false)
    }

    override fun onSupportNavigateUp()
        = Navigation.findNavController(this, R.id.nav_host).navigateUp()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.menu_main, menu)
        return true
    }
}
