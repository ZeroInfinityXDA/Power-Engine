package com.zeroinfinity.powerengine.fragments

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zeroinfinity.powerengine.R
import com.zeroinfinity.powerengine.Settings.getProfilePrefs
import com.zeroinfinity.powerengine.helpers.CPUHelper
import com.zeroinfinity.powerengine.helpers.CPUHelper.clusters
import com.zeroinfinity.powerengine.helpers.CPUHelper.formatFreq
import com.zeroinfinity.powerengine.helpers.CPUHelper.freqTables
import com.zeroinfinity.powerengine.helpers.CPUHelper.governorList
import com.zeroinfinity.powerengine.helpers.GPUHelper
import com.zeroinfinity.powerengine.helpers.GPUHelper.GPUFreqTable
import com.zeroinfinity.powerengine.helpers.GPUHelper.GPUPath
import kotlinx.android.synthetic.main.fragment_profiles_fragment_content.*
import kotlinx.android.synthetic.main.fragment_profiles_fragment_content.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ProfilesFragmentContent(private val profile: String) : Fragment() {
    private lateinit var profilePreferences: SharedPreferences
    private var currentCluster: Int = 0
    private lateinit var fragment: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragment = inflater.inflate(R.layout.fragment_profiles_fragment_content, container, false)

        GlobalScope.launch {
            profilePreferences = getProfilePrefs(profile, requireContext())
            if (freqTables.isNotEmpty()) {
                reloadUI()
                setup()
            } else {
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.profileError),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        return fragment
    }

    private fun setup() {
        if (clusters.size > 1) {
            activity?.runOnUiThread {
                fragment.headerIndicator.visibility = View.VISIBLE
                fragment.cluster.isToggleOnClick = true
            }

            val clusterGroup = RadioGroup(requireContext())

            for ((index, _) in clusters.withIndex()) {
                val clusterRadio = RadioButton(requireContext())
                clusterRadio.text = getString(R.string.cluster) + " " + index
                clusterRadio.setOnClickListener {
                    currentCluster = index
                    fragment.expansionLayout.collapse(true)
                    profilePreferences = getProfilePrefs(profile, requireContext())
                    reloadUI()
                }

                clusterGroup.addView(clusterRadio)

                if (index == currentCluster)
                    clusterRadio.isChecked = true
            }

            activity?.runOnUiThread {
                fragment.clusterOptions.addView(clusterGroup)
            }
        }

        val currentGov = CPUHelper.getCurrentProfileGovernor(profilePreferences)

        activity?.runOnUiThread {
            fragment.currentGovernor.text = currentGov
            fragment.governorCard.setOnClickListener {
                AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.governor))
                    .setItems(governorList.toTypedArray()) { _, which ->
                        CPUHelper.setGovernor(profilePreferences, governorList[which])
                        fragment.currentGovernor.text = governorList[which]
                    }.show()
            }
        }

        if (GPUPath != "" && GPUFreqTable.isNotEmpty()) {
            val gpuCurrFreqs = GPUHelper.getCurrentProfileFreqs(profilePreferences)

            activity?.runOnUiThread {
                fragment.gpuCurrMax.text = formatFreq(gpuCurrFreqs[1])
                fragment.gpuCurrMin.text = formatFreq(gpuCurrFreqs[0])

                fragment.gpuMaxCard.setOnClickListener {
                    AlertDialog.Builder(activity)
                        .setTitle(getString(R.string.maxfreq_dialog))
                        .setItems(GPUFreqTable.toTypedArray()) { _, which ->
                            GPUHelper.setMaxFreq(profilePreferences, GPUFreqTable[which])
                            fragment.gpuCurrMax.text = formatFreq(GPUFreqTable[which])
                        }.show()
                }

                fragment.gpuMinCard.setOnClickListener {
                    AlertDialog.Builder(activity)
                        .setTitle(getString(R.string.minfreq_dialog))
                        .setItems(GPUFreqTable.toTypedArray()) { _, which ->
                            GPUHelper.setMinFreq(profilePreferences, GPUFreqTable[which])
                            fragment.gpuCurrMin.text = formatFreq(GPUFreqTable[which])
                        }.show()
                }
            }
        } else {
            activity?.runOnUiThread {
                gpuFreqsTitle.visibility = View.GONE
                gpuFreq.visibility = View.GONE
            }
        }
    }

    private fun reloadUI() {
        val currentFreqs = CPUHelper.getCurrentProfileFreqs(profilePreferences)

        activity?.runOnUiThread {
            fragment.currentCluster.text = getString(R.string.cluster) + " " + currentCluster
            fragment.currentMaxFreq.text = formatFreq(currentFreqs[currentCluster]!![1]!!)
            fragment.currentMinFreq.text = formatFreq(currentFreqs[currentCluster]!![0]!!)
            fragment.maxCard.setOnClickListener {
                AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.maxfreq_dialog))
                    .setItems(freqTables.getValue(currentCluster).toTypedArray()) { _, which ->
                        CPUHelper.setMaxFreq(
                            profilePreferences,
                            freqTables.getValue(currentCluster)[which],
                            clusters[currentCluster]
                        )
                        fragment.currentMaxFreq.text =
                            formatFreq(freqTables.getValue(currentCluster)[which])
                    }.show()
            }

            fragment.minCard.setOnClickListener {
                AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.minfreq_dialog))
                    .setItems(freqTables.getValue(currentCluster).toTypedArray()) { _, which ->
                        CPUHelper.setMinFreq(
                            profilePreferences,
                            freqTables.getValue(currentCluster)[which],
                            clusters[currentCluster]
                        )
                        fragment.currentMinFreq.text =
                            formatFreq(freqTables.getValue(currentCluster)[which])
                    }.show()
            }
        }
    }
}
