package com.shuttl.location_pings.config.components

import com.shuttl.location_pings.data.api.LocationApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal object LocationRetrofit {

    private var baseUrl: String? = ""

    private var retrofit: Retrofit? = null

    private var networkDebug: Interceptor? = null

    private const val TIMEOUT_RESPONSE: Long = 30
    private const val TIMEOUT_CONNECTION: Long = 10

    private fun getOkHttpClient(interceptor: Interceptor? = networkDebug): OkHttpClient {
        networkDebug = interceptor
        val b = OkHttpClient.Builder()
            b.readTimeout(TIMEOUT_RESPONSE, TimeUnit.SECONDS)
            b.connectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS)
        if (interceptor != null)
            b.addInterceptor(interceptor)
        return b.build()
    }

    fun resetRetrofit(baseUrl: String?, httpClient: Interceptor?) {
        if (baseUrl.isNullOrEmpty()) return
        retrofit = Retrofit.Builder()
            .client(getOkHttpClient(httpClient))
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }

    fun getRetrofitObj(): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .client(getOkHttpClient(networkDebug))
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()
        }
        return retrofit
    }

    fun getRetrofitObj(baseUrl: String?): Retrofit? {
        if (baseUrl.isNullOrEmpty()) return retrofit
        retrofit = Retrofit.Builder()
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
        return retrofit
    }

    fun getLocationApi(baseUrl: String? = null): LocationApi? {
        return getRetrofitObj(baseUrl)?.create(LocationApi::class.java)
    }

}