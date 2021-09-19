

package com.example.android.codelabs.paging.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface TwitchService {

    @Headers(
        "client-id: radgijo8h8k2j3v9fjj4iivqkef0y7",
        "authorization: Bearer rfn6889a2ljhqcc5s20s8qkhmjp1fh"
    )
    @GET("helix/games/top")
    suspend fun searchRepos(
       @Query("after") after: String = "",
    ): TwitchSearchResponse

    companion object {
        private const val BASE_URL = "https://api.twitch.tv"

        fun create(): TwitchService {
            val logger = HttpLoggingInterceptor()
            logger.level = Level.BASIC

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TwitchService::class.java)
        }
    }
}
