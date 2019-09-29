package com.zeroinfinity.powerengine.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mancj.materialsearchbar.MaterialSearchBar
import com.zeroinfinity.powerengine.R
import com.zeroinfinity.powerengine.adapters.AppEngineAdapter
import com.zeroinfinity.powerengine.helpers.AppEngineFABHelper
import com.zeroinfinity.powerengine.helpers.AppHelper
import com.zeroinfinity.powerengine.objects.App
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_app_engine.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class AppEngineFragment : Fragment(), MaterialSearchBar.OnSearchActionListener {
    private lateinit var fragment: View
    private lateinit var fragmentActivity: FragmentActivity
    private var isSearch = false

    companion object {
        fun loadData(fragment: View, activity: FragmentActivity, search: String, context: Context) {
            fragment.swiperefresh.isRefreshing = true

            GlobalScope.launch {
                val apps: MutableList<App> = AppHelper(context).getAllApps(search)

                activity.runOnUiThread {
                    fragment.recycler_view.layoutAnimation = AnimationUtils
                        .loadLayoutAnimation(context, R.anim.layout_enter_anim)
                    fragment.recycler_view.scheduleLayoutAnimation()
                    fragment.recycler_view.adapter =
                        AppEngineAdapter(context, activity, apps, fragment)

                    if (fragment.battery_fab.isShown)
                        AppEngineFABHelper.hideMenuFAB(fragment, context)
                    fragment.profiles_fab.hide()
                    fragment.swiperefresh.isRefreshing = false
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragment = inflater.inflate(R.layout.fragment_app_engine, container, false)
        fragmentActivity = activity!!
        setup(fragment)
        return fragment
    }

    private fun setup(fragment: View) {
        requireActivity().toolbar.elevation = 0.toFloat()
        requireActivity().searchBar.apply {
            setOnSearchActionListener(this@AppEngineFragment)
            visibility = View.VISIBLE
            inflateMenu(R.menu.app_engine_menu)
            menu.setOnMenuItemClickListener {
                menuItemListener(it)
            }
        }

        fragment.recycler_view.layoutManager = LinearLayoutManager(requireContext())
        fragment.swiperefresh.setOnRefreshListener {
            loadData(
                fragment,
                fragmentActivity,
                "",
                requireContext()
            )
        }
        fragment.swiperefresh.post { loadData(fragment, fragmentActivity, "", requireContext()) }

        fragment.profiles_fab.setOnClickListener {
            if (!fragment.battery_fab.isShown && fragment.profiles_fab.isShown)
                AppEngineFABHelper.showMenuFAB(fragment, requireContext())
            else if (fragment.battery_fab.isShown && fragment.profiles_fab.isShown)
                AppEngineFABHelper.hideMenuFAB(fragment, requireContext())
        }
    }

    private fun menuItemListener(it: MenuItem): Boolean {
        GlobalScope.launch {
            when (it.itemId) {
                R.id.battery -> {
                    setAllApps("battery")
                }

                R.id.balanced -> {
                    setAllApps("balanced")
                }

                R.id.performance -> {
                    setAllApps("performance")
                }
            }
        }

        return false
    }

    private fun setAllApps(profile: String) {
        val apps: MutableList<App> = AppHelper(requireContext()).getAllApps("")

        for (app in apps) {
            App.setProfile(app.packageName, profile, requireContext())
        }

        requireActivity().runOnUiThread {
            var message = ""

            when (profile) {
                "battery" -> message = getString(R.string.setAllBatteryNotif)
                "balanced" -> message = getString(R.string.setAllBalancedNotif)
                "performance" -> message = getString(R.string.setAllPerformanceNotif)
            }
            Snackbar.make(
                activity!!.fragment_container, message, Snackbar.LENGTH_SHORT
            ).show()

            loadData(fragment, requireActivity(), "", requireContext())
        }
    }

    override fun onSearchStateChanged(enabled: Boolean) {
        if (!enabled && isSearch) {
            isSearch = false
            loadData(fragment, fragmentActivity, "", requireContext())
        }
    }

    override fun onSearchConfirmed(text: CharSequence) {
        isSearch = true
        loadData(fragment, fragmentActivity, text.toString(), requireContext())
    }

    override fun onButtonClicked(buttonCode: Int) {}
}
