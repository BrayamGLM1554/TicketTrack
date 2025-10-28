package com.tickettrack.app.data.remote

import com.tickettrack.app.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit específico para el módulo de Trips.
 * Separado del RetrofitClient principal para no afectar otros módulos.
 */
object TripRetrofitClient {
    private const val BASE_URL = "https://tickettrakedauth.runasp.net/"

    private lateinit var tokenManager: TokenManager

    /**
     * Inicializar con TokenManager.
     * Se llama desde TicketTrackApplication.
     */
    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    /**
     * Interceptor para agregar el token de autenticación en cada request.
     */
    private val authInterceptor = Interceptor { chain ->
        val token = tokenManager.getToken()
        val requestBuilder = chain.request().newBuilder()

        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        chain.proceed(requestBuilder.build())
    }

    /**
     * Interceptor para logging (debug).
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Cliente HTTP configurado.
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Instancia de Retrofit.
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Servicio API para viajes.
     */
    val tripApiService: TripApiService = retrofit.create(TripApiService::class.java)
}