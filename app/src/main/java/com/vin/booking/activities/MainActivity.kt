package com.vin.booking.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.vin.booking.R
import com.vin.booking.viewmodels.RoomBookingViewModel
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModel: RoomBookingViewModel

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.rooms.observe(this, Observer {
            // Handle UI Update here
        })
    }

    override fun onResume() {
        super.onResume()
        // Ensure that the Data is re-fetched when this screen is returned
        viewModel.fetchRooms()
    }
}
