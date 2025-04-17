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
import javax.imageio.ImageIO
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
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
            CaptchaType.PATTERN -> generatePatternCaptcha()
            CaptchaType.AUDIO -> generateTextCaptcha() // For audio, we'll use text captcha and convert it to audio
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
     * Generates a pattern sequence for pattern CAPTCHA
     * @return Pattern sequence as a string with the answer separated by a special character
     */
    private fun generatePatternCaptcha(): String {
        return when (captchaProperties.difficultyLevel) {
            DifficultyLevel.EASY -> generateEasyPattern()
            DifficultyLevel.MEDIUM -> generateMediumPattern()
            DifficultyLevel.HARD -> generateHardPattern()
        }
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

    /**
     * Generates an audio representation of the CAPTCHA text
     * @param text The CAPTCHA text to convert to audio
     * @return Byte array of the audio in WAV format
     */
    fun generateCaptchaAudio(text: String): ByteArray {
        val sampleRate = 44100f // 44.1 kHz
        val sampleSizeInBits = 16
        val channels = 1 // mono
        val signed = true
        val bigEndian = false

        val audioFormat = AudioFormat(
            sampleRate,
            sampleSizeInBits,
            channels,
            signed,
            bigEndian
        )

        val outputStream = ByteArrayOutputStream()

        try {
            // Generate audio data
            val audioData = generateAudioData(text, sampleRate)

            // Create audio input stream
            val audioBytes = audioData.toByteArray()
            val audioInputStream = AudioInputStream(
                ByteArrayInputStream(audioBytes),
                audioFormat,
                (audioBytes.size / (sampleSizeInBits / 8)).toLong()
            )

            // Write to output stream
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputStream)

            return outputStream.toByteArray()
        } catch (e: IOException) {
            throw RuntimeException("Failed to generate audio CAPTCHA", e)
        }
    }

    /**
     * Generates audio data for the CAPTCHA text
     * @param text The text to convert to audio
     * @param sampleRate The sample rate of the audio
     * @return ByteArrayOutputStream containing the audio data
     */
    private fun generateAudioData(text: String, sampleRate: Float): ByteArrayOutputStream {
        val outputStream = ByteArrayOutputStream()
        val bytesPerSample = 2 // 16 bits = 2 bytes
        val duration = 0.5 // seconds per character
        val pauseDuration = 0.2 // seconds between characters

        text.forEach { char ->
            // Generate a unique frequency for each character
            val frequency = when {
                char.isDigit() -> 500 + (char.toString().toInt() * 50) // 500-950 Hz for digits
                char.isUpperCase() -> 1000 + ((char - 'A') * 25) // 1000-1625 Hz for uppercase
                else -> 1700 + ((char - 'a') * 25) // 1700-2325 Hz for lowercase
            }

            // Generate tone
            addTone(outputStream, frequency, duration, sampleRate, bytesPerSample)

            // Add pause between characters
            addTone(outputStream, 0, pauseDuration, sampleRate, bytesPerSample)
        }

        return outputStream
    }

    /**
     * Adds a tone to the audio stream
     * @param outputStream The output stream to write to
     * @param frequency The frequency of the tone in Hz (0 for silence)
     * @param duration The duration of the tone in seconds
     * @param sampleRate The sample rate of the audio
     * @param bytesPerSample The number of bytes per sample
     */
    private fun addTone(
        outputStream: ByteArrayOutputStream,
        frequency: Int,
        duration: Double,
        sampleRate: Float,
        bytesPerSample: Int
    ) {
        val numSamples = (duration * sampleRate).toInt()
        val amplitude = if (frequency > 0) 32767 else 0 // Max amplitude for 16-bit audio

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i * frequency / sampleRate
            val sample = (amplitude * Math.sin(angle)).toInt()

            // Write sample to output stream (little endian)
            outputStream.write(sample and 0xFF)
            outputStream.write((sample shr 8) and 0xFF)
        }
    }
}
