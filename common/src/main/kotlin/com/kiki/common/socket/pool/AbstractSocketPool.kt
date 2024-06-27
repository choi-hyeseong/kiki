package com.kiki.common.socket.pool

import com.kiki.common.packet.Packet
import com.kiki.common.socket.ClientSocket
import com.kiki.common.util.PacketUtil
import java.net.Socket
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * 소켓을 관리하는 추상 소켓풀
 * @property socket 서버와 연결된 소켓
 */
abstract class AbstractSocketPool(val socket: Socket) {

    @Volatile
    protected var isRunning: Boolean = false
    private val atomicInteger : AtomicInteger = AtomicInteger(1)
    private val socketPool: Queue<ClientSocket> = ConcurrentLinkedQueue()
    private val threadPoolExecutor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()


    // 해당 풀 시작하는 메소드 - init 호출 필요
    abstract fun startPool()

    // 해당 풀 종료하는 메소드 - close 호출 필요
    abstract fun stopPool()

    // 패킷 전송, 수신 관리 init 메소드
    fun init() {
        isRunning = true
        requestPacketRead()
    }

    fun close() {
        isRunning = false
        threadPoolExecutor.shutdown()
        socketPool.forEach { it.stopSocket() }
    }

    // 소켓 풀에 추가. 추가된 소켓 객체 반환. 내부 id 사용
    fun addSocketToPool(clientSocket: Socket) : ClientSocket {
        return addSocketToPoolWithId(atomicInteger.getAndIncrement(), clientSocket)
    }

    // 소켓 풀에 추가. 추가된 소켓 객체 반환, id 직접 지정가능. 내부 카운터값 변하지 않음.
    protected fun addSocketToPoolWithId(id : Int, clientSocket: Socket) : ClientSocket {
        val client = ClientSocket(id, socket, clientSocket)
        socketPool.add(client)
        threadPoolExecutor.submit(client)
        println("Added Socket - $id")
        return client
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
                stopPool() //소켓 연결 자체가 끊겼으므로 중단
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




