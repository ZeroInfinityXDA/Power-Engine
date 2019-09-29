package com.zeroinfinity.powerengine.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.topjohnwu.superuser.Shell
import com.zeroinfinity.powerengine.*
import com.zeroinfinity.powerengine.Settings.darkTheme
import com.zeroinfinity.powerengine.fragments.AppEngineFragment
import com.zeroinfinity.powerengine.fragments.MainFragment
import com.zeroinfinity.powerengine.fragments.MoreFragment
import com.zeroinfinity.powerengine.fragments.ProfilesFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private var toolbarElevation: Float = 0.toFloat()

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    searchBar.visibility = View.GONE
                    toolbar.elevation = toolbarElevation
                    toolbar.title = getString(R.string.title_dashboard)
                    toolbar.menu.clear()
                    if (supportFragmentManager.backStackEntryCount > 1)
                        supportFragmentManager.popBackStack("root", 0)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_appengine -> {
                    displaySelectedFragment(AppEngineFragment(), this, "appEngine")
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_profiles -> {
                    if (!Shell.rootAccess()) {
                        Snackbar.make(
                            fragment_container,
                            getString(R.string.no_root_detected),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        searchBar.visibility = View.GONE
                        toolbar.elevation = toolbarElevation
                        toolbar.title = getString(R.string.title_profiles)
                        toolbar.menu.clear()
                        displaySelectedFragment(ProfilesFragment(), this, "profiles")
                        return@OnNavigationItemSelectedListener true
                    }
                }
                R.id.navigation_settings -> {
                    searchBar.visibility = View.GONE
                    toolbar.elevation = toolbarElevation
                    toolbar.title = getString(R.string.title_more)
                    toolbar.menu.clear()
                    displaySelectedFragment(MoreFragment(), this, "more")
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack("root", 0)
            nav_view.selectedItemId = R.id.navigation_dashboard
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (darkTheme)
            setTheme(R.style.AppTheme_Dark)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.title = getString(R.string.title_dashboard)
        displaySelectedFragment(MainFragment(), this, "root")
        setSupportActionBar(toolbar)
        toolbarElevation = toolbar.elevation

        nav_view.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        if (!Shell.rootAccess()) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle(R.string.no_root_dialog_title)
                .setMessage(R.string.no_root_dialog_desc)
                .setNeutralButton(android.R.string.ok) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()
        }
    }

    private fun displaySelectedFragment(
        fragment: Fragment,
        activity: AppCompatActivity,
        tag: String
    ) {
        GlobalScope.launch {
            val fragmentTransaction = activity.supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            fragmentTransaction.replace(R.id.fragment_container, fragment)
            fragmentTransaction.addToBackStack(tag)
            fragmentTransaction.commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == APP_BACKUP || resultCode == PROFILE_BACKUP ||
            resultCode == APP_RESTORE || resultCode == PROFILE_RESTORE
        ) {
            if (data != null) {
                displaySelectedFragment(MoreFragment(), this, "more")
                nav_view.selectedItemId = R.id.navigation_settings
                val fragment = supportFragmentManager.findFragmentByTag("more")
                fragment!!.onActivityResult(requestCode, resultCode, data)
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
