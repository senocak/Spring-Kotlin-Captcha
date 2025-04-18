package com.github.senocak.captcha.controller

import com.github.senocak.captcha.config.CaptchaProperties
import com.github.senocak.captcha.config.CaptchaType
import com.github.senocak.captcha.config.DifficultyLevel
import com.github.senocak.captcha.service.CaptchaService
import com.github.senocak.captcha.service.CaptchaStorageService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Controller
class HomeController {
    /**
     * Renders the home page
     * @return The name of the template to render
     */
    @GetMapping("/")
    fun home(): String = "home"
}

@RestController
@RequestMapping("/captcha")
class CaptchaController(
    private val captchaService: CaptchaService,
    private val captchaStorageService: CaptchaStorageService,
    private val captchaProperties: CaptchaProperties
) {
    companion object {
        private const val CAPTCHA_TOKEN_HEADER = "X-Captcha-Token"
    }

    /**
     * Returns the current captcha configuration
     * @return The current captcha configuration
     */
    @GetMapping("/config")
    fun getCaptchaConfig(): Map<String, Any> =
        mapOf(
            "DifficultyLevel" to DifficultyLevel.entries.map { it.name },
        )

    /**
     * Generates a new text CAPTCHA image and returns it with a token in the header
     * @param difficultyLevel The difficulty level of the captcha (optional)
     * @param useBackgroundImage Whether to use a background image (optional)
     * @return The text CAPTCHA image as a byte array with a token header
     */
    @GetMapping("/text", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getTextCaptchaImage(
        @RequestParam(required = false) difficultyLevel: DifficultyLevel?,
        @RequestParam(required = false) useBackgroundImage: Boolean? = null
    ): ResponseEntity<ByteArray> {
        val captchaText: String = captchaService.generateTextCaptcha(
            difficultyLevel = difficultyLevel ?: captchaProperties.defaultDifficulty
        )
        val token: String = captchaStorageService.storeCaptcha(
            captchaValue = captchaText,
            captchaType = CaptchaType.TEXT
        )
        val captchaImage: ByteArray = captchaService.generateCaptchaImage(
            text = captchaText,
            useBackgroundImage = useBackgroundImage
        )
        return ResponseEntity.ok()
            .header(CAPTCHA_TOKEN_HEADER, token)
            .body(captchaImage)
    }

    /**
     * Generates a new math CAPTCHA image and returns it with a token in the header
     * @param difficultyLevel The difficulty level of the captcha (optional)
     * @param useBackgroundImage Whether to use a background image (optional)
     * @return The math CAPTCHA image as a byte array with a token header
     */
    @GetMapping("/math", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getMathCaptchaImage(
        @RequestParam(required = false) difficultyLevel: DifficultyLevel?,
        @RequestParam(required = false) useBackgroundImage: Boolean? = null
    ): ResponseEntity<ByteArray> {
        val captchaText: String = captchaService.generateMathCaptcha(
            difficultyLevel = difficultyLevel ?: captchaProperties.defaultDifficulty
        )
        val token: String = captchaStorageService.storeCaptcha(
            captchaValue = captchaText,
            captchaType = CaptchaType.MATH
        )
        val captchaImage: ByteArray = captchaService.generateCaptchaImage(
            text = captchaText,
            useBackgroundImage = useBackgroundImage
        )
        return ResponseEntity.ok()
            .header(CAPTCHA_TOKEN_HEADER, token)
            .body(captchaImage)
    }

    /**
     * Generates a new pattern CAPTCHA image and returns it with a token in the header
     * @param difficultyLevel The difficulty level of the captcha (optional)
     * @param useBackgroundImage Whether to use a background image (optional)
     * @return The pattern CAPTCHA image as a byte array with a token header
     */
    @GetMapping("/pattern", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getPatternCaptchaImage(
        @RequestParam(required = false) difficultyLevel: DifficultyLevel?,
        @RequestParam(required = false) useBackgroundImage: Boolean? = null
    ): ResponseEntity<ByteArray> {
        val captchaText: String = captchaService.generatePatternCaptcha(
            difficultyLevel = difficultyLevel ?: captchaProperties.defaultDifficulty
        )
        val token: String = captchaStorageService.storeCaptcha(
            captchaValue = captchaText,
            captchaType = CaptchaType.PATTERN
        )
        val captchaImage: ByteArray = captchaService.generateCaptchaImage(
            text = captchaText,
            useBackgroundImage = useBackgroundImage
        )
        return ResponseEntity.ok()
            .header(CAPTCHA_TOKEN_HEADER, token)
            .body(captchaImage)
    }

    /**
     * Generates a new CAPTCHA audio and returns it with a token in the header
     * @param difficultyLevel The difficulty level of the captcha (optional)
     * @return The CAPTCHA audio as a byte array with a token header
     */
    @GetMapping("/audio", produces = ["audio/wav"])
    fun getCaptchaAudio(
        @RequestParam(required = false) difficultyLevel: DifficultyLevel?
    ): ResponseEntity<ByteArray> {
        val captchaText: String = captchaService.generateTextCaptcha(
            difficultyLevel = difficultyLevel ?: captchaProperties.defaultDifficulty,
            useDigitsOnly = true
        )
        val token: String = captchaStorageService.storeCaptcha(
            captchaValue = captchaText,
            captchaType = CaptchaType.AUDIO
        )
        val captchaAudio: ByteArray = captchaService.generateCaptchaAudio(text = captchaText)
        return ResponseEntity.ok()
            .header(CAPTCHA_TOKEN_HEADER, token)
            .body(captchaAudio)
    }

    /**
     * Validates a CAPTCHA input against the token
     * @param captchaInput The user input to validate
     * @param token The encrypted token containing the original CAPTCHA value
     * @return A response indicating whether the CAPTCHA is valid
     */
    @PostMapping("/validate")
    fun validateCaptcha(
        @RequestParam captchaInput: String,
        @RequestParam token: String
    ): ResponseEntity<Map<String, Boolean>> {
        val isValid: Boolean = captchaStorageService.validateCaptcha(userInput = captchaInput, token = token)
        val response: Map<String, Boolean> = mapOf("valid" to isValid)
        return when {
            isValid -> ResponseEntity.ok(response)
            else -> ResponseEntity.badRequest().body(response)
        }
    }
}
