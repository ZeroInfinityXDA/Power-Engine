package com.zeroinfinity.powerengine.helpers

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.zeroinfinity.powerengine.R
import kotlinx.android.synthetic.main.fragment_app_engine.view.*

object AppEngineFABHelper {
    fun hideMenuFAB(fragment: View, context: Context) {
        val fabClose: Animation = AnimationUtils.loadAnimation(context, R.anim.fab_close)
        val fabAntiClock: Animation =
            AnimationUtils.loadAnimation(context, R.anim.fab_rotate_anticlock)

        fragment.text_battery.startAnimation(fabClose)
        fragment.text_balanced.startAnimation(fabClose)
        fragment.text_performance.startAnimation(fabClose)
        fragment.text_whitelist.startAnimation(fabClose)
        fragment.profiles_fab.startAnimation(fabAntiClock)

        fragment.battery_fab.hide()
        fragment.balanced_fab.hide()
        fragment.performance_fab.hide()
        fragment.whitelist_fab.hide()

    }

    fun showMenuFAB(fragment: View, context: Context) {
        val fabOpen: Animation = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        val fabClock: Animation = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_clock)

        fragment.text_battery.startAnimation(fabOpen)
        fragment.text_balanced.startAnimation(fabOpen)
        fragment.text_performance.startAnimation(fabOpen)
        fragment.text_whitelist.startAnimation(fabOpen)
        fragment.profiles_fab.startAnimation(fabClock)

        fragment.battery_fab.show()
        fragment.balanced_fab.show()
        fragment.performance_fab.show()
        fragment.whitelist_fab.show()
    }
}