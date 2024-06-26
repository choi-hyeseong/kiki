package com.kiki.common.packet.type

/**
 * 패킷의 타입 구분하기 위한 enum.
 * @property ACCEPT 클라이언트 - 서버 연결 과정에서 socket이 accpet 되어 연결됨을 알려주기 위한 enum
 * @property MESSAGE 일반 패킷 메시지
 */
enum class PacketType {
    ACCEPT, MESSAGE
}