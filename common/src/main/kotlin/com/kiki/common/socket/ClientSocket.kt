package com.kiki.common.socket

import com.kiki.common.packet.Packet
import com.kiki.common.packet.getBytePayloadOrNull
import com.kiki.common.packet.type.PacketType
import com.kiki.common.result.PayloadResult
import com.kiki.common.result.exception.PayloadException
import com.kiki.common.socket.queue.PacketQueue
import com.kiki.common.util.readUntilClose
import java.net.Socket

class ClientSocket(id: Int, outBoundQueue: PacketQueue, socket: Socket) : AbstractSocket(id, outBoundQueue, socket) {


    override fun run() {
        readPacket()
    }

    //소켓 작동 중단
    fun stopSocket() {
        socket.close()
    }

    // 서버에서 패킷을 받은경우.
    override fun handlePacket(packet: Packet) {
        runCatching {
            val payload = packet.payload
            //payload 확인
            if (!payload.isSuccess()) //에러가 있는경우 throw
                throw IllegalStateException("Encountered Tunneling Exception ${payload.exception?.message}")
            writePacket(payload)
        }.onFailure {
            // 소켓에서 오류 발생시
            if (it !is IllegalStateException)
                //IS는 반대측에서 소켓 끊어졌다고 보내는 에러 메시지. 이게 아닌경우 에러 메시지 보내줘야함. 왜 위에는 IS로 했으면서 여기는 IA로 체크..
                sendPacketToServer(Packet(id, PacketType.MESSAGE, PayloadResult.failure(PayloadException("Connection reset")))) //에러 핸들
            println("Socket Error Found - $id | ${it.javaClass.simpleName} ${it.message.toString()}")
            stopSocket()
        }


    }

    // 연결된 소켓에서 읽어서 - 서버로 전달
    private fun readPacket() {
        socket.getInputStream().readUntilClose(10000, onRead = {
            sendPacketToServer(Packet(id, PacketType.MESSAGE, it))
        }, onError = {
            println("Read error - $id | ${it.message}")
            sendPacketToServer(Packet(id, PacketType.MESSAGE, PayloadResult.failure(PayloadException("Connection reset"))))
            stopSocket()
        })
    }

    // 연결된 소켓의 데이터 -> 클라이언트 소켓으로 내보내기.
    private fun writePacket(payload: PayloadResult<String>) {
        val bytes = payload.getBytePayloadOrNull()
        // notnull이긴 하지만 혹시나 모르는 null check
        if (bytes == null)
            println("Payload is empty. $id")
        else
            socket.getOutputStream().write(bytes)
    }

}