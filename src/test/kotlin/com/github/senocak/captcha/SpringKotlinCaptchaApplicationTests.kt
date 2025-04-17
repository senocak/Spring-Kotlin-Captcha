package com.github.senocak.captcha

import com.github.senocak.captcha.config.DifficultyLevel
import com.github.senocak.captcha.service.CaptchaService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SpringKotlinCaptchaApplicationTests {

    @Autowired
    private lateinit var captchaService: CaptchaService

    @Test
    fun contextLoads() {
    }

    @Test
    fun `test generateTextCaptcha with useDigitsOnly parameter`() {
        // Test with useDigitsOnly = true
        val digitsOnlyCaptcha = captchaService.generateTextCaptcha(useDigitsOnly = true)
        println("[DEBUG_LOG] Digits only captcha: $digitsOnlyCaptcha")
        assertTrue(digitsOnlyCaptcha.all { it.isDigit() }, "Captcha should contain only digits when useDigitsOnly is true")

        // Test with useDigitsOnly = false (default)
        val regularCaptcha = captchaService.generateTextCaptcha()
        println("[DEBUG_LOG] Regular captcha: $regularCaptcha")

        // Run multiple times to increase the chance of getting non-digits
        repeat(5) {
            val anotherRegularCaptcha = captchaService.generateTextCaptcha()
            println("[DEBUG_LOG] Another regular captcha: $anotherRegularCaptcha")
        }
    }
}
