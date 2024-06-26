package com.kiki.common.packet

import com.kiki.common.packet.type.PacketType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeoutException

class PacketTest {


    @Test
    fun testAcceptPacket() {
        //accept로 된 패킷 테스트
        val packet = Packet(1, PacketType.ACCEPT, Result.success(""))
        assertEquals(1, packet.id)
        assertEquals(PacketType.ACCEPT, packet.packetType)
        assertTrue(packet.payload.isSuccess)
    }

    @Test
    fun testPacketException() {
        //exception 전달 테스트
        val packet = Packet(1, PacketType.MESSAGE, Result.failure(TimeoutException("타임아웃")))
        assertTrue(packet.payload.isFailure)
        val exception = packet.payload.exceptionOrNull()
        assertNotNull(exception)
        assertEquals("타임아웃", exception?.message)
    }
}