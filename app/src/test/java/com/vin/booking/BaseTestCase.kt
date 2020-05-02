package com.vin.booking

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.vin.booking.api.Api
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.nio.charset.StandardCharsets

open class BaseTestCase {

    protected var server: MockWebServer? = null

    @After
    @Throws(Exception::class)
    fun tearDown() {
        if (server != null) {
            server!!.shutdown()
            server = null
        }
    }

    protected fun getMockApi(): Api {
        return Api(getApiHelper().retrofit)
    }

    private fun getApiHelper(): ApiHelper {
        if (server == null) {
            startMockServer()
        }

        val mockServerUrl = server!!.url("/")

        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                // Redirect all requests to internal mock server
                var original = chain.request()
                val newUrl = original.url
                    .newBuilder()
                    .scheme(mockServerUrl.scheme)
                    .host(mockServerUrl.host)
                    .port(mockServerUrl.port)
                    .build()
                original = original.newBuilder().url(newUrl).build()
                return chain.proceed(original)
            }
        })

        val gsonBuilder = GsonBuilder()
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)

        val retrofit = Retrofit.Builder()
            .client(okHttpBuilder.build())
            .baseUrl(mockServerUrl)
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
            .build()

        return ApiHelper(retrofit)
    }

    /**
     * Convenient Method to get a static file to simulate an API response in JSON
     */
    protected fun mockResponseFromFile(name: String): MockResponse {
        return MockResponse().setBody(getResourceAsString("api-response/$name"))
    }

    /**
     * Convenient Method to simulate an API response
     */
    protected fun startMockServer(vararg responses: MockResponse) {
        server = MockWebServer()
        for (response in responses) {
            server!!.enqueue(response)
        }
        try {
            server!!.start()
        } catch (e: Exception) {
            println("Unable to start mock server: " + e.message)
        }
    }

    private fun getResourceAsString(name: String): String {
        val inputStream = javaClass.classLoader?.getResourceAsStream(name)
        return try {
            val source = inputStream?.source()?.buffer()
            source?.readString(StandardCharsets.UTF_8).orEmpty()
        } catch (e: IOException) {
            throw RuntimeException("Error reading $name\n$e")
        }
    }
}

data class ApiHelper(val retrofit: Retrofit)