package com.vin.booking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class ViewModelFactory @Inject constructor(
    creators: Map<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {

    private var vms: Map<Class<out ViewModel>, Provider<ViewModel>> = creators

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        var creator: Provider<ViewModel>? = vms[modelClass]
        if (creator == null) {
            for ((key, value) in vms.entries) {
                if (modelClass.isAssignableFrom(key)) {
                    creator = value
                    break
                }
            }
        }
        requireNotNull(creator) { "Cannot create an instance of $modelClass" }
        return try {
            creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        }
    }
}