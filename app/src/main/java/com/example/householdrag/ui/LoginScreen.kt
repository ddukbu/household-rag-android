package com.example.householdrag.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onSignUpClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        // 이메일 입력창
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 입력창
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(), // 비밀번호 숨기기
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 로그인 버튼
        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Login")
        }

        // 회원가입 유도 버튼
        TextButton(onClick = onSignUpClick) {
            Text("계정이 없으신가요? 회원가입")
        }
    }
}

// 로그인 화면 미리보기
@Preview(showSystemUi = true, name = "로그인 화면 미리보기")
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        // 미리보기이므로 클릭했을 때 아무 동작도 하지 않도록 비워둡니다.
        LoginScreen(
            onLoginClick = { email, password ->
                println("로그인 시도: $email")
            },
            onSignUpClick = {
                println("회원가입으로 이동")
            }
        )
    }
}