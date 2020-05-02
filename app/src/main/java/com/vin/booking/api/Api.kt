package com.vin.booking.api

import retrofit2.Retrofit
import javax.inject.Inject

class Api @Inject constructor(retrofit: Retrofit) {

    private val apiService = retrofit.create(ApiService::class.java)

}