package com.github.senocak.captcha.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for the captcha library.
 * These properties can be configured in your application.properties or application.yml file
 * with the prefix "captcha".
 *
 * Example:
 * ```
 * captcha:
 *   secret-key: YourSecretKey1234567890123456789012
 *   expiration-minutes: 15
 *   default-type: MATH
 *   default-difficulty: HARD
 *   use-background-image: true
 * ```
 */
@ConfigurationProperties(prefix = "captcha")
data class CaptchaProperties(
    /**
     * Secret key used for AES encryption of captcha tokens.
     * Must be 32 bytes (256 bits) for AES-256.
     */
    var secretKey: String = "ThisIsASecretKey1234567890123456",

    /**
     * How long captchas are valid, in minutes.
     */
    var expirationMinutes: Long = 10,

    /**
     * Default captcha type to use if not specified.
     */
    var defaultType: CaptchaType = CaptchaType.TEXT,

    /**
     * Default difficulty level to use if not specified.
     */
    var defaultDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM,

    /**
     * Whether to use background images by default.
     */
    var useBackgroundImage: Boolean = false
)

/**
 * Types of captchas supported by the library.
 */
enum class CaptchaType {
    /**
     * Text captcha with random alphanumeric characters.
     */
    TEXT,

    /**
     * Math captcha with arithmetic expressions.
     */
    MATH,

    /**
     * Pattern captcha with sequence patterns.
     */
    PATTERN,

    /**
     * Audio captcha with spoken digits.
     */
    AUDIO,

    /**
     * Text captcha with a background image.
     */
    BACKGROUND_IMAGE
}

/**
 * Difficulty levels for captchas.
 */
enum class DifficultyLevel {
    /**
     * Easy difficulty (shorter length, simpler patterns).
     */
    EASY,

    /**
     * Medium difficulty (moderate length and complexity).
     */
    MEDIUM,

    /**
     * Hard difficulty (longer length, more complex patterns).
     */
    HARD
}
