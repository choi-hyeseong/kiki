package com.kiki.common.result

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.kiki.common.result.exception.PayloadException

/**
 * 기존 kotlin Result에서 영감을 받아 CustomException을 받는 Result 클래스
 * @property data 내부에서 success와 failure의 값이 들어있는 value입니다.
 * @property exception 원래는 data를 제네릭으로 만들어 쓰려 했으나.. jackson deserialize가 너무 안되서 어쩔수 없이..
 */
data class PayloadResult<T>(@JsonProperty("data") val data: T?, @JsonProperty("exception") val exception: PayloadException?) {

    companion object {

        @JsonIgnore
        fun <T : Any> success(data: T): PayloadResult<T> {
            return PayloadResult(data, null)
        }

        @JsonIgnore
        fun <T> failure(exception: PayloadException): PayloadResult<T> {
            return PayloadResult(null, exception)
        }
    }

    @JsonIgnore
    fun isSuccess(): Boolean {
        return data != null
    }

    @JsonIgnore
    fun getOrNull(): T? {
        return if (isSuccess())
            return data
        else
            null
    }

    @JsonIgnore
    fun getOrElse(block: () -> T): T {
        return getOrNull() ?: block()
    }

    @JsonIgnore
    fun exceptionOrNull(): PayloadException? {
        return if (!isSuccess())
            exception
        else
            null
    }

}