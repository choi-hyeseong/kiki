package com.kiki.client

import java.net.Socket
import java.util.Scanner

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
    val scanner : Scanner = Scanner(System.`in`)
    print("터널링 할 포트를 입력해주세요 : ")
    // 예외처리 안됨. 포트 범위 bound 체크 안됨
    val int = scanner.nextInt()
    Client(int).startClient()
}
