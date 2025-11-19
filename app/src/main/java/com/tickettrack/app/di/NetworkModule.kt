package com.tickettrack.app.di

import com.tickettrack.app.data.remote.api.ReportsApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    // OkHttp Client
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit Instance para Reports API
    single {
        Retrofit.Builder()
            .baseUrl("https://tickettrack-dashboard-production.up.railway.app/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Reports API Service
    single {
        get<Retrofit>().create(ReportsApi::class.java)
    }
}