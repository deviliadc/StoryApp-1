package com.devapps.storyapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devapps.storyapp.data.Resource
import com.devapps.storyapp.data.api.ApiConfig
import com.devapps.storyapp.data.pref.UserPreferences
import com.devapps.storyapp.data.request.RegisterRequest
import com.devapps.storyapp.data.response.AppResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response as RetrofitResponse

class RegisterViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _registerResult = MutableLiveData<Resource<String>>()
    val registerResult: LiveData<Resource<String>> = _registerResult

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerResult.postValue(Resource.Loading())
            try {
                ApiConfig.getApiClient().register(RegisterRequest(name, email, password))
                    .enqueue(object : Callback<AppResponse> {
                        override fun onResponse(call: Call<AppResponse>, response: RetrofitResponse<AppResponse>) {
                            handleRegisterResponse(response)
                        }

                        override fun onFailure(call: Call<AppResponse>, t: Throwable) {
                            _registerResult.postValue(Resource.Error("An error occurred"))
                        }
                    })
            } catch (e: Exception) {
                Log.e(RegisterViewModel::class.java.simpleName, "Exception during register: $e")
                _registerResult.postValue(Resource.Error("An error occurred"))
            }
        }
    }

    private fun handleRegisterResponse(response: RetrofitResponse<AppResponse>) {
        if (response.isSuccessful) {
            val message = response.body()?.message.toString()
            _registerResult.postValue(Resource.Success(message))
        } else {
            val errorResponse = Gson().fromJson(
                response.errorBody()?.charStream(),
                AppResponse::class.java
            )
            _registerResult.postValue(Resource.Error(errorResponse?.message ?: "Unknown error"))
        }
    }
}
