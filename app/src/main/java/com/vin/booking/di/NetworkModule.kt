package com.vin.booking.di

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vin.booking.api.Api
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    internal fun provideGson(): Gson =
        GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()

    @Provides
    @Singleton
    internal fun provideOkHttpClientBuilder(): OkHttpClient =
        OkHttpClient.Builder().apply {
            connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        }.build()

    @Provides
    @Singleton
    internal fun provideRetrofitBuilder(gson: Gson): Retrofit.Builder =
        Retrofit.Builder()
            .baseUrl(BASE_DOMAIN)
            .addConverterFactory(GsonConverterFactory.create(gson))

    @Provides
    @Singleton
    internal fun provideApi(
        okHttpClient: OkHttpClient,
        retrofitBuilder: Retrofit.Builder
    ): Api = Api(retrofitBuilder.client(okHttpClient).build())

    companion object {
        private const val CONNECTION_TIMEOUT = 30L

        internal const val BASE_DOMAIN = "https://gist.githubusercontent.com/"
    }
}