package com.kiki.common.packet

import com.kiki.common.packet.type.PacketType
import com.kiki.common.result.PayloadResult
import java.util.Base64

/**
 * Client - Server에서 처리하는 패킷 클래스
 * @property id 연결된 소켓의 id. packetType이 ACCEPT일경우 해당 id로 된 소켓 연결을 생성함.
 * @property packetType 패킷의 타입 (최초 연결여부, 메시지 구분)
 * @property payload 패킷의 데이터. 만약 exception이 발생했을경우 exception이 포함될 수 있음. Base64 인코딩
 */
class Packet(
    val id : Int,
    val packetType: PacketType,
    val payload : PayloadResult<String>
) {
    constructor(id : Int, packetType: PacketType, byteArray: ByteArray) : this(id, packetType, PayloadResult.success(Base64.getEncoder().encodeToString(byteArray)))
}

// bytearray로 변환된 payload 얻거나 null반환
fun PayloadResult<String>.getBytePayloadOrNull() : ByteArray? {
    return this.getOrNull()?.let {
        Base64.getDecoder().decode(it)
    }
}