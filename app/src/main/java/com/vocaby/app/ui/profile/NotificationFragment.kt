package com.vocaby.app.ui.profile

import androidx.preference.PreferenceFragmentCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.vocaby.app.R

class NotificationFragment : PreferenceFragmentCompat() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val backButton = view?.findViewById<Button>(R.id.back_button)
        backButton?.setOnClickListener { requireActivity().onBackPressed() }

        return view
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notification_preferences, rootKey)
    }
}