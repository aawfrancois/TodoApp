package com.example.android.todoapp.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface TodoApiService {
    @GET("tasks")
//    @Headers("Content-Type: application/json")
    fun getTasks(): Deferred<List<Task>>

    @POST("tasks")
    @Headers("Content-Type: application/json")
    fun createTasks(@Body task: Task): Deferred<Task>

    @DELETE("tasks/{id}")
    fun deleteTask(@Path("id") id: String): Deferred<Response<Unit>>

    @GET("tasks/{id}/close")
//    @Headers("Content-Type: application/json")
    fun checkedTasks(@Path("id") id: String): Deferred<Response<Unit>>

    @GET("tasks/{id}/reopen")
//    @Headers("Content-Type: application/json")
    fun unCheckedTasks(@Path("id") id: String): Deferred<Response<Unit>>
}


object TodoApiFactory {

    private const val BASE_URL = "https://beta.todoist.com/API/v8/"
    private const val TOKEN = "d34780ea18332ecaca98a2638eec54eefa776915"

    // Create OkHttpClient object
    // Each request made with my Retrofit instance, the token will be add
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $TOKEN")
                .build()
            chain.proceed(newRequest)
        }.build()

    // Create Moshi object
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Create Retrofit object
    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(BASE_URL)
        .build()

    val retrofitService: TodoApiService by lazy {
        retrofit.create(TodoApiService::class.java)
    }
}