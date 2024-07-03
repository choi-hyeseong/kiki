package com.kiki.client

import com.kiki.common.packet.Packet
import com.kiki.common.packet.type.PacketType
import com.kiki.common.result.PayloadResult
import com.kiki.common.result.exception.PayloadException
import com.kiki.common.socket.ClientSocket
import com.kiki.common.socket.pool.AbstractSocketPool
import com.kiki.common.util.writeObject
import java.net.Socket
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ClientPool(private val port : Int, socket: Socket) : AbstractSocketPool(socket) {

    override fun startPool() {
        socket.getInputStream().bufferedReader().readLine().also { println(it) } //url print 하기 위해 맨처음 listen
        init()
        while (isRunning) {
            // server pool은 socket accept가 있어 blocking이 되지만 client는 그런게 없으므로 while loop sleep 하기
            Thread.sleep(1000)
        }
    }

    override fun stopPool() {
        close()
    }

    override fun handlePacket(packet: Packet) {
        if (packet.packetType == PacketType.ACCEPT) {
            kotlin.runCatching { addSocketToPoolWithId(packet.id, Socket("127.0.0.1", port)) }
                .onFailure { packetQueue.addPacket(Packet(packet.id, PacketType.MESSAGE, PayloadResult.failure(PayloadException("Can't connect")))) } //접속 실패시 error 발생 알림
        }
        else
            notifyPacket(packet)
    }

}