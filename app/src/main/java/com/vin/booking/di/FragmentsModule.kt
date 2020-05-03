package com.vin.booking.di

import com.vin.booking.fragments.MainFragment
import com.vin.booking.fragments.ScanQRFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentsModule {

    @ContributesAndroidInjector
    internal abstract fun bindMainFragment(): MainFragment

    @ContributesAndroidInjector
    internal abstract fun bindScanQRFragment(): ScanQRFragment
}