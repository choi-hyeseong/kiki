package com.kiki.client

import java.net.Socket
import java.util.Scanner

/**
 * 터널링 될 서버 (클라이언트)
 * @property port 포워딩 될 포트입니다.
 */
class Client(val ip : String, val port : Int) {

    private lateinit var clientPool : ClientPool

    fun startClient() {
        val socket = Socket(ip, 6960)
        val pool = ClientPool(port, socket)
        pool.startPool()
    }

    fun stopClient() {
        clientPool.stopPool()
    }


}

fun main() {
    val scanner : Scanner = Scanner(System.`in`)
    print("서버의 ip를 입력해주세요 : ")
    val ip = scanner.next()
    print("터널링 할 포트를 입력해주세요 : ")
    // 예외처리 안됨. 포트 범위 bound 체크 안됨
    val port = scanner.nextInt()
    Client(ip, port).startClient()
}
