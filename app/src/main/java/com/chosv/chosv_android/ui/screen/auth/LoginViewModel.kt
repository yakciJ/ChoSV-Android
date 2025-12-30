package com.chosv.chosv_android.ui.screen.auth

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chosv.chosv_android.ChoSVApplication
import kotlinx.coroutines.launch
import com.chosv.chosv_android.data.repository.AuthRepository
import com.chosv.chosv_android.data.model.*

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var userName = mutableStateOf("")
        private set

    var password = mutableStateOf("")
        private set

    var passwordVisible = mutableStateOf(false)
        private set

    var rememberMe = mutableStateOf(true)
        private set

    fun onUserNameChanged(newValue: String) {
        userName.value = newValue
    }

    fun onPasswordChanged(newValue: String) {
        password.value = newValue
    }

    fun togglePasswordVisibility() {
        passwordVisible.value = !passwordVisible.value
    }

    fun toggleRememberMe() {
        rememberMe.value = !rememberMe.value
    }

    var loginSuccess = mutableStateOf(false)
    var loginError = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    fun onLoginClick() {
        loginError.value = null
        isLoading.value = true

        viewModelScope.launch {
            val result = authRepository.login(
                LoginRequest(
                    userName = userName.value,
                    password = password.value,
                    rememberMe = rememberMe.value
                )
            )

            isLoading.value = false

            result.fold(
                onSuccess = { response ->
                    Log.d("LoginViewModel", "Login thành công, token: ${response.accessToken}")
                    loginSuccess.value = true
                },
                onFailure = { e ->
                    Log.e("LoginViewModel", "Login thất bại: ${e.localizedMessage}")
                    loginError.value = "Sai tài khoản hoặc mật khẩu"
                }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ChoSVApplication)
                val authRepository = application.container.authRepository
                LoginViewModel(
                    authRepository = authRepository
                )
            }
        }
    }
}