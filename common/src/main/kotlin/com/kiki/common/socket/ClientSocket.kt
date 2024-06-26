package com.kiki.common.socket

import com.kiki.common.packet.Packet
import com.kiki.common.packet.getBytePayloadOrNull
import com.kiki.common.packet.type.PacketType
import com.kiki.common.result.PayloadResult
import com.kiki.common.result.exception.PayloadException
import java.io.IOException
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

class ClientSocket(id : Int, outboundQueue : Queue<Packet>, socket : Socket) : AbstractSocket(id, outboundQueue, socket) {

    @Volatile
    private var isRunning: Boolean = false

    override fun run() {
        isRunning = true
        readSocket() //내부 쓰레드
        writeSocket() //블라킹
    }

    // 서버에서 읽은 데이터(패킷)을 - 연결된 소켓으로 전달
    private fun writeSocket() {
        while (isRunning) {
            runCatching {
                val packet: Packet? = inboundQueue.poll()
                if (packet != null) {
                    val payload = packet.payload
                    //payload 확인
                    if (payload.isSuccess())
                        socket.getOutputStream().write(payload.getBytePayloadOrNull()!!)
                    else {
                        println("Encountered Tunneling Exception ${payload.exceptionOrNull()}")
                        socket.close()
                    }
                }
            }.onFailure {
                // 소켓에서 오류 발생시
                socket.close()
                it.printStackTrace()
                println("Socket Error Found - $id | ${it.message.toString()}")
                sendPacket(Packet(id, PacketType.MESSAGE, PayloadResult.failure(PayloadException("Connection reset")))) //에러 핸들
                return
            }
        }
    }

    // 연결된 소켓에서 읽어서 - 서버로 전달
    private fun readSocket() {
        thread {
            try {
                val array = ByteArray(10000)
                val reader = socket.getInputStream()
                var readByte = reader.read(array)
                while (readByte != -1 && isRunning) {
                    val readPacket = array.slice(IntRange(0, readByte - 1)).toByteArray()
                    sendPacket(Packet(id, PacketType.MESSAGE, readPacket))
                    readByte = reader.read(array)
                }
            } catch (e: Exception) {
                println("Read error - $id | ${e.message}")
                socket.close()
                sendPacket(Packet(id, PacketType.MESSAGE, PayloadResult.failure(PayloadException("Connection reset")))) //에러 핸들
            }
        }
    }

    //소켓 작동 중단
    fun stopSocket() {
        isRunning = false
    }
}