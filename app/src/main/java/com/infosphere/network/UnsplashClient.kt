package com.infosphere.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UnsplashClient {
    private const val BASE_URL = "https://api.unsplash.com/"

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        // Récupérer la clé via réflexion pour éviter une dépendance directe au BuildConfig généré
        val key: String? = try {
            val bcClass = Class.forName("com.infosphere.BuildConfig")
            val field = bcClass.getDeclaredField("UNSPLASH_ACCESS_KEY")
            field.isAccessible = true
            field.get(null) as? String
        } catch (t: Throwable) {
            null
        }
        if (!key.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Client-ID $key")
        }
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: UnsplashApi by lazy {
        retrofit.create(UnsplashApi::class.java)
    }
}
