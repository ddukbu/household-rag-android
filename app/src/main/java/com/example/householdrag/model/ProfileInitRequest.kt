package com.example.householdrag.model

// Firebase 인증 이후 서버 프로필 초기화 요청 바디.
data class ProfileInitRequest(
    val email: String
)
