package com.example.householdrag.auth

import com.google.firebase.auth.FirebaseAuth

// Firebase Authentication 래퍼.
// 이메일/비밀번호 인증과 ID 토큰 조회를 단순 콜백 형태로 제공한다.
object FirebaseAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
                    onResult(false, task.exception?.message)
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
                    onResult(false, task.exception?.message)
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
