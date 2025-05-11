package quran.shakir

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility class for AES encryption and decryption using a password
 */
object AESCrypt {
    private const val ALGORITHM = "AES/CBC/PKCS7Padding"
    private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_LENGTH = 256
    private const val ITERATION_COUNT = 10000

    /**
     * Decrypts the given encrypted string using the provided password
     *
     * @param encryptedText Base64 encoded string containing salt, IV, and encrypted data
     * @param password Password to derive the decryption key from
     * @return The decrypted string
     */
    fun decrypt(encryptedText: String, password: String): String {
        try {
            // Decode from Base64
            val encryptedData = Base64.decode(encryptedText, Base64.DEFAULT)

            // First 16 bytes: salt
            // Next 16 bytes: IV
            // Remaining: encrypted data
            val salt = encryptedData.copyOfRange(0, 16)
            val iv = encryptedData.copyOfRange(16, 32)
            val encrypted = encryptedData.copyOfRange(32, encryptedData.size)

            // Derive the key from password
            val secretKey = generateKey(password, salt)

            // Initialize cipher for decryption
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                IvParameterSpec(iv)
            )

            // Decrypt and convert to string
            val decryptedBytes = cipher.doFinal(encrypted)
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw RuntimeException("Decryption failed", e)
        }
    }

    /**
     * Encrypts the given string using the provided password
     *
     * @param plainText String to encrypt
     * @param password Password to derive the encryption key from
     * @return Base64 encoded string containing salt, IV, and encrypted data
     */
    fun encrypt(plainText: String, password: String): String {
        try {
            // Generate random salt and IV
            val random = SecureRandom()
            val salt = ByteArray(16)
            val iv = ByteArray(16)
            random.nextBytes(salt)
            random.nextBytes(iv)

            // Derive the key from password
            val secretKey = generateKey(password, salt)

            // Initialize cipher for encryption
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(
                Cipher.ENCRYPT_MODE,
                secretKey,
                IvParameterSpec(iv)
            )

            // Encrypt
            val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

            // Combine salt + IV + encrypted data
            val combined = ByteArray(salt.size + iv.size + encrypted.size)
            System.arraycopy(salt, 0, combined, 0, salt.size)
            System.arraycopy(iv, 0, combined, salt.size, iv.size)
            System.arraycopy(encrypted, 0, combined, salt.size + iv.size, encrypted.size)

            // Encode as Base64 string
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            throw RuntimeException("Encryption failed", e)
        }
    }

    /**
     * Generates an AES key from a password and salt using PBKDF2
     */
    private fun generateKey(password: String, salt: ByteArray): SecretKeySpec {
        val keySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            ITERATION_COUNT,
            KEY_LENGTH
        )
        val keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val keyBytes = keyFactory.generateSecret(keySpec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}