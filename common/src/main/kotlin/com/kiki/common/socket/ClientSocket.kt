package com.kiki.common.socket

import com.kiki.common.packet.Packet
import com.kiki.common.packet.getBytePayloadOrNull
import com.kiki.common.packet.type.PacketType
import com.kiki.common.result.PayloadResult
import com.kiki.common.result.exception.PayloadException
import com.kiki.common.socket.queue.PacketQueue
import java.io.IOException
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

class ClientSocket(id : Int, outBoundQueue : PacketQueue, socket : Socket) : AbstractSocket(id, outBoundQueue, socket) {

    @Volatile
    private var isRunning: Boolean = false
    private lateinit var thread: Thread

    override fun run() {
        isRunning = true
        thread = readSocket() //내부 쓰레드
    }


    // 연결된 소켓에서 읽어서 - 서버로 전달
    private fun readSocket() : Thread {
        val runnable = Runnable {
            try {
                val array = ByteArray(10000)
                val reader = socket.getInputStream()
                var readByte = reader.read(array)
                while (readByte != -1 && isRunning) {
                    val readPacket = array.slice(IntRange(0, readByte - 1)).toByteArray()
                    sendPacket(Packet(id, PacketType.MESSAGE, readPacket))
                    readByte = reader.read(array)
                }
            }
            catch (e: Exception) {
                println("Read error - $id | ${e.message}")
                socket.close()
                sendPacket(Packet(id, PacketType.MESSAGE, PayloadResult.failure(PayloadException("Connection reset")))) //에러 핸들
            }
        }
        return Thread.startVirtualThread(runnable) //가상 쓰레드로 시작
    }

    //소켓 작동 중단
    fun stopSocket() {
        isRunning = false
        thread.interrupt() //쓰레드 중단
    }
}