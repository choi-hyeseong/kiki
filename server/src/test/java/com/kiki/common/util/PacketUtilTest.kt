package com.kiki.common.util

import com.kiki.common.packet.Packet
import com.kiki.common.packet.type.PacketType
import com.kiki.common.result.PayloadResult
import com.kiki.common.result.exception.PayloadException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class PacketUtilTest {

    @Test
    fun successNormalPacketSerialize() {
        // 직렬화 성공
        val packet = Packet(1, PacketType.MESSAGE, PayloadResult.success("success"))
        val serializedString = assertDoesNotThrow {
            PacketUtil.serialize(packet)
        }
        assertNotNull(serializedString)
    }

    @Test
    fun successExceptionPacketSerialize() {
        // 직렬화 성공
        val packet = Packet(1, PacketType.MESSAGE, PayloadResult.failure(PayloadException("실패")))
        val serializedString = assertDoesNotThrow {
            PacketUtil.serialize(packet)
        }
        assertNotNull(serializedString)
    }

    @Test
    fun successNormalPacketDeserialize() {
        // 일반 패킷 역직렬화 성공
        val packet = Packet(1, PacketType.MESSAGE, PayloadResult.success("success"))
        assertDoesNotThrow {
            val str = PacketUtil.serialize(packet)
            val deserialize = PacketUtil.deserialize(str, Packet::class.java)
            assertEquals(deserialize.id, packet.id)
            assertEquals(deserialize.packetType, packet.packetType)
            assertEquals(deserialize.payload, packet.payload)
        }
    }

    @Test
    fun successExceptionPacketDeserialize() {
        // 예외 패킷 역직렬화 성공
        val packet = Packet(1, PacketType.MESSAGE, PayloadResult.failure(PayloadException("실패")))
        assertDoesNotThrow {
            val data = PacketUtil.serialize(packet)
            val deserialize = PacketUtil.deserialize(data, Packet::class.java)
            assertEquals(deserialize.id, packet.id)
            assertEquals(deserialize.packetType, packet.packetType)
            assertEquals(deserialize.payload.isSuccess(), packet.payload.isSuccess())
            assertEquals("실패", deserialize.payload.exceptionOrNull()?.message)
        }
    }
}