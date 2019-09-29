package com.zeroinfinity.powerengine.fragments


import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection
import com.google.android.material.snackbar.Snackbar
import com.zeroinfinity.powerengine.*
import com.zeroinfinity.powerengine.Notification.cancelNotification
import com.zeroinfinity.powerengine.Notification.createNotification
import com.zeroinfinity.powerengine.Settings.chargingMode
import com.zeroinfinity.powerengine.Settings.darkTheme
import com.zeroinfinity.powerengine.Settings.getAllProfiles
import com.zeroinfinity.powerengine.Settings.notifications
import com.zeroinfinity.powerengine.Settings.showSystemApps
import com.zeroinfinity.powerengine.helpers.AppHelper
import com.zeroinfinity.powerengine.helpers.CPUHelper
import com.zeroinfinity.powerengine.helpers.CPUHelper.clusters
import com.zeroinfinity.powerengine.helpers.CPUHelper.governorList
import com.zeroinfinity.powerengine.helpers.GPUHelper
import com.zeroinfinity.powerengine.helpers.GPUHelper.GPUPath
import com.zeroinfinity.powerengine.helpers.LoggingHelper
import com.zeroinfinity.powerengine.objects.App
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_more.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.SAXParseException
import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory


class MoreFragment : Fragment() {
    private lateinit var fragment: View

    companion object {
        fun writePreference(preferenceTag: String, value: Boolean, context: Context) {
            context.getSharedPreferences(
                context.getString(R.string.defaultSharedPrefs),
                Context.MODE_PRIVATE
            ).edit {
                putBoolean(preferenceTag, value)
            }
        }

        fun readBooleanPreference(preferenceTag: String, context: Context): Boolean =
            context.getSharedPreferences(
                context.getString(R.string.defaultSharedPrefs),
                Context.MODE_PRIVATE
            )
                .getBoolean(preferenceTag, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragment = inflater.inflate(R.layout.fragment_more, container, false)
        setup()
        return fragment
    }

    private fun setup() {
        val expansionLayoutCollection = ExpansionLayoutCollection()
        expansionLayoutCollection.add(fragment.aboutExpansionLayout)
        expansionLayoutCollection.add(fragment.helpExpansionLayout)
        expansionLayoutCollection.add(fragment.backupExpansionLayout)
        expansionLayoutCollection.add(fragment.bugReportExpansionLayout)
        expansionLayoutCollection.add(fragment.preferencesExpansionLayout)
        expansionLayoutCollection.openOnlyOne(true)

        fragment.systemAppSwitch.isChecked = showSystemApps
        fragment.notificationsSwitch.isChecked = notifications
        fragment.darkThemeSwitch.isChecked = darkTheme
        fragment.chargingModeSwitch.isChecked = chargingMode

        fragment.appEngineBackupButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/xml"
                putExtra(Intent.EXTRA_TITLE, "app_engine_backup")
            }

            startActivityForResult(intent, APP_BACKUP)
        }

