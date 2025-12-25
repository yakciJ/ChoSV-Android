package com.chosv.chosv_android.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chosv.chosv_android.ChoSVApplication
import com.chosv.chosv_android.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
    val application = LocalContext.current.applicationContext as ChoSVApplication
    val authRepo = application.container.authRepository
    val tokenPrefs = application.container.tokenPreferences

    val userName by viewModel.userName
    val password by viewModel.password
    val passwordVisible by viewModel.passwordVisible
    val loginSuccess by viewModel.loginSuccess
    var rememberMeChecked by remember { mutableStateOf(false) }


    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
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
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .clip(CircleShape)
                    .size(100.dp)
            )
            Spacer(modifier = Modifier.fillMaxHeight(0.0078f))

            Text(
                text = "GTG SHOP",
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.fillMaxHeight(0.0625f))

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

            Spacer(modifier = Modifier.fillMaxHeight(0.0694f))

            Text("Mật Khẩu:", fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.fillMaxHeight(0.0078f))
            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChanged,
                placeholder = { Text("Điền mật khẩu của bạn...") },
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
            Spacer(modifier = Modifier.fillMaxHeight(0.02f))
            Row(modifier = Modifier.fillMaxWidth()){
                RememberMeRow(
                    checked = rememberMeChecked,
                    onCheckedChange = { rememberMeChecked = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 0.dp, bottom = 0.dp)
                )
                Text(
                    text = "Quên mật khẩu?",
                    fontSize = 16.sp,
                    //color = Color(0xFF2196F3), // màu xanh
                    //textDecoration = TextDecoration.Underline, // gạch chân
                    modifier = Modifier.clickable {
                        // gọi api quên mật khẩu
                    }
                        .weight(1f)
                        .fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }


            Spacer(modifier = Modifier.fillMaxHeight(0.1f))

            Button(
                onClick = viewModel::onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                )
            ) {
                Text("Đăng Nhập", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.fillMaxHeight(0.1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp), //  khoảng cách giữa 2 Text
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Chưa có tài khoản?")

                Text(
                    text = "Đăng ký ngay!",
                    color = Color(0xFF2196F3), // màu xanh
                    textDecoration = TextDecoration.Underline, // gạch chân
                    modifier = Modifier.clickable {
                        //  Chuyển trang tại đây (ví dụ gọi navController hoặc thay đổi state)
                        onSignupClick()
                    }
                )
            }
        }
    }
}

@Composable
fun RememberMeRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .size(18.dp),
        )

        Text(
            text = "Ghi nhớ tôi",
            fontSize = 16.sp,
            modifier = Modifier
                .padding(start = 10.dp)
                .clickable { onCheckedChange(!checked) } // cho phép click vào text cũng toggle checkbox
        )
    }
}

