package com.kiki.common.socket

import com.kiki.common.packet.Packet
import com.kiki.common.socket.queue.PacketQueue
import java.net.Socket

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
     * 서버 - 클라이언트 소켓에게 처리 가능한 패킷인지 질의하는 메소드. 다루기 가능하면 handlePacket 호출
     */
    fun notifyPacket(packet: Packet) {
        if (isHandleable(packet))
            handlePacket(packet)
    }

    /**
     * 패킷 핸들링하기.
     */
    protected abstract fun handlePacket(packet: Packet)



    /**
     * 연결된 서버로 패킷 보내는 큐에 패킷 담기
     */
    fun sendPacketToServer(packet: Packet) {
        // 큐 담기
        outBoundQueue.addPacket(packet)
    }
}
