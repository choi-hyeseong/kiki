package com.kiki.common.socket

import com.kiki.common.packet.Packet
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * end 단에서 처리할 소켓 추상 클래스
 * @property id 각 소켓의 고유한 id입니다.
 * @property outboundQueue 서버에 전송될 패킷을 담을 큐입니다. 해당 큐로 패킷을 넣을경우 큐가 처리되며 패킷이 전달됩니다. 외부 socket에 대한 의존성 X
 * @property socket 현재 연결된 소켓입니다. (끝단)
 */
abstract class AbstractSocket(val id : Int, private val outboundQueue : Queue<Packet>, protected val socket : Socket) : Runnable {

    // 내부에서 처리해야할 큐 입니다.
    protected val inboundQueue : Queue<Packet> = ConcurrentLinkedQueue()
    /**
     * 처리 가능한 패킷인지 확인
     */
    private fun isHandleable(packet: Packet) : Boolean {
        return packet.id == id
    }

    /**
     * 패킷 핸들링하기. 만약 자신의 패킷인경우 큐에 추가.
     */
    fun handlePacket(packet : Packet) {
        if (isHandleable(packet))
            inboundQueue.add(packet)
    }

    /**
     * 연결된 서버로 패킷 보내는 큐
     */
    fun sendPacket(packet : Packet) {
        outboundQueue.add(packet) //터널링 클라이언트로 전달하는 큐에 패킷 등록
    }

}