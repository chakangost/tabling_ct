package com.tabling.homework.api

import android.util.Log
import java.io.*
import java.lang.reflect.Field
import java.net.HttpCookie
import kotlin.experimental.and

class SerializableHttpCookie : Serializable {

    @Transient
    private var cookie: HttpCookie? = null
    // Workaround httpOnly: The httpOnly attribute is not accessible so when we
    // serialize and deserialize the cookie it not preserve the same value. We
    // need to access it using reflection
    private var fieldHttpOnly: Field? = null

    fun encode(cookie: HttpCookie?): String? {
        this.cookie = cookie
        val os = ByteArrayOutputStream()
        try {
            val outputStream = ObjectOutputStream(os)
            outputStream.writeObject(this)
        } catch (e: IOException) {
            Log.d(
                TAG,
                "IOException in encodeCookie",
                e
            )
            return null
        }
        return byteArrayToHexString(os.toByteArray())
    }

    fun decode(encodedCookie: String): HttpCookie? {
        var encodedCookie = encodedCookie
        val classNameBytes = this.javaClass.name.toByteArray()
        val aaa = ByteArray(classNameBytes.size + 1)
        aaa[0] = classNameBytes.size.toByte()
        System.arraycopy(classNameBytes, 0, aaa, 1, classNameBytes.size)
        encodedCookie = encodedCookie.replace(
            "3c6b722e7a65726f7765622e666c6965725f637573746f6d65725f616e64726f69642e6170692e53657269616c697a61626c6548747470436f6f6b6965".toRegex(),
            byteArrayToHexString(aaa)
        )
        val bytes = hexStringToByteArray(encodedCookie)
        val byteArrayInputStream = ByteArrayInputStream(bytes)
        var cookie: HttpCookie? = null
        try {
            val objectInputStream =
                ObjectInputStream(byteArrayInputStream)
            cookie = (objectInputStream.readObject() as SerializableHttpCookie).cookie
        } catch (e: IOException) {
            Log.d(
                TAG,
                "IOException in decodeCookie",
                e
            )
        } catch (e: ClassNotFoundException) {
            Log.d(
                TAG,
                "ClassNotFoundException in decodeCookie",
                e
            )
        }
        return cookie
    }

    private var httpOnly: Boolean
        get() {
            try {
                initFieldHttpOnly()
                return fieldHttpOnly!![cookie] as Boolean
            } catch (e: Exception) {
                Log.w(TAG, e)
            }
            return false
        }
        private set(httpOnly) {
            try {
                initFieldHttpOnly()
                fieldHttpOnly!![cookie] = httpOnly
            } catch (e: Exception) {
                Log.w(TAG, e)
            }
        }

    @Throws(NoSuchFieldException::class)
    private fun initFieldHttpOnly() {
        fieldHttpOnly = cookie!!.javaClass.getDeclaredField("httpOnly")
        fieldHttpOnly!!.isAccessible = true
    }

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        out.writeObject(cookie!!.name)
        out.writeObject(cookie!!.value)
        out.writeObject(cookie!!.comment)
        out.writeObject(cookie!!.commentURL)
        out.writeObject(cookie!!.domain)
        out.writeLong(cookie!!.maxAge)
        out.writeObject(cookie!!.path)
        out.writeObject(cookie!!.portlist)
        out.writeInt(cookie!!.version)
        out.writeBoolean(cookie!!.secure)
        out.writeBoolean(cookie!!.discard)
        out.writeBoolean(httpOnly)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        val name = `in`.readObject() as String
        val value = `in`.readObject() as String
        cookie = HttpCookie(name, value)
        cookie!!.comment = `in`.readObject() as String
        cookie!!.commentURL = `in`.readObject() as String
        cookie!!.domain = `in`.readObject() as String
        cookie!!.maxAge = `in`.readLong()
        cookie!!.path = `in`.readObject() as String
        cookie!!.portlist = `in`.readObject() as String
        cookie!!.version = `in`.readInt()
        cookie!!.secure = `in`.readBoolean()
        cookie!!.discard = `in`.readBoolean()
        httpOnly = `in`.readBoolean()
    }

    /**
     * Using some super basic byte array &lt;-&gt; hex conversions so we don't
     * have to rely on any large Base64 libraries. Can be overridden if you
     * like!
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
     */
    private fun byteArrayToHexString(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (element in bytes) {
            val v: Byte = element and 0xff.toByte()
            if (v < 16) {
                sb.append('0')
            }
            sb.append(Integer.toHexString(v.toInt()))
        }
        return sb.toString()
    }

    /**
     * Converts hex values from strings to byte array
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len-1) {
            data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character
                    .digit(hexString[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    companion object {
        private val TAG = SerializableHttpCookie::class.java
            .simpleName
        private const val serialVersionUID = 6374381323722046732L
    }
}