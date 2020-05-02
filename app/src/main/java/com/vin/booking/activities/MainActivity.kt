package com.vin.booking.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.vin.booking.R
import com.vin.booking.fragments.MainFragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.main_layout.toolbar
import kotlinx.android.synthetic.main.main_layout.toolbarTitle
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private lateinit var toolbarDisplay: Toolbar
    private lateinit var toolbarDisplayTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        toolbarDisplay = toolbar
        toolbarDisplayTitle = toolbarTitle

        setContentFragment(MainFragment.newInstance())
    }

    /**
     * Convenient method to set Toolbar title
     */
    fun setTitle(title: String) {
        toolbarDisplayTitle.text = title
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    /**
     * Method to set a fragment to an activity without adding to backstack
     */
    private fun setContentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.content, fragment).commit()
    }
}
