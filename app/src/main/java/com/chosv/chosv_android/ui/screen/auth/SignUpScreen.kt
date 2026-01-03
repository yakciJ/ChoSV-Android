package com.chosv.chosv_android.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val viewModel: SignUpViewModel = viewModel(factory = SignUpViewModel.Factory)

    val userName by viewModel.userName
    val password by viewModel.password
    val confirmPassword by viewModel.confirmPassword
    val email by viewModel.email
    val passwordVisible by viewModel.passwordVisible
    val signUpSuccess by viewModel.registerSuccess
    val registerError by viewModel.registerError
    val isLoading by viewModel.isLoading

    // Dialog thành công
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(signUpSuccess) {
        if (signUpSuccess) {
            showSuccessDialog = true
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Thành công") },
            text = { Text("Đăng ký thành công. Vui lòng kiểm tra email để kích hoạt tài khoản.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearRegisterSuccess()
                        onBackToLogin()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "ĐĂNG KÝ TÀI KHOẢN",
                        fontSize = 24.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // TODO: Xử lý quay lại ở đây, ví dụ: navController.popBackStack()
                        onBackToLogin()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // quan trọng: để nội dung tránh đè lên top bar
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//            Image(
//                painter = painterResource(id = R.drawable.ttg_logo),
//                contentDescription = "Logo",
//                modifier = Modifier
//                    .clip(CircleShape)
//            )
//            Spacer(modifier = Modifier.fillMaxHeight(0.0078f))

//            Text(
//                text = "ĐĂNG KÝ TÀI KHOẢN",
//                textAlign = TextAlign.Center,
//                fontSize = 24.sp,
//                modifier = Modifier.fillMaxWidth()
//            )
//            Spacer(modifier = Modifier.height(16.dp))

                Text("Tên Đăng Nhập:", fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.fillMaxHeight(0.0078f))
                OutlinedTextField(
                    value = userName,
                    onValueChange = viewModel::onUserNameChanged,
                    placeholder = { Text("Điền tên đăng nhập của bạn...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Email:", fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.fillMaxHeight(0.0078f))
                OutlinedTextField(
                    value = email,
                    onValueChange = viewModel::onEmailChanged,
                    placeholder = { Text("Điền email của bạn...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Mật Khẩu:", fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.fillMaxHeight(0.0078f))
                TextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChanged,
                    placeholder = { Text("Điền mật khẩu của bạn...") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff
                        val desc = if (passwordVisible) "Ẩn mật khẩu"
                        else "Hiện mật khẩu"
                        IconButton(onClick = viewModel::togglePasswordVisibility) {
                            Icon(imageVector = icon, contentDescription = desc)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,

                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Gray,

                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Nhập lại mật khẩu:", fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.fillMaxHeight(0.0078f))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChanged,
                    placeholder = { Text("Nhập lại mật khẩu của bạn...") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon =
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val desc = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                        IconButton(onClick = viewModel::togglePasswordVisibility) {
                            Icon(imageVector = icon, contentDescription = desc)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )


                Spacer(modifier = Modifier.fillMaxHeight(0.15f))

                // Hiển thị lỗi nếu có
                if (registerError != null) {
                    Text(
                        text = registerError ?: "",
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = viewModel::onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Đăng Ký", fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.fillMaxHeight(0.1f))

//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp), //  khoảng cách giữa 2 Text
//                modifier = Modifier.padding(top = 16.dp)
//            ) {
//                Text("Đã có tài khoản?")
//
//                Text(
//                    text = "Đăng nhập ngay!",
//                    color = Color(0xFF2196F3), // màu xanh
//                    textDecoration = TextDecoration.Underline, // gạch chân
//                    modifier = Modifier.clickable {
//                        //  Chuyển trang tại đây (ví dụ gọi navController hoặc thay đổi state)
//
//                    }
//                )
//            }
            }
        }
    }
}
