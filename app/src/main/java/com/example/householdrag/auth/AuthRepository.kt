package com.example.householdrag.auth

import com.example.householdrag.api.ApiClient
import com.example.householdrag.api.ProfileInitRequest
import com.example.householdrag.api.TokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AuthRepository {

    fun signUpAndInitProfile(
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
                if (token == null) {
                    onResult(false, "ID Token을 가져오지 못했습니다.")
                    return@getIdToken
                }

                TokenProvider.idToken = token

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        ApiClient.api.initProfile(ProfileInitRequest(email))
                        onResult(true, null)
                    } catch (e: Exception) {
                        onResult(false, e.message)
                    }
                }
            }
        }
    }

    fun loginAndSetToken(
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
                if (token == null) {
                    onResult(false, "ID Token을 가져오지 못했습니다.")
                } else {
                    TokenProvider.idToken = token
                    onResult(true, null)
                }
            }
        }
    }
}