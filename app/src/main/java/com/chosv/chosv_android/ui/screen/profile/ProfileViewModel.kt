package com.chosv.chosv_android.ui.screen.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chosv.chosv_android.data.model.ChangePasswordRequest
import com.chosv.chosv_android.data.model.UpdateProfileRequest
import com.chosv.chosv_android.data.model.UserProfile
import com.chosv.chosv_android.data.repository.ImageRepository
import com.chosv.chosv_android.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val currentUser: UserProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null,

    // Edit profile dialog
    val showEditProfileDialog: Boolean = false,
    val editFullName: String = "",
    val editBio: String = "",
    val editAddress: String = "",
    val editPhoneNumber: String = "",
    val isUpdatingProfile: Boolean = false,

    // Change password dialog
    val showChangePasswordDialog: Boolean = false,
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isChangingPassword: Boolean = false,

    // Avatar
    val isUploadingAvatar: Boolean = false,

    // Logout
    val showLogoutConfirmDialog: Boolean = false,
    val isLoggingOut: Boolean = false,
    val logoutSuccess: Boolean = false
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = userRepository.getCurrentUser()
            _uiState.update { it.copy(currentUser = user, isLoading = false) }
        }
    }

    // Edit Profile Dialog
    fun showEditProfileDialog() {
        val user = _uiState.value.currentUser
        _uiState.update {
            it.copy(
                showEditProfileDialog = true,
                editFullName = user?.fullName ?: "",
                editBio = user?.bio ?: "",
                editAddress = user?.address ?: "",
                editPhoneNumber = user?.phoneNumber ?: ""
            )
        }
    }

    fun hideEditProfileDialog() {
        _uiState.update { it.copy(showEditProfileDialog = false) }
    }

    fun onEditFullNameChange(value: String) {
        _uiState.update { it.copy(editFullName = value) }
    }

    fun onEditBioChange(value: String) {
        _uiState.update { it.copy(editBio = value) }
    }

    fun onEditAddressChange(value: String) {
        _uiState.update { it.copy(editAddress = value) }
    }

    fun onEditPhoneNumberChange(value: String) {
        _uiState.update { it.copy(editPhoneNumber = value) }
    }

    fun updateProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingProfile = true, error = null) }
            val request = UpdateProfileRequest(
                fullName = _uiState.value.editFullName,
                bio = _uiState.value.editBio,
                address = _uiState.value.editAddress,
                phoneNumber = _uiState.value.editPhoneNumber
            )
            userRepository.updateProfile(request).fold(
                onSuccess = { message ->
                    _uiState.update {
                        it.copy(
                            isUpdatingProfile = false,
                            showEditProfileDialog = false,
                            successMessage = message
                        )
                    }
                    loadCurrentUser() // Reload user info
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isUpdatingProfile = false,
                            error = e.message
                        )
                    }
                }
            )
        }
    }

    // Change Password Dialog
    fun showChangePasswordDialog() {
        _uiState.update {
            it.copy(
                showChangePasswordDialog = true,
                oldPassword = "",
                newPassword = "",
                confirmPassword = ""
            )
        }
    }

    fun hideChangePasswordDialog() {
        _uiState.update { it.copy(showChangePasswordDialog = false) }
    }

    fun onOldPasswordChange(value: String) {
        _uiState.update { it.copy(oldPassword = value) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }

    fun changePassword() {
        val state = _uiState.value
        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(error = "Mật khẩu xác nhận không khớp") }
            return
        }
        if (state.newPassword.length < 6) {
            _uiState.update { it.copy(error = "Mật khẩu mới phải có ít nhất 6 ký tự") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true, error = null) }
            val request = ChangePasswordRequest(
                oldPassword = state.oldPassword,
                newPassword = state.newPassword,
                confirmPassword = state.confirmPassword
            )
            userRepository.changePassword(request).fold(
                onSuccess = { message ->
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            showChangePasswordDialog = false,
                            successMessage = message
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            error = e.message
                        )
                    }
                }
            )
        }
    }

    // Avatar
    fun updateAvatar(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, error = null) }
            try {
                // Upload image first
                val uploadResponse = imageRepository.uploadImage(uri)
                val imageUrl = uploadResponse.imageUrl
                // Then update avatar
                userRepository.updateAvatar(imageUrl).fold(
                    onSuccess = { message ->
                        _uiState.update {
                            it.copy(
                                isUploadingAvatar = false,
                                successMessage = message
                            )
                        }
                        loadCurrentUser() // Reload user info
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isUploadingAvatar = false,
                                error = e.message
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploadingAvatar = false,
                        error = "Lỗi upload ảnh: ${e.message}"
                    )
                }
            }
        }
    }

    // Logout
    fun showLogoutConfirmDialog() {
        _uiState.update { it.copy(showLogoutConfirmDialog = true) }
    }

    fun hideLogoutConfirmDialog() {
        _uiState.update { it.copy(showLogoutConfirmDialog = false) }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            userRepository.logout()
            _uiState.update {
                it.copy(
                    isLoggingOut = false,
                    showLogoutConfirmDialog = false,
                    logoutSuccess = true
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    companion object {
        fun provideFactory(
            userRepository: UserRepository,
            imageRepository: ImageRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(userRepository, imageRepository) as T
            }
        }
    }
}

