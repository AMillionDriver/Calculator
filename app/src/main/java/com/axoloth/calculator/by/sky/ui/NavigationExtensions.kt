package com.axoloth.calculator.by.sky.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.axoloth.calculator.by.sky.R

fun navigateToFragment(activity: AppCompatActivity, fragment: Fragment) {
    activity.supportFragmentManager.beginTransaction()
        .setCustomAnimations(
            R.anim.slide_in_right,
            R.anim.slide_out_left,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        .replace(R.id.fragment_container, fragment)
        .addToBackStack(null)
        .commit()
}
