package co.nordprojects.lantern.search

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import co.nordprojects.lantern.home.HomeActivity
import co.nordprojects.lantern.R

class ProjectorSearchActivity : AppCompatActivity(), ProjectorSelectFragment.OnProjectorSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projector_search)

    }

    override fun onProjectorSelected(position: Int) {
        showHomeActivity()
    }

    private fun showProjectorSearchFragment() {
        val searchFragment = ProjectorSearchFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, searchFragment)
        fragmentTransaction.commit()
    }

    private fun showProjectorListFragment() {
        val listFragment = ProjectorSelectFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, listFragment)
        fragmentTransaction.commit()
    }

    private fun showHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}
