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
import com.chosv.chosv_android.data.model.RegisterRequest
import com.chosv.chosv_android.data.repository.AuthRepository
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    var email = mutableStateOf("")
        private set

    var userName = mutableStateOf("")
        private set

    var password = mutableStateOf("")
        private set

    var passwordVisible = mutableStateOf(false)
        private set

    var confirmPassword = mutableStateOf("")
        private set

    var isLoading = mutableStateOf(false)
        private set

    fun onConfirmPasswordChanged(newValue: String) {
        confirmPassword.value = newValue
    }

    fun onUserNameChanged(newValue: String) {
        userName.value = newValue
    }

    fun onEmailChanged(newValue: String) {
        email.value = newValue
    }

    fun onPasswordChanged(newValue: String) {
        password.value = newValue
    }

    fun togglePasswordVisibility() {
        passwordVisible.value = !passwordVisible.value
    }

    var registerSuccess = mutableStateOf(false)
        private set

    var registerError = mutableStateOf<String?>(null)
        private set

    fun clearRegisterSuccess() {
        registerSuccess.value = false
    }

    fun onRegisterClick() {
        if (password.value != confirmPassword.value) {
            registerError.value = "Mật khẩu không khớp"
            return
        }
        registerError.value = null
        isLoading.value = true

        viewModelScope.launch {
            val result = authRepository.register(
                RegisterRequest(
                    userName = userName.value,
                    email = email.value,
                    password = password.value
                )
            )

            isLoading.value = false

            result.fold(
                onSuccess = { message ->
                    Log.d("SignUpViewModel", "Đăng ký thành công: $message")
                    registerSuccess.value = true
                },
                onFailure = { e ->
                    Log.e("SignUpViewModel", "Đăng ký thất bại: ${e.localizedMessage}")
                    registerError.value = e.message ?: "Đăng ký thất bại"
                }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ChoSVApplication)
                val authRepository = application.container.authRepository
                SignUpViewModel(authRepository = authRepository)
            }
        }
    }
}