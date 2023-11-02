package com.devapps.storyapp.data.api

import com.devapps.storyapp.data.request.LoginRequest
import com.devapps.storyapp.data.request.RegisterRequest
import com.devapps.storyapp.data.response.AppResponse
import com.devapps.storyapp.data.response.LoginResponse
import com.devapps.storyapp.data.response.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.*
import retrofit2.http.*

interface ApiService {
    @POST("register")
    fun register(
        @Body request: RegisterRequest
    ): Call<AppResponse>

    @POST("login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @GET("stories")
    fun getStories(
        @Header("Authorization") token: String,
    ): Call<StoryResponse>

    @Multipart
    @POST("stories")
    fun addStory(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Call<AppResponse>
}