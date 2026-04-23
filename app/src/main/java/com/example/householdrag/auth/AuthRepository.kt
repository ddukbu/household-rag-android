package com.example.householdrag.auth

import android.content.Context
import com.example.householdrag.api.ApiClient
import com.example.householdrag.model.ProfileInitRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Firebase 인증 결과를 현재 앱의 토큰 저장소/Auth API 흐름과 연결하는 어댑터.
object AuthRepository {

    fun signUpAndInitProfile(
        context: Context,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        FirebaseAuthManager.signUp(email, password) { success, error ->
            if (!success) {
                onResult(false, error)
                return@signUp
            }

            FirebaseAuthManager.getIdToken { token ->
                if (token.isNullOrBlank()) {
                    onResult(false, "ID Token을 가져오지 못했습니다.")
                    return@getIdToken
                }

                AuthTokenStore.saveTokens(
                    context = context,
                    accessToken = token,
                    refreshToken = null,
                    userId = FirebaseAuthManager.getCurrentUserUid()
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        ApiClient.api.initProfile(ProfileInitRequest(email))
                        onResult(true, null)
                    } catch (e: Exception) {
                        val warning = when (e) {
                            is SocketTimeoutException -> "회원가입은 완료됐지만 서버 응답이 지연되어 프로필 초기화가 늦어지고 있습니다."
                            is UnknownHostException -> "회원가입은 완료됐지만 네트워크 문제로 프로필 초기화에 실패했습니다."
                            else -> "회원가입은 완료됐지만 프로필 초기화에 실패했습니다."
                        }
                        onResult(true, warning)
                    }
                }
            }
        }
    }

    fun loginAndSetFirebaseIdToken(
        context: Context,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        FirebaseAuthManager.login(email, password) { success, error ->
            if (!success) {
                onResult(false, error)
                return@login
            }

            FirebaseAuthManager.getIdToken { token ->
                if (token.isNullOrBlank()) {
                    onResult(false, "ID Token을 가져오지 못했습니다.")
                } else {
                    AuthTokenStore.saveTokens(
                        context = context,
                        accessToken = token,
                        refreshToken = null,
                        userId = FirebaseAuthManager.getCurrentUserUid()
                    )
                    onResult(true, null)
                }
            }
        }
    }
}
