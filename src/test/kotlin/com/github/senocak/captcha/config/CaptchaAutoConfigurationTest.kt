package com.github.senocak.captcha.config

import com.github.senocak.captcha.controller.CaptchaController
import com.github.senocak.captcha.service.CaptchaService
import com.github.senocak.captcha.service.CaptchaStorageService
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.assertj.core.api.Assertions.assertThat

class CaptchaAutoConfigurationTest {

    private val contextRunner = WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(CaptchaAutoConfiguration::class.java))

    @Test
    fun `should auto-configure captcha beans`() {
        contextRunner.run { context ->
            assertThat(context).hasSingleBean(CaptchaProperties::class.java)
            assertThat(context).hasSingleBean(CaptchaService::class.java)
            assertThat(context).hasSingleBean(CaptchaStorageService::class.java)
            assertThat(context).hasSingleBean(CaptchaController::class.java)
        }
    }

    @Test
    fun `should use custom properties`() {
        contextRunner
            .withPropertyValues(
                "captcha.secret-key=CustomSecretKey1234567890123456789012",
                "captcha.expiration-minutes=15",
                "captcha.default-type=MATH",
                "captcha.default-difficulty=HARD",
                "captcha.use-background-image=true"
            )
            .run { context ->
                val properties = context.getBean(CaptchaProperties::class.java)
                assertThat(properties.secretKey).isEqualTo("CustomSecretKey1234567890123456789012")
                assertThat(properties.expirationMinutes).isEqualTo(15)
                assertThat(properties.defaultType).isEqualTo(CaptchaType.MATH)
                assertThat(properties.defaultDifficulty).isEqualTo(DifficultyLevel.HARD)
                assertThat(properties.useBackgroundImage).isTrue()
            }
    }

    @Test
    fun `should allow overriding beans`() {
        contextRunner
            .withUserConfiguration(TestConfiguration::class.java)
            .run { context ->
                assertThat(context).hasSingleBean(CaptchaService::class.java)
                assertThat(context).getBean(CaptchaService::class.java)
                    .isExactlyInstanceOf(TestConfiguration.CustomCaptchaService::class.java)
            }
    }

    class TestConfiguration {
        @org.springframework.context.annotation.Bean
        fun captchaService(captchaProperties: CaptchaProperties): CaptchaService {
            return CustomCaptchaService(captchaProperties)
        }

        class CustomCaptchaService(captchaProperties: CaptchaProperties) : CaptchaService(captchaProperties)
    }
}