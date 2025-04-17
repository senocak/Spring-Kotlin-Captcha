package com.github.senocak.captcha.controller

import com.github.senocak.captcha.config.CaptchaProperties
import com.github.senocak.captcha.config.CaptchaType
import com.github.senocak.captcha.config.DifficultyLevel
import com.github.senocak.captcha.service.CaptchaService
import com.github.senocak.captcha.service.CaptchaStorageService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
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
    @GetMapping("/captcha/config")
    @ResponseBody
    fun getCaptchaConfig(): Map<String, Any> =
        mapOf(
            "type" to captchaProperties.captchaType.name,
            "difficulty" to captchaProperties.difficultyLevel.name
        )

    /**
     * Renders the home page
     * @return The name of the template to render
     */
    @GetMapping("/")
    fun home(): String = "home"

    /**
     * Generates a new CAPTCHA image and returns it with a token in the header
     * @return The CAPTCHA image as a byte array with a token header
     */
    @GetMapping("/captcha/image", produces = [MediaType.IMAGE_PNG_VALUE])
    @ResponseBody
    fun getCaptchaImage(): ResponseEntity<ByteArray> {
        val captchaText: String = captchaService.generateCaptchaText()
        val token: String = captchaStorageService.storeCaptcha(captchaValue = captchaText) // Generate token with the CAPTCHA text
        val captchaImage: ByteArray = captchaService.generateCaptchaImage(text = captchaText) // Generate the CAPTCHA image
        // Return the image with the token in the header
        val headers = HttpHeaders()
        headers.add(CAPTCHA_TOKEN_HEADER, token)
        return ResponseEntity.ok()
            .headers(headers)
            .body(captchaImage)
    }

    /**
     * Generates a new CAPTCHA audio and returns it with a token in the header
     * @return The CAPTCHA audio as a byte array with a token header
     */
    @GetMapping("/captcha/audio", produces = ["audio/wav"])
    @ResponseBody
    fun getCaptchaAudio(): ResponseEntity<ByteArray> {
        val captchaText: String = captchaService.generateCaptchaText()
        val token: String = captchaStorageService.storeCaptcha(captchaValue = captchaText) // Generate token with the CAPTCHA text
        val captchaAudio: ByteArray = captchaService.generateCaptchaAudio(text = captchaText) // Generate the CAPTCHA audio
        // Return the audio with the token in the header
        val headers = HttpHeaders()
        headers.add(CAPTCHA_TOKEN_HEADER, token)
        return ResponseEntity.ok()
            .headers(headers)
            .body(captchaAudio)
    }

    /**
     * Validates a CAPTCHA input against the token
     * @param captchaInput The user input to validate
     * @param token The encrypted token containing the original CAPTCHA value
     * @return A response indicating whether the CAPTCHA is valid
     */
    @PostMapping("/captcha/validate")
    @ResponseBody
    fun validateCaptcha(
        @RequestParam captchaInput: String,
        @RequestParam token: String
    ): ResponseEntity<Map<String, Boolean>> {
        val isValid: Boolean = captchaStorageService.validateCaptcha(userInput = captchaInput, token = token)
        val response: Map<String, Boolean> = mapOf("valid" to isValid)
        return when {
            isValid -> ResponseEntity.ok(response)
            else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
        }
    }
}
