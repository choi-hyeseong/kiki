package com.kiki.common.socket.queue

import com.kiki.common.packet.Packet
import com.kiki.common.socket.ClientSocket
import com.kiki.common.util.writeObject
import java.net.Socket
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * 패킷 write시 안전하게 처리하기 위한 큐
 * read - handlePacket은 각 소켓마다 생성해야 하기 때문에 성능이 낮아짐 - 싱글 쓰레드 기반 처리 
 * @property socket 패킷이 전송될 소켓입니다.
 */
class PacketQueue(private val socket: Socket) {
    
    @Volatile
    private var isRunning = false //작동여부 boolean
    private var thread : Thread? = null // 패킷 처리할 쓰레드 (virtual)
    private val queue : BlockingQueue<Packet> = LinkedBlockingQueue()
    
    fun addPacket(packet: Packet) {
        queue.add(packet)
    }
    
    fun startHandle(onError : (throwable : Throwable) -> Unit) {
        isRunning = true
        val runnable = Runnable {
            try {
                while (isRunning) {
                    kotlin.runCatching {
                        val packet: Packet = queue.take() //blocking queue이므로 안전하게 대기
                        socket.getOutputStream().writeObject(packet)
                    }.onFailure {
                        println("Can't write packet - ${it.message}")
                        onError(it)
                        return@Runnable
                    }
                }
            }
            catch (e : InterruptedException) {
                return@Runnable
            }
        }
        thread = Thread.startVirtualThread(runnable)
    }
    
    fun stopHandle() {
        isRunning = false
        thread?.interrupt()
    }
    
    
}