        fragment.profilesBackupButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/xml"
                putExtra(Intent.EXTRA_TITLE, "profile_backup")
            }

            startActivityForResult(intent, PROFILE_BACKUP)
        }

        fragment.appEngineRestoreButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/xml"
            }

            startActivityForResult(intent, APP_RESTORE)
        }

        fragment.profilesRestoreButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/xml"
            }

            startActivityForResult(intent, PROFILE_RESTORE)
        }

        fragment.darkThemeSwitch.setOnCheckedChangeListener { _, b ->
            darkTheme = b
            writePreference(getString(R.string.dark_theme), darkTheme, requireContext())
            Snackbar.make(
                activity!!.fragment_container,
                getString(R.string.dark_theme_notice),
                Snackbar.LENGTH_SHORT
            ).show()
        }

        fragment.systemAppSwitch.setOnCheckedChangeListener { _, b ->
            showSystemApps = b
            writePreference(getString(R.string.system_app), showSystemApps, requireContext())
        }

        fragment.notificationsSwitch.setOnCheckedChangeListener { _, b ->
            notifications = b
            writePreference(getString(R.string.notifications), notifications, requireContext())

            if (!notifications)
                cancelNotification(requireContext())
            else
                createNotification(requireContext())
        }

        fragment.chargingModeSwitch.setOnCheckedChangeListener { _, b ->
            chargingMode = b
            writePreference(getString(R.string.charging_mode), chargingMode, requireContext())
        }

        fragment.wipeDataButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.wipeDataAlertTitle))
                .setMessage(getString(R.string.wipeDataAlertDesc))
                .setPositiveButton(getString(R.string.positiveButton)) { _, _ ->
                    val activityManager =
                        activity?.getSystemService(ACTIVITY_SERVICE) as ActivityManager
                    activityManager.clearApplicationUserData()
                }
                .setNegativeButton(getString(R.string.negativeButton)) { dialogInterface, _ ->
                    dialogInterface.cancel()
                }
                .show()
        }

        fragment.darkThemePreferences.setOnClickListener { fragment.darkThemeSwitch.toggle() }
        fragment.systemAppPreference.setOnClickListener { fragment.systemAppSwitch.toggle() }
        fragment.notificationsPreference.setOnClickListener { fragment.notificationsSwitch.toggle() }
        fragment.chargingModePreference.setOnClickListener { fragment.chargingModeSwitch.toggle() }

        fragment.bugReportButton.setOnClickListener {
            LoggingHelper.produceLogcat(requireContext())

            Toast.makeText(
                requireContext(),
                "TODO: Implement bug report function",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun writeFile(outputStream: OutputStream, xmlString: String) {
        try {
            val outputStreamWriter = OutputStreamWriter(outputStream)
            outputStreamWriter.write(xmlString)
            outputStreamWriter.flush()
            outputStreamWriter.close()
            Snackbar.make(
                activity!!.fragment_container,
                getString(R.string.backup_success),
                Snackbar.LENGTH_SHORT
            )
                .show()
        } catch (e: IOException) {
            Snackbar.make(
                activity!!.fragment_container,
                getString(R.string.storage_error),
                Snackbar.LENGTH_SHORT
            )
                .show()
        }
    }

    //  XML generation by code
    private fun XmlSerializer.document(
        docName: String = "UTF-8",
        xmlStringWriter: StringWriter = StringWriter(),
        init: XmlSerializer.() -> Unit
    ): String {
        startDocument(docName, true)
        xmlStringWriter.buffer.setLength(0) //  refreshing string writer due to reuse
        setOutput(xmlStringWriter)
        init()
        endDocument()
        return xmlStringWriter.toString()
    }

    //  element
    private fun XmlSerializer.element(name: String, init: XmlSerializer.() -> Unit) {
        startTag("", name)
        init()
        endTag("", name)
    }

    //  attribute
    private fun XmlSerializer.attribute(name: String, value: String) =
        attribute("", name, value)

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == APP_BACKUP || requestCode == PROFILE_BACKUP ||
                requestCode == APP_RESTORE || requestCode == PROFILE_RESTORE
            ) {
                resultData?.data?.also { uri ->
                    when (requestCode) {
                        APP_BACKUP -> {
                            appBackup(requireContext().contentResolver.openOutputStream(uri)!!)
                        }

                        PROFILE_BACKUP -> {
                            profileBackup(requireContext().contentResolver.openOutputStream(uri)!!)
                        }

                        APP_RESTORE -> {
                            appRestore(requireContext().contentResolver.openInputStream(uri)!!)
                        }

                        PROFILE_RESTORE -> {
                            profileRestore(requireContext().contentResolver.openInputStream(uri)!!)
                        }
                    }
                }
            }
        }
    }

    private fun appBackup(output: OutputStream) {
        GlobalScope.launch {
            val appEngineList: ArrayList<App> = AppHelper(requireContext()).getAllApps("")
            val xmlSerializer = Xml.newSerializer()
                .apply { setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true) }
            val xmlString = xmlSerializer.document {
                element("appengine") {
                    for (app in appEngineList) {
                        element("app") {
                            attribute("packageName", app.packageName)
                            attribute(
                                "profile",
                                App.getProfile(app.packageName, requireContext())!!
                            )
                        }
                    }
                }
            }

            writeFile(output, xmlString)
        }
    }

    private fun profileBackup(output: OutputStream) {
        GlobalScope.launch {
            val xmlSerializer = Xml.newSerializer()
                .apply { setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true) }
            val xmlString = xmlSerializer.document {
                element("profiles") {
                    for (profile in getAllProfiles(requireContext()).entries) {
                        element("profile") {
                            attribute("name", profile.key)

                            val config: MutableMap<Int, MutableList<String?>> =
                                CPUHelper.getCurrentProfileFreqs(profile.value)

                            for ((index, cluster) in clusters.withIndex()) {
                                element(cluster) {
                                    attribute("max", config[index]!![1]!!)
                                    attribute("min", config[index]!![0]!!)
                                }
                            }

                            if (governorList[0] != "") {
                                element("governor") {
                                    attribute(
                                        "name",
                                        CPUHelper.getCurrentProfileGovernor(profile.value)
                                    )
                                }
                            }

                            if (GPUPath != "") {
                                val gpuConfig: List<String> =
                                    GPUHelper.getCurrentProfileFreqs(profile.value)

                                element("gpu") {
                                    attribute("max", gpuConfig[1])
                                    attribute("min", gpuConfig[0])
                                }
                            }
                        }
                    }
                }
            }

            writeFile(output, xmlString)
        }
    }

    private fun appRestore(input: InputStream) {
        GlobalScope.launch {
            try {
                val doc: Document =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)
                val apps: NodeList = doc.getElementsByTagName("app")

                if (apps.length > 0) {
                    for (i in 0 until apps.length) {
                        App.setProfile(
                            apps.item(i).attributes.getNamedItem("packageName").nodeValue,
                            apps.item(i).attributes.getNamedItem("profile").nodeValue,
                            requireContext()
                        )
                    }

                    activity?.runOnUiThread {
                        Snackbar.make(
                            activity!!.fragment_container,
                            getString(R.string.restore_success),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    activity?.runOnUiThread {
                        Snackbar.make(
                            activity!!.fragment_container,
                            getString(R.string.read_failed_appengine),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: SAXParseException) {
                activity?.runOnUiThread {
                    Snackbar.make(
                        activity!!.fragment_container,
                        getString(R.string.read_failed_structure_error),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun profileRestore(input: InputStream) {
        GlobalScope.launch {
            try {
                val doc: Document =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)
                val profiles: NodeList = doc.getElementsByTagName("profile")

                if (profiles.length > 0) {
                    for (i in 0 until profiles.length) {
                        val profile = profiles.item(i).attributes.getNamedItem("name").nodeValue
                        val sharedPreferences: SharedPreferences =
                            Settings.getProfilePrefs(profile, requireContext())
                        val profileConfig: NodeList = profiles.item(i).childNodes

                        if (profileConfig.length > 0) {
                            for (j in 0 until profileConfig.length) {
                                val nodeName = profileConfig.item(j).nodeName
                                val attributes = profileConfig.item(j).attributes

                                if (attributes != null) {
                                    when (nodeName) {
                                        "governor" -> {
                                            CPUHelper.setGovernor(
                                                sharedPreferences,
                                                attributes.getNamedItem("name").nodeValue
                                            )
                                        }

                                        "gpu" -> {
                                            if (GPUPath != "") {
                                                GPUHelper.setMaxFreq(
                                                    sharedPreferences,
                                                    attributes.getNamedItem("max").nodeValue
                                                )
                                                GPUHelper.setMinFreq(
                                                    sharedPreferences,
                                                    attributes.getNamedItem("min").nodeValue
                                                )
                                            }
                                        }

                                        else -> {
                                            CPUHelper.setMaxFreq(
                                                sharedPreferences,
                                                attributes.getNamedItem("max").nodeValue,
                                                nodeName
                                            )

                                            CPUHelper.setMinFreq(
                                                sharedPreferences,
                                                attributes.getNamedItem("min").nodeValue,
                                                nodeName
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    activity?.runOnUiThread {
                        Snackbar.make(
                            activity!!.fragment_container,
                            getString(R.string.restore_success),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    activity?.runOnUiThread {
                        Snackbar.make(
                            activity!!.fragment_container,
                            getString(R.string.read_failed_profile),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: SAXParseException) {
                activity?.runOnUiThread {
                    Snackbar.make(
                        activity!!.fragment_container,
                        getString(R.string.read_failed_structure_error),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
