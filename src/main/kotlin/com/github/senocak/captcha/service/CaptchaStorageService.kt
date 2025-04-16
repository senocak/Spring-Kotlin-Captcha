package com.github.senocak.captcha.service

import com.github.senocak.captcha.config.CaptchaProperties
import com.github.senocak.captcha.config.CaptchaType
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Service
class CaptchaStorageService(private val captchaProperties: CaptchaProperties) {

    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"
        private const val DELIMITER = "::"
        private const val TYPE_DELIMITER = "||"
    }

    /**
     * Generates an encrypted token containing the CAPTCHA value, type, and timestamp
     * @param captchaValue The CAPTCHA value to store
     * @return Encrypted token containing the CAPTCHA value, type, and timestamp
     */
    fun storeCaptcha(captchaValue: String): String {
        val timestamp: String = LocalDateTime.now().toString()
        val captchaType: CaptchaType = captchaProperties.captchaType
        val dataToEncrypt = "$captchaValue$TYPE_DELIMITER$captchaType$DELIMITER$timestamp"
        return encrypt(data = dataToEncrypt)
    }

    /**
     * Validates a CAPTCHA value against the encrypted token
     * @param userInput The user input to validate
     * @param token The encrypted token containing the original CAPTCHA value, type, and timestamp
     * @return True if the input matches the stored CAPTCHA and it hasn't expired
     */
    fun validateCaptcha(userInput: String, token: String): Boolean {
        try {
            val decryptedData: String = decrypt(encryptedData = token)
            val mainParts: List<String> = decryptedData.split(DELIMITER)
            if (mainParts.size != 2)
                return false
            val captchaPart: String = mainParts[0]
            val timestamp: LocalDateTime = LocalDateTime.parse(mainParts[1])
            // If the CAPTCHA has expired, validation fails
            if (isExpired(timestamp = timestamp))
                return false
            val captchaParts: List<String> = captchaPart.split(TYPE_DELIMITER)
            if (captchaParts.size != 2)
                return false
            val storedValue: String = captchaParts[0]
            val captchaType: CaptchaType = CaptchaType.valueOf(captchaParts[1])
            return when (captchaType) {
                CaptchaType.TEXT -> storedValue.equals(other = userInput, ignoreCase = true)
                CaptchaType.MATH -> evaluateMathExpression(storedValue) == userInput.trim().toIntOrNull()
            }
        } catch (e: Exception) {
            // If decryption fails or any other error occurs, validation fails
            return false
        }
    }

    /**
     * Evaluates a math expression
     * @param expression The math expression to evaluate
     * @return The result of the expression
     */
    private fun evaluateMathExpression(expression: String): Int? {
        try {
            // Handle parentheses expressions
            if (expression.contains(other = "(") && expression.contains(other = ")")) {
                val openIndex: Int = expression.indexOf(string = "(")
                val closeIndex: Int = expression.indexOf(string = ")")
                if (openIndex < closeIndex) {
                    val innerExpression: String = expression.substring(startIndex = openIndex + 1, endIndex = closeIndex)
                    val innerResult: Int = evaluateMathExpression(expression = innerExpression) ?: return null
                    val newExpression: String = expression.substring(startIndex = 0, endIndex = openIndex) + innerResult + expression.substring(startIndex = closeIndex + 1)
                    return evaluateMathExpression(expression = newExpression)
                }
            }

            // Handle addition
            if (expression.contains(other = "+")) {
                val parts: List<String> = expression.split("+")
                var sum = 0
                parts.forEach { part: String ->
                    val value: Int = evaluateMathExpression(expression = part.trim()) ?: return null
                    sum += value
                }
                return sum
            }

            // Handle subtraction
            if (expression.contains("-")) {
                val parts: List<String> = expression.split("-")
                var result: Int = evaluateMathExpression(expression = parts[0].trim()) ?: return null
                (1 until parts.size).forEach { i: Int ->
                    val value: Int = evaluateMathExpression(parts[i].trim()) ?: return null
                    result -= value
                }
                return result
            }
            // Handle multiplication
            if (expression.contains(other = "×")) {
                val parts: List<String> = expression.split("×")
                var product = 1
                parts.forEach { part: String ->
                    val value: Int = evaluateMathExpression(expression = part.trim()) ?: return null
                    product *= value
                }
                return product
            }
            // If it's just a number, return it
            return expression.trim().toIntOrNull()
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Checks if a CAPTCHA has expired
     * @param timestamp The timestamp when the CAPTCHA was generated
     * @return True if the CAPTCHA has expired
     */
    private fun isExpired(timestamp: LocalDateTime): Boolean {
        val expirationTime: LocalDateTime? = timestamp.plusMinutes(captchaProperties.expirationMinutes)
        return LocalDateTime.now().isAfter(expirationTime)
    }

    /**
     * Encrypts data using AES encryption
     * @param data The data to encrypt
     * @return Base64-encoded encrypted data
     */
    private fun encrypt(data: String): String {
        val key = SecretKeySpec(captchaProperties.secretKey.toByteArray(), ALGORITHM)
        val cipher: Cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes: ByteArray = cipher.doFinal(data.toByteArray())
        return Base64.getUrlEncoder().encodeToString(encryptedBytes)
    }

    /**
     * Decrypts data using AES encryption
     * @param encryptedData Base64-encoded encrypted data
     * @return Decrypted data
     */
    private fun decrypt(encryptedData: String): String {
        val key = SecretKeySpec(captchaProperties.secretKey.toByteArray(), ALGORITHM)
        val cipher: Cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedBytes: ByteArray = Base64.getUrlDecoder().decode(encryptedData)
        val decryptedBytes: ByteArray = cipher.doFinal(decodedBytes)
        return String(bytes = decryptedBytes)
    }
}
