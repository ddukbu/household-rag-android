package com.example.householdrag.api

import android.content.Context
import com.example.householdrag.auth.AuthSessionEvents
import com.example.householdrag.auth.AuthTokenStore
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 네트워크 클라이언트 단일 진입점.
// - Authorization 헤더 자동 주입
// - 401 응답 공통 처리(토큰 제거 + 세션 만료 이벤트)
object ApiClient {
    private const val BASE_URL = "https://household-rag-server.onrender.com/"
    @Volatile
    // 인터셉터에서 토큰 저장소 접근 시 필요한 앱 컨텍스트.
    private var appContext: Context? = null

    // 앱 시작 시 1회 호출해 애플리케이션 컨텍스트를 주입한다.
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // Render와 같은 환경의 cold start를 고려해 기본 timeout을 여유 있게 설정한다.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                // 저장된 access token을 읽어 현재 요청에 붙일지 판단한다.
                val accessToken = appContext?.let { AuthTokenStore.getAccessToken(it) }

                val requestBuilder = original.newBuilder()
                if (!accessToken.isNullOrBlank()) {
                    // 인증이 필요한 API를 위해 Bearer 토큰을 자동 첨부한다.
                    requestBuilder.addHeader("Authorization", "Bearer $accessToken")
                }

                val response = chain.proceed(requestBuilder.build())
                if (response.code() == 401) {
                    // 토큰이 만료/무효화된 경우 로컬 세션을 정리하고 UI에 알린다.
                    appContext?.let { AuthTokenStore.clear(it) }
                    AuthSessionEvents.notifySessionExpired()
                }

                response
            }
            .build()
    }

    // 앱 전역에서 사용할 Retrofit API 구현체.
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}