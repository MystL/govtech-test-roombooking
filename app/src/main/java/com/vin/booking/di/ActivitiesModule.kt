package com.vin.booking.di

import com.vin.booking.activities.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesModule {

    @ContributesAndroidInjector(modules = [ViewModelsModule::class])
    internal abstract fun bindMainActivity(): MainActivity

}