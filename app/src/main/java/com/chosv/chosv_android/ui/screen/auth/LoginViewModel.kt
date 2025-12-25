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
import com.chosv.chosv_android.preferences.TokenPreferences
import com.chosv.chosv_android.data.model.*

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val tokenPreferences: TokenPreferences
) : ViewModel() {
    var userName = mutableStateOf("")
        private set

    var password = mutableStateOf("")
        private set

    var passwordVisible = mutableStateOf(false)
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

    var loginSuccess = mutableStateOf(false)
    var loginError = mutableStateOf<String?>(null)


    fun onLoginClick() {
        // 1. Set state về null / loading (nếu muốn)
        loginError.value = null
        viewModelScope.launch {
            try {
                // 2. Gọi API login và nhận AuthResponse
                val response: AuthResponse = authRepository.login(
                    LoginRequest(
                        userName = userName.value,
                        password = password.value
                    )
                )
                // 3. Lưu token vào DataStore qua TokenPreferences
                tokenPreferences.updateAccessToken(response.accessToken)
                Log.d("LoginViewModel", "Login thành công, token: ${response.accessToken}")

                // 4. Đánh dấu login thành công
                loginSuccess.value = true
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login thất bại: ${e.localizedMessage}")
                // 5. Nếu có lỗi (mạng, 401, v.v.), set loginError
                loginError.value = "Sai tài khoản hoặc mật khẩu"
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ChoSVApplication)
                val authRepository = application.container.authRepository
                val tokenPreferences = application.container.tokenPreferences
                LoginViewModel(
                    authRepository = authRepository,
                    tokenPreferences = tokenPreferences
                )
            }
        }
    }
}