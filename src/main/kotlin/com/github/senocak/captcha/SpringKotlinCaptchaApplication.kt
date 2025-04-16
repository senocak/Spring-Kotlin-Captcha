package com.github.senocak.captcha

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringKotlinCaptchaApplication

fun main(args: Array<String>) {
    runApplication<SpringKotlinCaptchaApplication>(*args)
}