package com.kiki.common.result

import com.kiki.common.result.exception.PayloadException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PayloadResultTest {

    @Test
    fun testCreateSuccessResult() {
        val result : PayloadResult<String> = PayloadResult.success("Success")
        assertTrue(result.isSuccess())
        assertNotNull(result.getOrNull())
        assertNull(result.exceptionOrNull())
    }

    @Test
    fun testCreateFailResult() {
        val result : PayloadResult<String> = PayloadResult.failure(PayloadException("에러"))
        assertFalse(result.isSuccess())
        assertNull(result.getOrNull())
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun testGetElseResultWhenFailure() {
        val result : PayloadResult<String> = PayloadResult.failure(PayloadException("실패"))
        assertFalse(result.isSuccess())
        assertEquals("실패지만 성공", result.getOrElse { "실패지만 성공" })
    }
}