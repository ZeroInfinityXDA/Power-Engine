package com.zeroinfinity.powerengine.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.github.florent37.expansionpanel.ExpansionLayout
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection
import com.zeroinfinity.powerengine.R
import com.zeroinfinity.powerengine.fragments.AppEngineFragment
import com.zeroinfinity.powerengine.helpers.AppEngineFABHelper
import com.zeroinfinity.powerengine.objects.App
import kotlinx.android.synthetic.main.app_engine_item.view.*
import kotlinx.android.synthetic.main.fragment_app_engine.view.*

class AppEngineAdapter(
    private val context: Context,
    private val activity: FragmentActivity,
    private var appList: MutableList<App>,
    private var rootFragment: View
) :
    RecyclerView.Adapter<AppHolder>() {
    private val selectedAppList: MutableList<String> = mutableListOf()
    private val expansionLayoutCollection = ExpansionLayoutCollection()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        expansionLayoutCollection.openOnlyOne(true)
        return AppHolder(
            LayoutInflater.from(context).inflate(
                R.layout.app_engine_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = appList.size

    override fun onBindViewHolder(holder: AppHolder, position: Int) {
        expansionLayoutCollection.add(holder.getExpansionLayout())
        holder.bind(appList[position], selectedAppList, context, activity, rootFragment, appList)
    }
}

class AppHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(
        app: App,
        selectedAppList: MutableList<String>,
        context: Context,
        activity: FragmentActivity,
        rootFragment: View,
        appList: MutableList<App>
    ) {
        itemView.expansionLayout.collapse(false)
        itemView.multiSelect.setOnCheckedChangeListener { _, _ -> }
        itemView.appName.text = app.appName
        itemView.packageName.text = app.packageName
        itemView.appIcon.setImageDrawable(app.icon)
        itemView.multiSelect.isChecked = selectedAppList.contains(app.packageName)

        rootFragment.battery_fab.setOnClickListener {
            fabClickListener(rootFragment, selectedAppList, context, activity, "battery")
        }

        rootFragment.balanced_fab.setOnClickListener {
            fabClickListener(rootFragment, selectedAppList, context, activity, "balanced")
        }

        rootFragment.performance_fab.setOnClickListener {
            fabClickListener(rootFragment, selectedAppList, context, activity, "performance")
        }

        rootFragment.whitelist_fab.setOnClickListener {
            fabClickListener(rootFragment, selectedAppList, context, activity, "whitelist")
        }

        itemView.profilesRadioGroup.setOnCheckedChangeListener { _, _ -> }

        when (app.profile) {
            "battery" -> itemView.batteryRadio.isChecked = true.also {
                itemView.currentProfile.text = context.getString(R.string.profile_battery)
            }
            "balanced" -> itemView.balancedRadio.isChecked = true.also {
                itemView.currentProfile.text = context.getString(R.string.profile_balanced)
            }
            "performance" -> itemView.performanceRadio.isChecked = true.also {
                itemView.currentProfile.text = context.getString(R.string.profile_performance)
            }
            "whitelist" -> itemView.whitelistRadio.isChecked = true.also {
                itemView.currentProfile.text = context.getString(R.string.profile_whitelist)
            }
        }

        itemView.profilesRadioGroup.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.batteryRadio -> {
                    App.setProfile(app.packageName, "battery", context)
                    app.profile = "battery"
                    itemView.batteryRadio.isChecked = true
                    itemView.currentProfile.text = context.getString(R.string.profile_battery)
                }

                R.id.balancedRadio -> {
                    App.setProfile(app.packageName, "balanced", context)
                    app.profile = "balanced"
                    itemView.balancedRadio.isChecked = true
                    itemView.currentProfile.text = context.getString(R.string.profile_balanced)
                }

                R.id.performanceRadio -> {
                    App.setProfile(app.packageName, "performance", context)
                    app.profile = "performance"
                    itemView.performanceRadio.isChecked = true
                    itemView.currentProfile.text = context.getString(R.string.profile_performance)
                }

                R.id.whitelistRadio -> {
                    App.setProfile(app.packageName, "whitelist", context)
                    app.profile = "whitelist"
                    itemView.whitelistRadio.isChecked = true
                    itemView.currentProfile.text = context.getString(R.string.profile_whitelist)
                }
            }

            appList[appList.indexOf(app)] = app
            itemView.expansionLayout.collapse(true)
        }

        setCheckChanged(itemView, selectedAppList, context, rootFragment)
    }

    private fun setCheckChanged(
        itemView: View, selectedAppList: MutableList<String>, context: Context,
        rootFragment: View
    ) {
        itemView.appCard.setOnClickListener {
            itemView.multiSelect.toggle()
        }

        itemView.multiSelect.setOnCheckedChangeListener { _, b ->
            val packageName: String = itemView.packageName.text.toString()

            if (b) {
                selectedAppList.add(packageName)

                if (!rootFragment.profiles_fab.isShown)
                    rootFragment.profiles_fab.show()
            } else {
                if (selectedAppList.isNotEmpty()) {
                    selectedAppList.remove(packageName)
                    if (selectedAppList.isEmpty()) {
                        if (rootFragment.profiles_fab.isShown) {
                            if (rootFragment.battery_fab.isShown)
                                AppEngineFABHelper.hideMenuFAB(rootFragment, context)
                            rootFragment.profiles_fab.hide()
                        }
                    }
                }
            }
        }
    }

    private fun fabClickListener(
        fragment: View,
        selectedAppList: MutableList<String>,
        context: Context,
        activity: FragmentActivity,
        profile: String
    ) {
        for (packageName in selectedAppList) {
            App.setProfile(packageName, profile, context)
        }

        AppEngineFragment.loadData(fragment, activity, "", context)
    }

    fun getExpansionLayout(): ExpansionLayout = itemView.expansionLayout
}