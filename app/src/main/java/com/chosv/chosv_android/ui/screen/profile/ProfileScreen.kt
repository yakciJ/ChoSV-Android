package com.chosv.chosv_android.ui.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chosv.chosv_android.ChoSVApplication
import com.chosv.chosv_android.convertBaseUrl
import com.chosv.chosv_android.data.model.Screen
import java.net.URLEncoder

@Composable
fun ProfileScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ChoSVApplication
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.provideFactory(
            userRepository = application.container.userRepository,
            imageRepository = application.container.imageRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateAvatar(it) }
    }

    // Handle logout success
    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Show success snackbar
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                val currentUser = uiState.currentUser

                // Avatar with camera icon overlay
                Box(contentAlignment = Alignment.BottomEnd) {
                    if (!currentUser?.avatarImage.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context = LocalContext.current)
                                .data(convertBaseUrl(currentUser?.avatarImage ?: ""))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Avatar",
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Camera icon
                    if (currentUser != null) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isUploadingAvatar) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Đổi ảnh",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User name
                Text(
                    text = currentUser?.fullName ?: "Chưa đăng nhập",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (currentUser != null) {
                    Text(
                        text = "@${currentUser.userName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons for logged-in user
                if (currentUser != null) {
                    // Xem trang cá nhân
                    Button(
                        onClick = {
                            val encodedUserName = URLEncoder.encode(currentUser.userName, "UTF-8")
                            navController.navigate("user_profile/$encodedUserName")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Xem trang cá nhân")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sản phẩm yêu thích
                    Button(
                        onClick = { navController.navigate(Screen.Favorites.route) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Sản phẩm yêu thích")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sửa thông tin cá nhân
                    Button(
                        onClick = { viewModel.showEditProfileDialog() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Sửa thông tin cá nhân")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Đổi mật khẩu
                    Button(
                        onClick = { viewModel.showChangePasswordDialog() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Đổi mật khẩu")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Đăng xuất
                    Button(
                        onClick = { viewModel.showLogoutConfirmDialog() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Đăng xuất")
                    }
                } else {
                    // Not logged in - show favorites button only
                    Button(
                        onClick = { navController.navigate(Screen.Favorites.route) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Sản phẩm yêu thích")
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Edit Profile Dialog
    if (uiState.showEditProfileDialog) {
        EditProfileDialog(
            fullName = uiState.editFullName,
            bio = uiState.editBio,
            address = uiState.editAddress,
            phoneNumber = uiState.editPhoneNumber,
            isLoading = uiState.isUpdatingProfile,
            onFullNameChange = viewModel::onEditFullNameChange,
            onBioChange = viewModel::onEditBioChange,
            onAddressChange = viewModel::onEditAddressChange,
            onPhoneNumberChange = viewModel::onEditPhoneNumberChange,
            onConfirm = viewModel::updateProfile,
            onDismiss = viewModel::hideEditProfileDialog
        )
    }

    // Change Password Dialog
    if (uiState.showChangePasswordDialog) {
        ChangePasswordDialog(
            oldPassword = uiState.oldPassword,
            newPassword = uiState.newPassword,
            confirmPassword = uiState.confirmPassword,
            isLoading = uiState.isChangingPassword,
            onOldPasswordChange = viewModel::onOldPasswordChange,
            onNewPasswordChange = viewModel::onNewPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onConfirm = viewModel::changePassword,
            onDismiss = viewModel::hideChangePasswordDialog
        )
    }

    // Logout Confirm Dialog
    if (uiState.showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideLogoutConfirmDialog,
            title = { Text("Xác nhận đăng xuất") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất?") },
            confirmButton = {
                Button(
                    onClick = viewModel::logout,
                    enabled = !uiState.isLoggingOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    if (uiState.isLoggingOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Đăng xuất", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideLogoutConfirmDialog) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun EditProfileDialog(
    fullName: String,
    bio: String,
    address: String,
    phoneNumber: String,
    isLoading: Boolean,
    onFullNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sửa thông tin cá nhân") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Họ và tên") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = onBioChange,
                    label = { Text("Giới thiệu") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    label = { Text("Địa chỉ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading && fullName.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Lưu")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    oldPassword: String,
    newPassword: String,
    confirmPassword: String,
    isLoading: Boolean,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi mật khẩu") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = onOldPasswordChange,
                    label = { Text("Mật khẩu cũ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                            Icon(
                                imageVector = if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (oldPasswordVisible) "Ẩn" else "Hiện"
                            )
                        }
                    }
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = { Text("Mật khẩu mới") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (newPasswordVisible) "Ẩn" else "Hiện"
                            )
                        }
                    }
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text("Xác nhận mật khẩu mới") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Ẩn" else "Hiện"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading && oldPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Đổi mật khẩu")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}