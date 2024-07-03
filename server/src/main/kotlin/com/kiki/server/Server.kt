package com.kiki.server

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.*

/**
 * Tunneling Client - 'Server' - Client
 * 패킷 다루는게 아닌 터널링 클라이언트 - 서버 N:1 처리를 위한 풀이므로 일단 Abstract Pool 미사용
 */
class Server {

    // 작동 여부 변수
    @Volatile //메모리에서 접근하게. isRunning 변수는 캐시로 들어가면 문제될 수 있음.
    private var isRunning : Boolean = false
    // 작동할 포트, NGC6960 :)
    private val port : Int = 6960
    // 동작중인 클라이언트 서버
    private val serverList : MutableList<SocketServer> = mutableListOf()
    private val threadPoolExecutor : ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
    private lateinit var serverSocket : ServerSocket

    /**
     * 서버 작동 함수
     * @throws IllegalStateException 이미 작동중인데 실행시키려 하면 발생합니다.
     */
    fun startServer() {
        if (isRunning)
            throw IllegalStateException("이미 동작중 입니다.")
        // 서버 소켓 생성
        println("Server is Running..")
        isRunning = true
        serverSocket = ServerSocket(port)
        acceptSocket(serverSocket)
    }

    /**
     * 서버를 중단합니다.
     */
    fun stopServer() {
        isRunning = false
        serverList.forEach {
            it.stopPool() //서버 중단
        }
        threadPoolExecutor.shutdown()
        serverSocket.close()
    }

    private fun acceptSocket(serverSocket: ServerSocket) {
        while (isRunning) {
            // 터널링 클라이언트 연결된 경우
            val socket = serverSocket.accept()
            val socketServer = SocketServer(socket)
            serverList.add(socketServer) //서버맵에 추가
            threadPoolExecutor.submit(socketServer) //작동되게 추가
            println("Client Connected")
        }
        stopServer()
    }

}

fun main() {
    Server().startServer()
}