package com.zeroinfinity.powerengine.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zeroinfinity.powerengine.R
import kotlinx.android.synthetic.main.fragment_profiles.view.*

class ProfilesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profiles, container, false)
    }

    override fun onViewCreated(fragment: View, savedInstanceState: Bundle?) {
        fragment.view_pager.adapter = ViewPagerAdapter(childFragmentManager)
    }
}

class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int = 3

    override fun getItem(i: Int): Fragment {
        return when (i) {
            0 -> ProfilesFragmentContent("battery")
            1 -> ProfilesFragmentContent("balanced")
            2 -> ProfilesFragmentContent("performance")
            else -> ProfilesFragmentContent("battery")
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Battery"
            1 -> "Balanced"
            2 -> "Performance"
            else -> ""
        }
    }
}
