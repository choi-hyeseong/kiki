package com.kiki.client

import com.kiki.common.packet.Packet
import com.kiki.common.packet.type.PacketType
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
            Thread.sleep(1000)
        }
    }

    override fun stopPool() {
        close()
    }

    override fun handlePacket(packet: Packet) {
        if (packet.packetType == PacketType.ACCEPT)
            addSocketToPool(Socket("127.0.0.1", port))
        else
            notifyPacket(packet)
    }

}