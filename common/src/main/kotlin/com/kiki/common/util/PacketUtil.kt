package com.kiki.common.util

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
