package com.kiki.common.socket.pool

import com.kiki.common.packet.Packet
import com.kiki.common.socket.ClientSocket
import com.kiki.common.util.PacketUtil
import com.kiki.common.util.writeObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 소켓을 관리하는 추상 소켓풀
 * @property socket 서버와 연결된 소켓
 */
abstract class AbstractSocketPool(val socket: Socket) {
    // TODO Virtual Thread써서 한번 풀 최적화 되는지 확인해보기

    @Volatile
    protected var isRunning: Boolean = false
    private val atomicInteger : AtomicInteger = AtomicInteger()
    private val packetQueue: Queue<Packet> = ConcurrentLinkedQueue()
    private val socketPool: Queue<ClientSocket> = ConcurrentLinkedQueue()
    private val threadPoolExecutor: ThreadPoolExecutor =
        ThreadPoolExecutor(6, 200, 10, TimeUnit.MINUTES, SynchronousQueue())


    // 해당 풀 시작하는 메소드 - init 호출 필요
    abstract fun startClient()

    // 해당 풀 종료하는 메소드 - close 호출 필요
    abstract fun stopClient()

    // 패킷 전송, 수신 관리 init 메소드
    fun init() {
        isRunning = true
        requestPacketWrite()
        requestPacketRead()
    }

    fun close() {
        isRunning = false
        threadPoolExecutor.shutdown()
        socketPool.forEach { it.stopSocket() }
    }

    // 소켓 풀에 추가. 추가된 소켓 객체 반환
    fun addSocketToPool(socket: Socket) : ClientSocket {
        val clientSocket = ClientSocket(atomicInteger.getAndIncrement(), packetQueue, socket)
        socketPool.add(clientSocket)
        threadPoolExecutor.submit(clientSocket)
        return clientSocket
    }

    // 서버로 패킷 전달하는 큐 작업 요청
    private fun requestPacketWrite() {
        val runnable = Runnable {
            while (isRunning) {
                runCatching {
                    // 전송할 패킷 큐에 패킷이 있는경우
                    val availablePacket = packetQueue.poll()
                    if (availablePacket != null)
                        socket.getOutputStream().writeObject(availablePacket)
                    Thread.sleep(10) //계속 할당하지 않게 슬립
                }.onFailure {
                    println("Can't send packet - ${it.message}")
                    // IOException인경우 연결 실패인경우이므로 connection 끊음
                    stopClient() //소켓 연결 자체가 끊겼으므로 중단
                }
            }
        }
        threadPoolExecutor.submit(runnable)
    }

    //서버로부터 패킷 읽어오는 작업 요청
    private fun requestPacketRead() {
        //터널링 서버 - 서버 연결하는 스트림 읽는 메소드. 서버와 서버의 연결 (json형식이기 때문에 buffered reader 써서 string으로 읽어도 안전)
        val runnable = Runnable {
            runCatching {
                val reader = socket.getInputStream().bufferedReader()
                var input = reader.readLine()
                while (input != null && isRunning) {
                    val packet = PacketUtil.deserialize(input.toByteArray(), Packet::class.java)
                    handlePacket(packet)
                    input = reader.readLine()
                }
            }.onFailure {
                println("Can't read packet - ${it.message}")
                stopClient() //소켓 연결 자체가 끊겼으므로 중단
            }
        }
        threadPoolExecutor.submit(runnable)
    }

    /**
     * 입력받은 패킷 어떻게 핸들링할지 정하기
     */
    protected abstract fun handlePacket(packet : Packet)

    // 풀 내에 있는 소켓들에게 패킷 notify
    protected fun notifyPacket(packet: Packet) {
        socketPool.forEach { it.handlePacket(packet) }
    }
}




