package com.zeroinfinity.powerengine.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.topjohnwu.superuser.Shell
import com.zeroinfinity.powerengine.R
import com.zeroinfinity.powerengine.version
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*


class MainFragment : Fragment() {
    private lateinit var fragment: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragment = inflater.inflate(R.layout.fragment_main, container, false)
        setup(fragment)
        return fragment
    }

    private fun setup(fragment: View) {

        if (Shell.rootAccess()) {
            fragment.rootStatus.setImageDrawable(activity?.getDrawable(R.drawable.outline_check_circle_24))
        }

        fragment.versionNumber.text = version

        fragment.serviceCard.setOnClickListener {
            if (Shell.rootAccess())
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            else
                Snackbar.make(
                    activity!!.fragment_container,
                    requireContext().getString(R.string.no_root_detected),
                    Snackbar.LENGTH_SHORT
                ).show()
        }
    }
}
