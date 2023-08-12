package edu.bluejack22_2.MuCiJCally.utility

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import edu.bluejack22_2.MuCiJCally.BottomNavbar
import edu.bluejack22_2.MuCiJCally.LastPlayedComponent
import edu.bluejack22_2.MuCiJCally.PlayPageActivity
import edu.bluejack22_2.MuCiJCally.TopLogo

class LayoutAssembler {
    private var activity: AppCompatActivity;
    private var layoutID: Int;

    constructor(activity: AppCompatActivity, layoutID: Int) {
        this.activity = activity;
        this.layoutID = layoutID;
    }

    fun initFooter() {
        val fragmentContainerView = activity.findViewById<FragmentContainerView>(layoutID);
        val topLogoFragment = BottomNavbar()
        activity.supportFragmentManager
            .beginTransaction()
            .add(fragmentContainerView.id, topLogoFragment)
            .commit()
    }

    fun initHeader() {
        val fragmentContainerView = activity.findViewById<FragmentContainerView>(layoutID)
        val topLogoFragment = TopLogo()
        activity.supportFragmentManager
            .beginTransaction()
            .add(fragmentContainerView.id, topLogoFragment)
            .commit()
    }

    fun initLastPlayedComponent() {
        val containerView = activity.findViewById<FragmentContainerView>(layoutID)
        val lastPlayedComponent = LastPlayedComponent(activity, layoutID)
        activity.supportFragmentManager
            .beginTransaction()
            .add(containerView.id, lastPlayedComponent)
            .commit()
    }

}