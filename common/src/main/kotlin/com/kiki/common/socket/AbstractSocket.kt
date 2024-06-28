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
    protected fun isHandleable(packet: Packet): Boolean {
        return packet.id == id
    }

    /**
     * 패킷 핸들링하기. 만약 자신의 패킷인경우 전송
     */
    abstract fun handlePacket(packet: Packet)

    /**
     * 연결된 서버로 패킷 보내는 큐에 패킷 담기
     */
    fun sendPacket(packet: Packet) {
        // 큐 담기
        outBoundQueue.addPacket(packet)
    }
}
