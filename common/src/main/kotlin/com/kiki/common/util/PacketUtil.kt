package com.kiki.common.util

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kiki.common.packet.Packet
import com.kiki.common.packet.type.PacketType
import com.kiki.common.result.PayloadResult
import com.kiki.common.result.exception.PayloadException
import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.net.Socket

class PacketUtil {

    companion object {

        // json mapper
        private val mapper = jacksonObjectMapper()

        // Object - ByteArray Serialize
        fun <T> serialize(data : T) : ByteArray {
            return mapper.writeValueAsBytes(data)
        }

        /**
         * object Deserialize
         * @throws IllegalArgumentException 역직렬화에 실패했을때 발생합니다.
         */
        fun <T> deserialize(array : ByteArray, targetClass : Class<T>) : T {
            return runCatching {
                mapper.readValue(array, targetClass)
            }.getOrElse {
                println("${it.javaClass} ${it.message}")
                throw IllegalArgumentException("Can't Deserialize")
            }
        }
    }
}

// OutputStream에서 바로 Object 직렬화 가능하게 하는 확장함수
fun <T> OutputStream.writeObject(data : T) {
   writeString(PacketUtil.serialize(data).decodeToString())
}

// 문자열 출력하는 확장 함수
fun OutputStream.writeString(data: String) {
    val printWriter = PrintWriter(this)
    printWriter.println(data)
    printWriter.flush()
}

// BufferedReader에서 Blocking 방식으로 stream이 close될때까지 읽는 확장함수
fun BufferedReader.readUntilClose(onRead : (String) -> Unit, onError : (Throwable) -> Unit) {
    runCatching {
        var input = this.readLine()
        while (input != null) {
            onRead(input)
            input = this.readLine()
        }
    }.onFailure {
        onError(it)
    }
}

// InputStreadm에서 Blocking 방식으로 Stream 읽기
fun InputStream.readUntilClose(bufferSize : Int, onRead : (ByteArray) -> Unit, onError : (Throwable) -> Unit) {
    kotlin.runCatching {
        val buffer = ByteArray(bufferSize) //버퍼 생성
        var readByte = read(buffer)
        while (readByte != -1) {
            val readPart = buffer.slice(IntRange(0, readByte - 1)).toByteArray() //버퍼에서 읽은 부분
            onRead(readPart)
            readByte = read(buffer)
        }
    }.onFailure {
       onError(it)
    }
}