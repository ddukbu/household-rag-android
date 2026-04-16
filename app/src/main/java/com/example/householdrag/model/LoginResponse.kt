package com.example.householdrag.model

// 로그인 응답 바디.
// 서버 구현 차이를 흡수하기 위해 토큰 필드를 optional로 둔다.
data class LoginResponse(
    // 일부 서버는 token 하나만 내려준다.
    val token: String? = null,
    // 일부 서버는 accessToken/refreshToken을 분리해 내려준다.
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val userId: String? = null,
    val message: String? = null
)
