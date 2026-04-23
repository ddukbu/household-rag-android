package com.example.householdrag.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException

// Firebase Authentication 래퍼.
// 이메일/비밀번호 인증과 ID 토큰 조회를 단순 콜백 형태로 제공한다.
object FirebaseAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private fun mapAuthErrorMessage(error: Exception?): String {
        return when (error) {
            is FirebaseAuthInvalidUserException -> "가입되지 않은 계정입니다."
            is FirebaseAuthInvalidCredentialsException -> "이메일 또는 비밀번호가 올바르지 않습니다."
            is FirebaseTooManyRequestsException -> "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."
            is FirebaseNetworkException -> "네트워크 상태가 불안정합니다. 인터넷 연결을 확인해 주세요."
            is FirebaseAuthException -> when (error.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "이미 가입된 이메일입니다."
                "ERROR_INVALID_EMAIL" -> "이메일 형식이 올바르지 않습니다."
                "ERROR_WEAK_PASSWORD" -> "비밀번호는 6자 이상이어야 합니다."
                else -> error.message ?: "인증 처리 중 오류가 발생했습니다."
            }
            else -> error?.message ?: "인증 처리 중 오류가 발생했습니다."
        }
    }

    fun signUp(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, mapAuthErrorMessage(task.exception))
                }
            }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, mapAuthErrorMessage(task.exception))
                }
            }
    }

    fun getIdToken(forceRefresh: Boolean = true, onResult: (String?) -> Unit) {
        val user = auth.currentUser
        user?.getIdToken(forceRefresh)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(task.result?.token)
                } else {
                    onResult(null)
                }
            } ?: onResult(null)
    }

    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun logout() {
        auth.signOut()
    }
}
