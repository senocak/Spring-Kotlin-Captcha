package com.github.senocak.captcha.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "captcha")
class CaptchaProperties {
    var secretKey: String = "ThisIsASecretKey1234567890123456" // 32 bytes for AES-256
    var expirationMinutes: Long = 10
    var captchaType: CaptchaType = CaptchaType.TEXT
}

enum class CaptchaType {
    TEXT, MATH, PATTERN, AUDIO, BACKGROUND_IMAGE
}

enum class DifficultyLevel {
    EASY, MEDIUM, HARD
}
