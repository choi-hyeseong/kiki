package com.kiki.common.socket

import com.kiki.common.socket.queue.PacketQueue
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.Socket

class ClientSocketTest {

    @Test
    fun testIsRunningTrue() {
        // final class라 mock 안됨..
        val socket : Socket = mockk()
        val buffer = ByteArray(1024)
        val inputStream = ByteArrayInputStream(buffer)
        val outputStream = ByteArrayOutputStream()
        every { socket.getInputStream() } returns inputStream
        every { socket.getOutputStream() } returns outputStream

        val clientSocket = ClientSocket(1, PacketQueue(mockk()), socket)
        clientSocket.run()
        assertTrue(clientSocket.javaClass.getField("isRunning").get(clientSocket) as Boolean)
    }
}