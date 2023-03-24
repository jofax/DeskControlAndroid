package com.smartpods.android.pulseecho.Utilities

import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.*
import javax.crypto.*


class SPPulseEncryption(val aesKey: ByteArray,
                        val aesIV: ByteArray,
                        val packet: UByteArray) {

    /*
    * ENCRYPT
    * */
    fun encryptPacketWithCBC(): ByteArray {
        val iv = IvParameterSpec(aesIV)
        val keySpec = SecretKeySpec(aesKey, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
        return cipher.doFinal(packet.toByteArray())
    }

    fun encryptCBC(): ByteArray {
        val iv = IvParameterSpec(aesIV)
        val keySpec = SecretKeySpec(aesKey, "AES")

        println("encryptCBC iVSpec : ${iv}")
        println("encryptCBC keySpec : ${keySpec}")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
        return cipher.doFinal(packet.toByteArray())
    }

    fun decryptCBC(): ByteArray {
        val iv = IvParameterSpec(aesIV)
        val keySpec = SecretKeySpec(aesKey, "AES")

        println("decryptCBC iVSpec : ${iv}")
        println("decryptCBC keySpec : ${keySpec}")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        //cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        return cipher.doFinal(packet.toByteArray())
    }

//    /*
//    *DECRYPT
//    * */
//    private fun decryptPacketWithCBC(): String {
//        val decodedByte: ByteArray = Base64.decode(this, Base64.DEFAULT)
//        val iv = IvParameterSpec(SECRET_IV.toByteArray())
//        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
//        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
//        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
//
//
//
//
//
//    }

//    fun decryptPacketWithCBC(): ByteArray {
//        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
//        val ivSpec = IvParameterSpec(iv)
//        val keySpec = SecretKeySpec(key, "AES")
//        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
//        return cipher.doFinal(packet)
//    }

//    fun decryptPacketWithCBC(): String {
//        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
//        val key = SecretKeySpec(key, "AES")
//        val iv = IvParameterSpec(iv)
//        cipher.init(Cipher.DECRYPT_MODE, key, iv)
//        val cipherText = cipher.doFinal(packet)
//        return String(cipherText)
//    }

    fun decryptPacketWithCBC(): UByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivSpec = IvParameterSpec(aesIV)
        val keySpec = SecretKeySpec(aesKey, "AES")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        //val cipherText = cipher.doFinal(packet.toByteArray()).toUByteArray()

//        val sb = StringBuilder()
//        for (b in cipherText) {
//            sb.append(b.toChar())
//        }
//
//        return cipherText
        return cipher.doFinal(packet.toByteArray()).toUByteArray()
    }


    @Throws(
        NoSuchPaddingException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class
    )
    fun encryptAES(key: ByteArray?, inputValue: ByteArray?): ByteArray? {
        val sKeyS = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, sKeyS)
        return cipher.doFinal(inputValue)
    }

    @Throws(
        NoSuchPaddingException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class
    )
    fun decryptAES(key: ByteArray?, encryptedData: ByteArray?): ByteArray? {
        val sKeyS = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, sKeyS)
        return cipher.doFinal(encryptedData)
    }

}

class EncryptionService {
    companion object {
        private val KEY_ALGORITHM = "AES"
        private val CIPHER_ALGORITHM = "AES/ECB/PKCS5PADDING"
        private val base64Encoder = Base64.getEncoder()
        private val base64Decoder = Base64.getDecoder()

        fun generateAesKey(): SecretKey {
            val keyGen = KeyGenerator.getInstance(KEY_ALGORITHM)
            keyGen.init(256)
            val secretKey = keyGen.generateKey()
            return secretKey
        }

//        fun generateRandomIV ():  {
//            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
//            val randomSecureRandom = SecureRandom()
//            val iv = ByteArray(128)
//            randomSecureRandom.nextBytes(iv)
//            return  IvParameterSpec(iv)
//        }

        fun encodeAesKeyBase64(key: SecretKey): String {
            return base64Encoder.encodeToString(key.encoded)
        }

        fun decodeAesKeyBase64(keyEncodedAsBase64: String): SecretKey {
            val keyData = base64Decoder.decode(keyEncodedAsBase64)
            return SecretKeySpec(keyData, 0, keyData.size, KEY_ALGORITHM)
        }

        fun encryptToBase64Ciphertext(key: ByteArray, packet: ByteArray): ByteArray {
            val keySpec = SecretKeySpec(key, "AES")
            val aesCipher = Cipher.getInstance(CIPHER_ALGORITHM)
            aesCipher.init(Cipher.ENCRYPT_MODE, keySpec)
            return aesCipher.doFinal(packet)
        }

//        fun decrypt(key: ByteArray, iv: ByteArray, base64Ciphertext: String): ByteArray {
//            val keySpec = SecretKeySpec(key, "AES")
//            val ivSpec = IvParameterSpec(iv)
//            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
//            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
//            val ciphertext = base64Decoder.decode(base64Ciphertext)
//            val decryptedBytes = cipher.doFinal(ciphertext)
//            return String(decryptedBytes)
//        }
    }


}
