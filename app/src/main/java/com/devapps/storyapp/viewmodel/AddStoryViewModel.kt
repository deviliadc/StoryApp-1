package com.devapps.storyapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devapps.storyapp.data.Resource
import com.devapps.storyapp.data.api.ApiConfig
import com.devapps.storyapp.data.pref.UserPreferences
import com.devapps.storyapp.data.response.AppResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddStoryViewModel(private val pref: UserPreferences) : ViewModel() {

    private val _uploadInfo = MutableLiveData<Resource<String>>()
    val uploadInfo: LiveData<Resource<String>> = _uploadInfo

    suspend fun uploadStory(
        imageMultipart: MultipartBody.Part,
        description: RequestBody,
    ) {
        _uploadInfo.postValue(Resource.Loading())
        val client = ApiConfig.getApiClient().addStory(
            token = "Bearer ${pref.getSession().first()}",
            imageMultipart,
            description
        )

        client.enqueue(object : Callback<AppResponse> {
            override fun onResponse(
                call: Call<AppResponse>,
                response: Response<AppResponse>
            ) {
                if (response.isSuccessful) {
                    _uploadInfo.postValue(Resource.Success(response.body()?.message))
                } else {
                    val errorResponse = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        AppResponse::class.java
                    )
                    _uploadInfo.postValue(Resource.Error(errorResponse.message))
                }
            }

            override fun onFailure(call: Call<AppResponse>, t: Throwable) {
                Log.e(
                    AddStoryViewModel::class.java.simpleName,
                    "onFailure upload"
                )
                _uploadInfo.postValue(Resource.Error(t.message))
            }
        })
    }
}