package com.devapps.storyapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.devapps.storyapp.data.Resource
import com.devapps.storyapp.data.api.ApiConfig
import com.devapps.storyapp.data.model.Story
import com.devapps.storyapp.data.pref.UserPreferences
import com.devapps.storyapp.data.response.AppResponse
import com.devapps.storyapp.data.response.StoryResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(private val pref: UserPreferences) : ViewModel() {

    private val _stories = MutableLiveData<Resource<ArrayList<Story>>>()
    val stories: LiveData<Resource<ArrayList<Story>>> = _stories

    private val _logoutResult = MutableLiveData<Resource<Unit>>()
    val logoutResult: LiveData<Resource<Unit>> = _logoutResult

    fun getStories() {
        viewModelScope.launch {
            _stories.postValue(Resource.Loading())
            try {
                val response: Response<StoryResponse> =
                    ApiConfig.getApiClient().getStories(token = "Bearer ${pref.getSession().first()}")
                handleStoriesResponse(response)
            } catch (e: Exception) {
                Log.e(MainViewModel::class.java.simpleName, "Exception during getStories: $e")
                _stories.postValue(Resource.Error("An error occurred"))
            }
        }
    }

    private fun handleStoriesResponse(response: Response<StoryResponse>) {
        try {
            if (response.isSuccessful) {
                response.body()?.let {
                    val listStory = it.listStory
                    _stories.postValue(Resource.Success(ArrayList(listStory)))
                }
            } else {
                val errorResponse = Gson().fromJson(
                    response.errorBody()?.charStream(),
                    AppResponse::class.java
                )
                _stories.postValue(Resource.Error(errorResponse.message))
            }
        } catch (e: Exception) {
            Log.e(MainViewModel::class.java.simpleName, "Exception during handleStoriesResponse: $e")
            _stories.postValue(Resource.Error("An error occurred"))
        }
    }

    fun logout() {
        viewModelScope.launch {
            deleteUserToken()
            _logoutResult.postValue(Resource.Success(Unit))
        }
    }

    fun getUserToken() = pref.getSession().asLiveData()

    private suspend fun deleteUserToken() {
        pref.deleteSession()
    }
}
