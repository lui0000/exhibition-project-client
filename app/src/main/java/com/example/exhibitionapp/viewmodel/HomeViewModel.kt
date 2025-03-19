package com.example.exhibitionapp.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exhibitionapp.RetrofitClient
import com.example.exhibitionapp.dataclass.UserResponse
import com.example.exhibitionapp.services.UserService
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _user = MutableLiveData<UserResponse?>()
    val user: LiveData<UserResponse?> = _user

    private val userService: UserService = RetrofitClient.createService(UserService::class.java)

    fun loadUser(token: String, userId: Int) {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Fetching user info for userId: $userId")
                val userResponse = userService.getUserInfo("Bearer $token", userId)
                Log.d("HomeViewModel", "User response received: $userResponse")
                _user.postValue(userResponse)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading user", e)
                _user.postValue(null)
            }
        }
    }
}
