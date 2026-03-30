package com.simats.databaseoddiseasestatus

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiClient {
    /**
     * IP ADDRESS GUIDE:
     * - Use "http://10.0.2.2:8000/api/" for Android Emulator.
     * - Use your machine's local IP (e.g., "http://192.168.x.x:8000/api/") for Real Devices.
     * - Ensure your Django server is running with: python manage.py runserver 0.0.0.0:8000
     */
    
    // Replace 10.52.221.72 with your current computer's IP (found via 'ipconfig')
    // and 8135 with your server's port (e.g., 8000)
    const val BASE_URL = "http://10.52.221.72:8135/api/"

    val instance: ApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd")
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()

        retrofit.create(ApiService::class.java)
    }
}
