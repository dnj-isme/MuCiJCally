package edu.bluejack22_2.MuCiJCally

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import edu.bluejack22_2.MuCiJCally.model.AccountSession
import edu.bluejack22_2.MuCiJCally.utility.PageNavigator

class BottomNavbar: Fragment() {

    val debug = "FLAG"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bottom_navbar, container, false)
        handleEvent(view)

        // Return the inflated view
        return view
    }

    private fun handleEvent(view: View) {
        val homeImg: ImageView = view.findViewById(R.id.home_img)
        val homeTxt: TextView = view.findViewById(R.id.home_txt)

        homeImg.setOnClickListener {navigateHome()}
        homeTxt.setOnClickListener {navigateHome()}

        val exploreImg: ImageView = view.findViewById(R.id.explore_img)
        val exploreTxt: TextView = view.findViewById(R.id.explore_txt)

        exploreImg.setOnClickListener {navigateExplore()}
        exploreTxt.setOnClickListener {navigateExplore()}

        val profileImg: ImageView = view.findViewById(R.id.profile_img)
        val profileTxt: TextView = view.findViewById(R.id.profile_txt)

        profileImg.setOnClickListener {navigateProfile()}
        profileTxt.setOnClickListener {navigateProfile()}

        val session = requireActivity().intent.getSerializableExtra("Session") as AccountSession
        if(session != null) {
            Log.d("SESSION DATA", session.sessionID)
        }
    }

    private fun notMatch(targetClassName: String): Boolean {
        return !requireActivity().javaClass.simpleName.equals(targetClassName)
    }

    private fun navigateHome() {
        if(notMatch("HomeActivity")) {
            PageNavigator.switchPage(requireActivity() as AppCompatActivity, HomeActivity::class.java)
        }
    }

    private fun navigateExplore() {
        if(notMatch("ExploreActivity")) {
            PageNavigator.switchPage(requireActivity() as AppCompatActivity, ExploreActivity::class.java)
        }
    }

    private fun navigateProfile() {
        if(notMatch("ProfileActivity")) {
            PageNavigator.switchPage(requireActivity() as AppCompatActivity, ProfileActivity::class.java)
        }
    }

}