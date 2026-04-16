package com.example.householdrag.auth

import android.content.Context
import com.example.householdrag.model.LoginResponse

// 로그인 토큰을 로컬에 저장/조회/삭제하는 단순 저장소.
// 현재는 SharedPreferences를 사용한다.
object AuthTokenStore {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 서버 로그인 응답 형태(token 또는 accessToken)를 흡수해 저장한다.
    fun saveFromLoginResponse(context: Context, response: LoginResponse) {
        val resolvedAccessToken = response.accessToken ?: response.token
        saveTokens(
            context = context,
            accessToken = resolvedAccessToken,
            refreshToken = response.refreshToken,
            userId = response.userId
        )
    }

    // null/blank가 들어오면 기존 값을 제거해 일관된 저장 상태를 유지한다.
    fun saveTokens(
        context: Context,
        accessToken: String?,
        refreshToken: String?,
        userId: String?
    ) {
        prefs(context).edit().apply {
            if (accessToken.isNullOrBlank()) remove(KEY_ACCESS_TOKEN) else putString(KEY_ACCESS_TOKEN, accessToken)
            if (refreshToken.isNullOrBlank()) remove(KEY_REFRESH_TOKEN) else putString(KEY_REFRESH_TOKEN, refreshToken)
            if (userId.isNullOrBlank()) remove(KEY_USER_ID) else putString(KEY_USER_ID, userId)
        }.apply()
    }

    fun getAccessToken(context: Context): String? =
        prefs(context).getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(context: Context): String? =
        prefs(context).getString(KEY_REFRESH_TOKEN, null)

    fun getUserId(context: Context): String? =
        prefs(context).getString(KEY_USER_ID, null)

    // access token이 있으면 로그인된 상태로 간주한다.
    fun hasAccessToken(context: Context): Boolean =
        !getAccessToken(context).isNullOrBlank()

    // 로그아웃 또는 401 만료 처리 시 전체 인증 정보를 삭제한다.
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
