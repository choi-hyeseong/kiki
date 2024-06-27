package com.kiki.common.socket

import com.kiki.common.packet.Packet
import com.kiki.common.packet.getBytePayloadOrNull
import com.kiki.common.packet.type.PacketType
import com.kiki.common.result.PayloadResult
import com.kiki.common.result.exception.PayloadException
import com.kiki.common.socket.queue.PacketQueue
import com.kiki.common.util.writeObject
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * end 단에서 처리할 소켓 추상 클래스
 * @property id 각 소켓의 고유한 id입니다.
 * @property outBoundQueue 서버에 정보를 전달하기 위한 패킷을 담을 큐입니다.
 * @property socket 현재 연결된 소켓입니다. (끝단)
 */
abstract class AbstractSocket(val id: Int, private val outBoundQueue : PacketQueue, protected val socket: Socket) :
    Runnable {

    /**
     * 처리 가능한 패킷인지 확인
     */
    private fun isHandleable(packet: Packet): Boolean {
        return packet.id == id
    }

    /**
     * 패킷 핸들링하기. 만약 자신의 패킷인경우 전송
     */
    fun handlePacket(packet: Packet) {
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

    /**
     * 연결된 서버로 패킷 보내는 큐에 패킷 담기
     */
    fun sendPacket(packet: Packet) {
        // 큐 담기
        outBoundQueue.addPacket(packet)
    }
}
