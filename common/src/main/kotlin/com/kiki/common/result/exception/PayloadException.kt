package com.kiki.common.result.exception

/**
 * Payload에 발생한 exception 클래스
 * 기존 Exception Class는 stack trace가 있어 역직렬화가 어려움
 */
data class PayloadException(val message : String)