package com.kiki.client

import com.kiki.common.packet.Packet
import com.kiki.common.packet.type.PacketType
import com.kiki.common.util.PacketUtil
import com.kiki.common.util.writeObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.util.Base64
import kotlin.concurrent.thread

/**
 * 터널링 될 서버 (클라이언트)
 * @property port 포워딩 될 포트입니다.
 */
class Client(val port : Int) {

    private lateinit var clientPool : ClientPool

    fun startClient() {
        val socket = Socket("127.0.0.1", 6960)
        val pool = ClientPool(port, socket)
        pool.startClient()
    }

    fun stopClient() {
        clientPool.stopClient()
    }


}

fun main() {
    Client(25565).startClient()
}
