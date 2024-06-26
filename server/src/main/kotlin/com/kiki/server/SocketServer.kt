package com.kiki.server

import com.kiki.common.packet.Packet
import com.kiki.common.packet.type.PacketType
import com.kiki.common.result.PayloadResult
import com.kiki.common.socket.ClientSocket
import com.kiki.common.socket.pool.AbstractSocketPool
import com.kiki.common.util.PacketUtil
import com.kiki.common.util.writeObject
import com.kiki.common.util.writeString
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 클라이언트와 연결하기 위한 서버
 * @property socket 터널링 서버와 연결된 소켓 커넥션입니다.
 */
class SocketServer(socket : Socket) : AbstractSocketPool(socket), Runnable {

    private lateinit var serverSocket : ServerSocket

    override fun startClient() {
        init()
        serverSocket = ServerSocket(50000) //랜덤포트로 생성 (0으로 지정해야 랜덤!!)
        socket.getOutputStream().writeString("Server Opened - ${serverSocket.inetAddress.hostAddress}:${serverSocket.localPort}")
        acceptSocket(serverSocket) //무한루프
    }

    private fun acceptSocket(serverSocket: ServerSocket) {
        while (isRunning) {
            val connectedSocket = serverSocket.accept() //Client - Server와 다르게 얘는 실제 클라이언트가 연결되므로 accept 수행해야함.
            val clientSocket = addSocketToPool(connectedSocket)
            socket.getOutputStream().writeObject(Packet(clientSocket.id, PacketType.ACCEPT, PayloadResult.success(""))) //소켓 연결됬다고 보내기
        }
    }

    override fun stopClient() {
        close()
        serverSocket.close()
    }


    override fun handlePacket(packet: Packet) {
        notifyPacket(packet)
    }

    override fun run() {
        startClient()
    }


}