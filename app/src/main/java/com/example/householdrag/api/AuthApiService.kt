package com.example.householdrag.api

import com.example.householdrag.model.LoginRequest
import com.example.householdrag.model.LoginResponse
import com.example.householdrag.model.SignupRequest
import com.example.householdrag.model.SignupResponse
import retrofit2.http.Body
import retrofit2.http.POST

// 인증 관련 서버 계약 정의.
// Retrofit이 이 선언을 실제 HTTP 호출 코드로 구현한다.
interface AuthApiService {
    // 이메일/비밀번호로 로그인하고 토큰 정보를 받는다.
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // 새 계정을 생성한다.
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): SignupResponse
}
