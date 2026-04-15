package com.example.householdrag.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// 인증 만료 같은 세션 이벤트를 UI로 전달하는 경량 이벤트 버스.
object AuthSessionEvents {
    // 버퍼 1칸을 둬서 발행 시점에 구독자가 잠시 없더라도 이벤트 유실을 줄인다.
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired = _sessionExpired.asSharedFlow()

    // 401 처리 이후 호출되어 UI가 재로그인 안내를 할 수 있게 한다.
    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}
