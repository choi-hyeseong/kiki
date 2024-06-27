package com.kiki.client

import java.net.Socket

/**
 * 터널링 될 서버 (클라이언트)
 * @property port 포워딩 될 포트입니다.
 */
class Client(val port : Int) {

    private lateinit var clientPool : ClientPool

    fun startClient() {
        val socket = Socket("127.0.0.1", 6960)
        val pool = ClientPool(port, socket)
        pool.startPool()
    }

    fun stopClient() {
        clientPool.stopPool()
    }


}

fun main() {
    Client(25565).startClient()
}
