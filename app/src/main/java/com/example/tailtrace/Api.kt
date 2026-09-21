package com.example.tailtrace

import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.http.*
import java.io.IOException
import java.util.concurrent.TimeUnit

interface TailTraceApi {
    @POST("api/auth/register") suspend fun register(@Body body: RegisterRequest): AuthResponse
    @POST("api/auth/login") suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/pets/nearby")
    suspend fun nearby(@Query("lat") lat: Double, @Query("lng") lng: Double, @Query("radiusKm") radiusKm: Int): List<Pet>
    @GET("api/pets/{id}") suspend fun pet(@Path("id") id: String): Pet
    @POST("api/pets") suspend fun createPet(@Body body: NewPet): Pet
    @PATCH("api/pets/{id}/status") suspend fun setStatus(@Path("id") id: String, @Body body: StatusRequest): Pet

    @GET("api/pets/{id}/sightings") suspend fun sightings(@Path("id") id: String): List<Sighting>
    @POST("api/sightings") suspend fun addSighting(@Body body: NewSighting): Sighting
}

object ApiClient {
    const val BASE_URL = "https://tailtrace-production.up.railway.app/"

    @Volatile var token: String? = null

    private val http = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
            token?.let { builder.header("Authorization", "Bearer " + it) }
            chain.proceed(builder.build())
        }
        .build()

    val api: TailTraceApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TailTraceApi::class.java)
}

fun Throwable.friendly(): String = when (this) {
    is HttpException -> runCatching {
        JSONObject(response()?.errorBody()?.string() ?: "").getString("error")
    }.getOrDefault("Server error (" + code() + ")")
    is IOException -> "No connection or the server is waking up. Check your internet and try again."
    else -> message ?: "Something went wrong"
}