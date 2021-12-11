package com.vocaby.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.vocaby.app.Constants.VOCABY_BASE_URL
import com.vocaby.app.R
import com.vocaby.app.ui.WebActivity

class ProfileHomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_main, container, false)
        setupButtons(view)
        return view
    }

    private fun setupButtons(view: View) {
        // NOTIFICATION
        val notificationButton = view.findViewById<Button>(R.id.notification_button)
        notificationButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_right_to_left,
                    R.anim.exit_right_to_left,
                    R.anim.enter_right_to_left,
                    R.anim.exit_left_to_right
                ).add(R.id.profile_fragment_container, NotificationFragment())
                .addToBackStack(null)
                .commit()
        }

        // DATA MANAGEMENT
        val dataButton = view.findViewById<Button>(R.id.data_management_button)
        dataButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_right_to_left,
                    R.anim.exit_right_to_left,
                    R.anim.enter_right_to_left,
                    R.anim.exit_left_to_right
                ).add(R.id.profile_fragment_container, DataManagementFragment())
                .addToBackStack(null)
                .commit()
        }

        val supportButton = view.findViewById<Button>(R.id.support_button)
        supportButton.setOnClickListener {
            val intent = Intent(requireActivity().applicationContext, WebActivity::class.java)
            intent.putExtra("URL", VOCABY_BASE_URL + "support")
            startActivity(intent)
        }

        val feedbackButton = view.findViewById<Button>(R.id.feedback_button)
        feedbackButton.setOnClickListener {
            val intent = Intent(requireActivity().applicationContext, WebActivity::class.java)
            intent.putExtra("URL", VOCABY_BASE_URL + "feedback")
            startActivity(intent)
        }

        // DANGER ZONE
        val dangerButton = view.findViewById<Button>(R.id.danger_zone_button)
        dangerButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_right_to_left,
                    R.anim.exit_right_to_left,
                    R.anim.enter_right_to_left,
                    R.anim.exit_left_to_right
                ).add(R.id.profile_fragment_container, DangerZoneFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}