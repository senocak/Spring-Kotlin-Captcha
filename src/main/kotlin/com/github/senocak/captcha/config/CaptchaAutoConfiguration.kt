package com.github.senocak.captcha.config

import com.github.senocak.captcha.service.CaptchaService
import com.github.senocak.captcha.service.CaptchaStorageService
import com.github.senocak.captcha.controller.CaptchaController
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Auto-configuration for Spring Kotlin Captcha library.
 * This class automatically configures the necessary beans for the captcha functionality.
 */
@Configuration
@EnableConfigurationProperties(CaptchaProperties::class)
class CaptchaAutoConfiguration {

    /**
     * Creates a CaptchaService bean if one doesn't already exist.
     * @param captchaProperties The captcha configuration properties
     * @return A new CaptchaService instance
     */
    @Bean
    @ConditionalOnMissingBean
    fun captchaService(captchaProperties: CaptchaProperties): CaptchaService =
        CaptchaService(captchaProperties = captchaProperties)

    /**
     * Creates a CaptchaStorageService bean if one doesn't already exist.
     * @param captchaProperties The captcha configuration properties
     * @return A new CaptchaStorageService instance
     */
    @Bean
    @ConditionalOnMissingBean
    fun captchaStorageService(captchaProperties: CaptchaProperties): CaptchaStorageService =
        CaptchaStorageService(captchaProperties = captchaProperties)

    /**
     * Creates a CaptchaController bean if one doesn't already exist.
     * @param captchaService The captcha service
     * @param captchaStorageService The captcha storage service
     * @param captchaProperties The captcha configuration properties
     * @return A new CaptchaController instance
     */
    @Bean
    @ConditionalOnMissingBean
    fun captchaController(
        captchaService: CaptchaService,
        captchaStorageService: CaptchaStorageService,
        captchaProperties: CaptchaProperties
    ): CaptchaController =
        CaptchaController(captchaService = captchaService, captchaStorageService = captchaStorageService, captchaProperties = captchaProperties)
}
