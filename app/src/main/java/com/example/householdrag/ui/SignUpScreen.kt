package com.example.householdrag.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.householdrag.ui.theme.CommonTextField

@Composable
fun SignUpScreen(
    onSignUpClick: (String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    val isPasswordMismatch = password.isNotEmpty() && passwordConfirm.isNotEmpty() && password != passwordConfirm

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        // Text("가계부 관리를 시작해보세요", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)

        Spacer(modifier = Modifier.height(32.dp))

        // 이메일 입력
        CommonTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 입력
        CommonTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 확인 입력
        OutlinedTextField(
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            visualTransformation = PasswordVisualTransformation(),
            isError = isPasswordMismatch,
            supportingText = {
                if (isPasswordMismatch) {
                    Text("비밀번호가 일치하지 않습니다.")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.Black,
                focusedLeadingIconColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 가입 완료 버튼
        Button(
            onClick = { onSignUpClick(email, password) },
            enabled = password == passwordConfirm && email.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign Up")
        }

        // 뒤로가기 버튼
        TextButton(onClick = onBackToLogin) {
            Text("이미 계정이 있나요? 로그인하러 가기", color = Color.Gray)
        }
    }
}

@Preview(showSystemUi = true, name = "회원가입 화면 (닉네임 제외)")
@Composable
fun SignUpScreenPreview() {
    MaterialTheme {
        SignUpScreen(onSignUpClick = { _, _ -> }, onBackToLogin = { })
    }
}