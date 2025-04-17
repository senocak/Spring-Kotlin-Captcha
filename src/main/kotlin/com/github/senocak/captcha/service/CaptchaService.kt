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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.SequenceInputStream
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
     * Generates a random digit string for audio CAPTCHA
     * @param difficultyLevel The difficulty level of the captcha (defaults to configured value)
     * @return Random string of digits
     */
    fun generateAudioCaptchaText(difficultyLevel: DifficultyLevel = DifficultyLevel.MEDIUM): String {
        val length: Int = when (difficultyLevel) {
            DifficultyLevel.EASY -> 4
            DifficultyLevel.MEDIUM -> 6
            DifficultyLevel.HARD -> 8
        }
        return (1..length)
            .map { Random.nextInt(from = 1, until = 10).toString() } // Only digits 1-9 since we have audio files for 1-10
            .joinToString(separator = "")
    }

    /**
     * Generates a random alphanumeric string for text CAPTCHA
     * @param difficultyLevel The difficulty level of the captcha (defaults to configured value)
     * @return Random string of specified length
     */
    fun generateTextCaptcha(
        difficultyLevel: DifficultyLevel = DifficultyLevel.MEDIUM,
        useDigitsOnly: Boolean = false
    ): String {
        val length: Int = when (difficultyLevel) {
            DifficultyLevel.EASY -> 4
            DifficultyLevel.MEDIUM -> 6
            DifficultyLevel.HARD -> 8
        }
        val charSet: List<Char> = if (useDigitsOnly) ('0'..'9').toList() else CHARS
        return (1..length)
            .map { charSet.random() }
            .joinToString(separator = "")
    }

    /**
     * Generates a math problem for math CAPTCHA
     * @param difficultyLevel The difficulty level of the captcha (defaults to configured value)
     * @return Math problem as a string
     */
    fun generateMathCaptcha(difficultyLevel: DifficultyLevel = DifficultyLevel.MEDIUM): String =
        when (difficultyLevel) {
            DifficultyLevel.EASY -> generateEasyMathProblem()
            DifficultyLevel.MEDIUM -> generateMediumMathProblem()
            DifficultyLevel.HARD -> generateHardMathProblem()
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
     * Generates a pattern sequence for pattern CAPTCHA
     * @param difficultyLevel The difficulty level of the captcha (defaults to configured value)
     * @return Pattern sequence as a string with the answer separated by a special character
     */
    fun generatePatternCaptcha(difficultyLevel: DifficultyLevel = DifficultyLevel.MEDIUM): String =
        when (difficultyLevel) {
            DifficultyLevel.EASY -> generateEasyPattern()
            DifficultyLevel.MEDIUM -> generateMediumPattern()
            DifficultyLevel.HARD -> generateHardPattern()
        }

    /**
     * Generates an easy pattern (simple arithmetic progression)
     */
    private fun generateEasyPattern(): String {
        val start = Random.nextInt(1, 5)
        val increment = Random.nextInt(1, 3)
        val sequence = (0 until 4).map { start + it * increment }.joinToString(" ")
        val next = start + 4 * increment
        return "$sequence ? $next"
    }

    /**
     * Generates a medium pattern (more complex arithmetic or alternating patterns)
     */
    private fun generateMediumPattern(): String {
        return when (Random.nextInt(3)) {
            0 -> {
                // Arithmetic sequence with larger numbers
                val start = Random.nextInt(5, 20)
                val increment = Random.nextInt(2, 5)
                val sequence = (0 until 4).map { start + it * increment }.joinToString(" ")
                val next = start + 4 * increment
                "$sequence ? $next"
            }
            1 -> {
                // Alternating increment pattern
                val start = Random.nextInt(1, 10)
                val increment1 = Random.nextInt(1, 4)
                val increment2 = Random.nextInt(1, 4)
                val sequence = mutableListOf<Int>()
                sequence.add(start)
                sequence.add(start + increment1)
                sequence.add(start + increment1 + increment2)
                sequence.add(start + increment1 + increment2 + increment1)
                val next = start + increment1 + increment2 + increment1 + increment2
                "${sequence.joinToString(" ")} ? $next"
            }
            else -> {
                // Multiplication pattern
                val start = Random.nextInt(2, 5)
                val multiplier = Random.nextInt(2, 3)
                val sequence = (0 until 4).map { start * Math.pow(multiplier.toDouble(), it.toDouble()).toInt() }.joinToString(" ")
                val next = start * Math.pow(multiplier.toDouble(), 4.0).toInt()
                "$sequence ? $next"
            }
        }
    }

    /**
     * Generates a hard pattern (complex mathematical patterns)
     */
    private fun generateHardPattern(): String {
        return when (Random.nextInt(3)) {
            0 -> {
                // Fibonacci-like sequence (each number is the sum of the two preceding ones)
                val a = Random.nextInt(1, 5)
                val b = Random.nextInt(a + 1, a + 5)
                val sequence = mutableListOf(a, b)
                for (i in 2 until 4) {
                    sequence.add(sequence[i-2] + sequence[i-1])
                }
                val next = sequence[2] + sequence[3]
                "${sequence.joinToString(" ")} ? $next"
            }
            1 -> {
                // Square/cube pattern
                val sequence = (1..4).map { it * it }.joinToString(" ")
                val next = 5 * 5
                "$sequence ? $next"
            }
            else -> {
                // Complex arithmetic pattern
                val start = Random.nextInt(2, 10)
                val sequence = mutableListOf<Int>()
                sequence.add(start)
                for (i in 1 until 4) {
                    sequence.add(sequence[i-1] + i * 2)
                }
                val next = sequence[3] + 4 * 2
                "${sequence.joinToString(" ")} ? $next"
            }
        }
    }

    /**
     * Converts a string to a CAPTCHA image
     * @param text The text to convert to an image
     * @param useBackgroundImage Whether to use a background image (defaults to configured value)
     * @return Byte array of the image in PNG format
     */
    fun generateCaptchaImage(text: String, useBackgroundImage: Boolean? = false): ByteArray {
        // Create a buffered image
        val image = BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = image.createGraphics()

        // Set rendering hints for better quality
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        // Check if we should use a background image
        if (useBackgroundImage == true) {
            // Load a random background image
            try {
                val bgNumber: Int = Random.nextInt(from = 1, until = 7)
                val bgImageFile = File("src/main/resources/bg/$bgNumber.png")
                if (bgImageFile.exists()) {
                    val bgImage: BufferedImage? = ImageIO.read(bgImageFile)
                    // Draw the background image, scaled to fit the captcha dimensions
                    graphics.drawImage(bgImage, 0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT, null)
                } else {
                    // Fallback to white background if image not found
                    graphics.color = Color.WHITE
                    graphics.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT)
                }
            } catch (e: Exception) {
                // Fallback to white background if there's an error
                graphics.color = Color.WHITE
                graphics.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT)
            }
        } else {
            // Fill background with white
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT)
        }

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
    private fun getRandomColor(): Color =
        Color(
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256)
        )

    /**
     * Generates a random dark color for text (to ensure readability)
     */
    private fun getRandomDarkColor(): Color =
        Color(
            Random.nextInt(100),
            Random.nextInt(100),
            Random.nextInt(100)
        )

    /**
     * Converts a BufferedImage to a byte array
     */
    private fun toByteArray(image: BufferedImage): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Generates an audio representation of the CAPTCHA text using pre-recorded audio files
     * @param text The CAPTCHA text to convert to audio
     * @return Byte array of the audio in WAV format
     */
    fun generateCaptchaAudio(text: String): ByteArray {
        try {
            // Randomly select a voice type
            val selectedVoice: String = arrayOf("man1", "woman1", "woman2").random()
            val validDigits: String = text.filter { it.isDigit() && it != '0' } // We have audio files for 1-9
            if (validDigits.isEmpty()) {
                // If no valid digits found, use a default audio file
                val defaultAudioFile = File("src/main/resources/audios/$selectedVoice/1.mp3")
                if (defaultAudioFile.exists())
                    return defaultAudioFile.readBytes()
                throw IOException("No valid audio files found for the captcha text")
            }
            // If there's only one digit, return its audio file directly
            if (validDigits.length == 1) {
                val digit: Int = validDigits[0].toString().toInt()
                val audioFile = File("src/main/resources/audios/$selectedVoice/$digit.mp3")
                if (audioFile.exists())
                    return audioFile.readBytes()
            }
            // For multiple digits, combine their audio files
            val audioStreams: MutableList<ByteArrayInputStream> = mutableListOf()
            validDigits.forEach { char: Char ->
                val digit: Int = char.toString().toInt()
                val audioFile = File("src/main/resources/audios/$selectedVoice/$digit.mp3")
                if (audioFile.exists()) {
                    audioStreams.add(element = ByteArrayInputStream(audioFile.readBytes()))
                }
            }
            if (audioStreams.isEmpty()) {
                throw IOException("No valid audio files found for the captcha text")
            }
            // Combine all audio streams into one
            val outputStream = ByteArrayOutputStream()
            try {
                // Create a sequence of all audio streams
                var combinedStream: InputStream = audioStreams[0]
                (1 until audioStreams.size).forEach { i: Int ->
                    combinedStream = SequenceInputStream(combinedStream, audioStreams[i])
                }
                // Copy the combined stream to the output stream
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (combinedStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                return outputStream.toByteArray()
            } finally {
                audioStreams.forEach { it.close() }
                outputStream.close()
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate audio CAPTCHA: ${e.localizedMessage}")
        }
    }
}
