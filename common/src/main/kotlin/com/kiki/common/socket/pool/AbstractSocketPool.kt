package com.kiki.common.socket.pool

import com.kiki.common.packet.Packet
import com.kiki.common.socket.ClientSocket
import com.kiki.common.socket.queue.PacketQueue
import com.kiki.common.util.PacketUtil
import com.kiki.common.util.readUntilClose
import java.net.Socket
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * 소켓을 관리하는 추상 소켓풀
 * @property socket 서버와 연결된 소켓
 * @property atomicInteger addSocketToPool에 사용되며, 쓰레드 안전하게 각 소켓별로 id 부여
 * @property packetQueue 서버로 전송되는 패킷을 담는 큐. 쓰레드 안전하게 전송하기 위함
 * @property socketPool 연결된 소켓이 담겨있는 풀(큐)
 * @property threadPoolExecutor 쓰레드풀
 */
abstract class AbstractSocketPool(val socket: Socket) {

    @Volatile
    protected var isRunning: Boolean = false
    private val atomicInteger: AtomicInteger = AtomicInteger(1) //소켓 id 부여하는 atomic Integer
    protected val packetQueue: PacketQueue = PacketQueue(socket) //전송될 패킷이 대기하는 큐. (등록시 패킷 전송 됨)
    private val socketPool: Queue<ClientSocket> = ConcurrentLinkedQueue() //연결된 소켓 담는 큐
    private val threadPoolExecutor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor() //쓰레드 풀


    // 해당 풀 시작하는 메소드 - init 호출 필요
    abstract fun startPool()

    // 해당 풀 종료하는 메소드 - close 호출 필요
    abstract fun stopPool()

    // 패킷 전송, 수신 관리 init 메소드
    fun init() {
        isRunning = true
        startPacketRead()
        startPacketWrite()
    }

    fun close() {
        isRunning = false
        socketPool.forEach { it.stopSocket() } //소켓 중단
        packetQueue.stopHandle() //패킷 전송 중단
        threadPoolExecutor.shutdown() //쓰레드풀 중단
    }

    /**
     * 소켓풀에 소켓을 추가하는데, 내부 카운터를 이용해 쓰레드 안전하게 추가합니다.
     * @param clientSocket 추가할 소켓입니다.
     */
    fun addSocketToPool(clientSocket: Socket): ClientSocket {
        return addSocketToPoolWithId(atomicInteger.getAndIncrement(), clientSocket)
    }


    /**
     * 소켓 풀에 소켓 추가, id값을 직접 지정 가능한 메소드. 주의! addSocketToPool 메소드의 내부 카운터와 동기화 되지 않습니다. (3으로 WithId 호출후 addSocketToPool했을때 4 들어가는거 X)
     * @param id 해당 소켓의 id를 지정할 수 있습니다. 이미 지정된 소켓의 id인경우 문제가 발생할 수 있습니다.
     * @param clientSocket 추가할 소켓의 id입니다.
     * @throws IllegalArgumentException 이미 풀에 등록된 소켓의 id를 지정할경우 발생합니다.
     */
    protected fun addSocketToPoolWithId(id: Int, clientSocket: Socket): ClientSocket {
        if (socketPool.find { it.id == id } != null)
            throw IllegalStateException("이미 존재하는 소켓의 id입니다.")
        val client = ClientSocket(id, packetQueue, clientSocket)
        socketPool.add(client)
        threadPoolExecutor.submit(client)
        println("Added Socket - $id")
        return client
    }


    //서버로부터 패킷 읽어오는 작업 요청
    private fun startPacketRead() {
        threadPoolExecutor.submit(ServerPacketReader(socket, onPacket = { handlePacket(it) }, onError = { stopPool() }))
        // 패킷 읽기 성공시 handlePacket, 실패시 stopPool 호출.
    }

    //큐에 있는 패킷 처리 시작
    private fun startPacketWrite() {
        packetQueue.startHandle (onError = { stopPool() }) //에러시 풀 중단
    }

    /**
     * 입력받은 패킷 어떻게 핸들링할지 정하기
     */
    protected abstract fun handlePacket(packet: Packet)

    // 풀 내에 있는 소켓들에게 패킷 notify
    protected fun notifyPacket(packet: Packet) {
        socketPool.stream().forEach { it.notifyPacket(packet) } //stream은 thread-safe (CLQ에 spliterator 지원)
    }

    /**
     * 서버의 패킷을 읽는 클래스.
     * @property socket 서버와 연결된 소켓입니다.
     * @property onPacket 패킷을 읽었을때 처리할 행동 (람다)입니다.
     * @property onError 패킷을 읽는데 실패했을때 처리할 행동입니다.
     */
    class ServerPacketReader(
        private val socket: Socket,
        private val onPacket: (Packet) -> Unit,
        private val onError: (Throwable) -> Unit
    ) : Runnable {
        override fun run() {
            val bufferedReader = socket.getInputStream().bufferedReader()
            bufferedReader.readUntilClose(onRead = {
                kotlin.runCatching {
                    val packet = PacketUtil.deserialize(it.toByteArray(), Packet::class.java)
                    onPacket(packet)
                }.onFailure(onError) // packet deserialize 실패시에도 동일하게 처리
            }, onError = {
                println("Can't read packet - ${it.message}")
                onError(it)
            })
        }
    }
}




