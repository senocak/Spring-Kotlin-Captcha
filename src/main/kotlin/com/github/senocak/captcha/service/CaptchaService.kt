package com.github.senocak.captcha.service

import com.github.senocak.captcha.config.CaptchaProperties
import com.github.senocak.captcha.config.CaptchaType
import com.github.senocak.captcha.config.DifficultyLevel
import org.springframework.stereotype.Service
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.random.Random

@Service
class CaptchaService(private val captchaProperties: CaptchaProperties) {

    companion object {
        private const val CAPTCHA_WIDTH = 200
        private const val CAPTCHA_HEIGHT = 80
        private const val FONT_SIZE = 40
        private val CHARS: List<Char> = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    }

    /**
     * Generates captcha text based on the configured type and difficulty
     * @return Random string or math problem
     */
    fun generateCaptchaText(): String {
        return when (captchaProperties.captchaType) {
            CaptchaType.TEXT -> generateTextCaptcha()
            CaptchaType.MATH -> generateMathCaptcha()
        }
    }

    /**
     * Generates a random alphanumeric string for text CAPTCHA
     * @return Random string of specified length
     */
    private fun generateTextCaptcha(): String {
        val length: Int = when (captchaProperties.difficultyLevel) {
            DifficultyLevel.EASY -> 4
            DifficultyLevel.MEDIUM -> 6
            DifficultyLevel.HARD -> 8
        }

        return (1..length)
            .map { CHARS.random() }
            .joinToString(separator = "")
    }

    /**
     * Generates a math problem for math CAPTCHA
     * @return Math problem as a string
     */
    private fun generateMathCaptcha(): String {
        return when (captchaProperties.difficultyLevel) {
            DifficultyLevel.EASY -> generateEasyMathProblem()
            DifficultyLevel.MEDIUM -> generateMediumMathProblem()
            DifficultyLevel.HARD -> generateHardMathProblem()
        }
    }

    private fun generateEasyMathProblem(): String =
        "${Random.nextInt(from = 1, until = 10)} + ${Random.nextInt(from = 1, until = 10)}"

    private fun generateMediumMathProblem(): String {
        val a: Int = Random.nextInt(from = 1, until = 20)
        val b: Int = Random.nextInt(from = 1, until = 10)
        return when (Random.nextInt(from = 0, until = 3)) {
            0 -> "$a + $b"
            1 -> "$a - $b"
            else -> "$a × $b"
        }
    }

    private fun generateHardMathProblem(): String {
        val a: Int = Random.nextInt(from = 10, until = 50)
        val b: Int = Random.nextInt(from = 1, until = 20)
        val c: Int = Random.nextInt(from = 1, until = 10)
        return when (Random.nextInt(from = 0, until = 4)) {
            0 -> "$a + $b + $c"
            1 -> "$a - $b + $c"
            2 -> "$a × $b"
            else -> "($a + $b) × $c"
        }
    }

    /**
     * Converts a string to a CAPTCHA image
     * @param text The text to convert to an image
     * @return Byte array of the image in PNG format
     */
    fun generateCaptchaImage(text: String): ByteArray {
        // Create a buffered image
        val image = BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()

        // Set rendering hints for better quality
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        // Fill background with white
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT)

        // Add noise (random lines)
        addNoise(graphics = graphics)

        // Draw the text
        drawText(graphics = graphics, text = text)

        // Add more noise (random dots)
        addDots(graphics = graphics)

        // Dispose of the graphics context
        graphics.dispose()

        // Convert the image to a byte array
        return toByteArray(image = image)
    }

    /**
     * Adds random lines to the image for noise
     */
    private fun addNoise(graphics: Graphics2D) {
        graphics.stroke = BasicStroke(2f)
        // Add 5-10 random lines
        repeat(Random.nextInt(5, 10)) {
            graphics.color = getRandomColor()
            val x1: Int = Random.nextInt(CAPTCHA_WIDTH)
            val y1: Int = Random.nextInt(CAPTCHA_HEIGHT)
            val x2: Int = Random.nextInt(CAPTCHA_WIDTH)
            val y2: Int = Random.nextInt(CAPTCHA_HEIGHT)
            graphics.drawLine(x1, y1, x2, y2)
        }
    }

    /**
     * Adds random dots to the image for additional noise
     */
    private fun addDots(graphics: Graphics2D) {
        // Add 50-100 random dots
        repeat(times = Random.nextInt(50, 100)) { it: Int ->
            graphics.color = getRandomColor()
            val x: Int = Random.nextInt(CAPTCHA_WIDTH)
            val y: Int = Random.nextInt(CAPTCHA_HEIGHT)
            val size: Int = Random.nextInt(1, 3)
            graphics.fillOval(x, y, size, size)
        }
    }

    /**
     * Draws the CAPTCHA text on the image with random fonts and colors
     */
    private fun drawText(graphics: Graphics2D, text: String) {
        val fonts: Array<Font> = arrayOf(
            Font("Arial", Font.BOLD, FONT_SIZE),
            Font("Verdana", Font.ITALIC, FONT_SIZE),
            Font("Tahoma", Font.PLAIN, FONT_SIZE)
        )

        // Calculate the width of each character
        val charWidth: Int = CAPTCHA_WIDTH / (text.length + 1)

        // Draw each character with a random font, color, and slight rotation
        text.forEachIndexed { index: Int, char: Char ->
            graphics.font = fonts.random()
            graphics.color = getRandomDarkColor()

            // Add slight rotation for each character
            val rotation: Double = Random.nextDouble(-0.4, 0.4)
            graphics.rotate(rotation, (index + 1) * charWidth.toDouble(), CAPTCHA_HEIGHT / 2.0)

            // Draw the character
            graphics.drawString(
                char.toString(),
                (index + 1) * charWidth - FONT_SIZE / 4,
                CAPTCHA_HEIGHT / 2 + FONT_SIZE / 3
            )

            // Reset rotation
            graphics.rotate(-rotation, (index + 1) * charWidth.toDouble(), CAPTCHA_HEIGHT / 2.0)
        }
    }

    /**
     * Generates a random color
     */
    private fun getRandomColor(): Color {
        return Color(
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256)
        )
    }

    /**
     * Generates a random dark color for text (to ensure readability)
     */
    private fun getRandomDarkColor(): Color {
        return Color(
            Random.nextInt(100),
            Random.nextInt(100),
            Random.nextInt(100)
        )
    }

    /**
     * Converts a BufferedImage to a byte array
     */
    private fun toByteArray(image: BufferedImage): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return outputStream.toByteArray()
    }
}
