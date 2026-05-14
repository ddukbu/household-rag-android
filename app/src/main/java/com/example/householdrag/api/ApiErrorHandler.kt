package com.example.householdrag.api

import android.util.Log
import com.example.householdrag.model.ErrorDetail
import com.google.gson.Gson
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// API 호출 실패 시 서버 에러 응답({"detail": "..."})을 파싱하고,
// 사용자/로그용 메시지로 변환하는 공통 헬퍼.
object ApiErrorHandler {
    private val gson = Gson()
    private const val TAG = "ApiErrorHandler"

    /**
     * HttpException에서 서버 detail 메시지 추출.
     * 파싱 실패 시 기본 메시지 반환.
     */
    fun getServerDetail(exception: HttpException): String? {
        return try {
            val errorBody = exception.response()?.errorBody()?.string() ?: return null
            val detail = gson.fromJson(errorBody, ErrorDetail::class.java)
            detail?.detail
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse error detail: ${e.message}")
            null
        }
    }

    /**
     * 모든 예외를 분류해 사용자에게 보여줄 메시지 생성.
     * 네트워크 에러, HTTP 에러, 기타 예외를 구분.
     */
    fun getUserMessage(exception: Exception): String {
        return when (exception) {
            is SocketTimeoutException -> "요청이 시간 초과되었습니다. 네트워크 상태를 확인해 주세요."
            is UnknownHostException -> "서버에 연결할 수 없습니다. 인터넷 연결을 확인해 주세요."
            is HttpException -> {
                val serverDetail = getServerDetail(exception)
                when {
                    serverDetail != null -> serverDetail
                    exception.code() == 401 -> "인증이 만료되었습니다. 다시 로그인해 주세요."
                    exception.code() == 403 -> "접근 권한이 없습니다."
                    exception.code() == 404 -> "요청한 리소스를 찾을 수 없습니다."
                    exception.code() == 400 -> "요청이 잘못되었습니다."
                    exception.code() in 500..599 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
                    else -> "통신 오류가 발생했습니다."
                }
            }
            else -> exception.message ?: "알 수 없는 오류가 발생했습니다."
        }
    }

}
