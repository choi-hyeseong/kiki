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

class ClientSocket(id: Int, outBoundQueue: PacketQueue, socket: Socket) : AbstractSocket(id, outBoundQueue, socket) {

    @Volatile
    private var isRunning: Boolean = false

    override fun run() {
        isRunning = true
        readSocket()
    }

    override fun handlePacket(packet: Packet) {
        // 핸들 불가능한경우 리턴
        if (!isHandleable(packet))
            return
        runCatching {
            val payload = packet.payload
            //payload 확인
            if (payload.isSuccess())
                socket.getOutputStream().write(payload.getBytePayloadOrNull()!!)
            else
                throw IllegalStateException("Encountered Tunneling Exception ${payload.exception?.message}")
        }.onFailure {
            // 소켓에서 오류 발생시
            socket.close()
            println("Socket Error Found - $id | ${it.javaClass.simpleName} ${it.message.toString()}")
            if (it !is IllegalStateException) //IS는 반대측에서 소켓 끊어졌다고 보내는 에러 메시지. 이게 아닌경우 에러 메시지 보내줘야함. 왜 위에는 IS로 했으면서 여기는 IA로 체크..
                sendPacket(Packet(id, PacketType.MESSAGE, PayloadResult.failure(PayloadException("Connection reset")))) //에러 핸들
        }


    }

    // 연결된 소켓에서 읽어서 - 서버로 전달
    private fun readSocket() {
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
            sendPacket(
                Packet(
                    id,
                    PacketType.MESSAGE,
                    PayloadResult.failure(PayloadException("Connection reset"))
                )
            ) //에러 핸들
        }
    }

    //소켓 작동 중단
    fun stopSocket() {
        isRunning = false
    }
}