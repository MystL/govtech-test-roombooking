package com.vin.booking

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.vin.booking.di.AppComponent
import com.vin.booking.di.DaggerAppComponent
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class App : Application(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        // Instantiate Dagger Injection
        appComponent = DaggerAppComponent.builder().application(this).build()
        appComponent.inject(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                inject(activity)
            }

            override fun onActivityPaused(p0: Activity) {} // ignore for now

            override fun onActivityStarted(p0: Activity) {} // ignore for now

            override fun onActivityDestroyed(p0: Activity) {} // ignore for now

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {} // ignore for now

            override fun onActivityStopped(p0: Activity) {} // ignore for now

            override fun onActivityResumed(p0: Activity) {} // ignore for now
        })
    }

    private fun inject(activity: Activity) {
        // Injection for Activity
        if (activity is HasAndroidInjector) {
            AndroidInjection.inject(activity)
        }

        // Injection for Fragment
        if (activity is FragmentActivity) {
            activity
                .supportFragmentManager
                .registerFragmentLifecycleCallbacks(object :
                    FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentCreated(
                        fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?
                    ) {
                        AndroidSupportInjection.inject(f)
                    }
                }, true)
        }
    }

    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }
}