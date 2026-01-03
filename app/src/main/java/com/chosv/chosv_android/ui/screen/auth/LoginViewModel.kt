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
import com.chosv.chosv_android.data.repository.UserRepository
import com.chosv.chosv_android.data.model.*

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    var userName = mutableStateOf("")
        private set

    var password = mutableStateOf("")
        private set

    var passwordVisible = mutableStateOf(false)
        private set

    var rememberMe = mutableStateOf(true)
        private set

    // Forgot password states
    var showForgotPasswordDialog = mutableStateOf(false)
        private set
    var forgotPasswordEmail = mutableStateOf("")
        private set
    var isSendingForgotPassword = mutableStateOf(false)
        private set
    var forgotPasswordMessage = mutableStateOf<String?>(null)
        private set
    var forgotPasswordError = mutableStateOf<String?>(null)
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

    // Forgot password functions
    fun showForgotPasswordDialog() {
        forgotPasswordEmail.value = ""
        forgotPasswordMessage.value = null
        forgotPasswordError.value = null
        showForgotPasswordDialog.value = true
    }

    fun hideForgotPasswordDialog() {
        showForgotPasswordDialog.value = false
    }

    fun onForgotPasswordEmailChanged(newValue: String) {
        forgotPasswordEmail.value = newValue
    }

    fun onSendForgotPassword() {
        if (forgotPasswordEmail.value.isBlank()) {
            forgotPasswordError.value = "Vui lòng nhập email"
            return
        }

        isSendingForgotPassword.value = true
        forgotPasswordError.value = null
        forgotPasswordMessage.value = null

        viewModelScope.launch {
            userRepository.forgotPassword(forgotPasswordEmail.value).fold(
                onSuccess = { message ->
                    forgotPasswordMessage.value = message
                    isSendingForgotPassword.value = false
                },
                onFailure = { e ->
                    forgotPasswordError.value = e.message
                    isSendingForgotPassword.value = false
                }
            )
        }
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
                    loginError.value = "Tài khoản hoặc mật khẩu không đúng!"
                }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ChoSVApplication)
                val authRepository = application.container.authRepository
                val userRepository = application.container.userRepository
                LoginViewModel(
                    authRepository = authRepository,
                    userRepository = userRepository
                )
            }
        }
    }
}