# Spring Kotlin CAPTCHA

A flexible and customizable CAPTCHA implementation for Spring Boot applications written in Kotlin. This library provides multiple CAPTCHA types with varying difficulty levels to protect your web applications from bots.

## Features

- **Multiple CAPTCHA Types**:
  - **Text**: Random alphanumeric characters
  - **Math**: Mathematical expressions to solve
  - **Pattern**: Sequence patterns where users identify the next number
  - **Audio**: Audio representation of digits for accessibility
  - **Background Image**: Text captcha with decorative background images

- **Configurable Difficulty Levels**:
  - **Easy**: Simpler challenges suitable for most users
  - **Medium**: Moderate difficulty for better protection
  - **Hard**: Complex challenges for maximum security

- **Security Features**:
  - AES-256 encryption for CAPTCHA tokens
  - Configurable expiration time
  - Secure validation mechanism

- **Customizable Appearance**:
  - Random fonts, colors, and rotations
  - Noise elements (lines and dots) to prevent OCR
  - Responsive design

## Technologies Used

- Kotlin 1.9.24
- Spring Boot 3.3.1
- Java 17
- Thymeleaf for templating
- AES encryption for secure token handling

## Getting Started

### Prerequisites

- JDK 17 or higher
- Gradle or Maven

### Installation

#### Gradle

Add the following to your `build.gradle` or `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    // You may need to add the repository where this library is published
    maven {
        url = uri("https://github.com/senocak/Spring-Kotlin-Captcha/raw/maven-repo")
    }
}

dependencies {
    implementation("com.github.senocak:Spring-Kotlin-Captcha:0.0.1")
}
```

#### Maven

Add the following to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.senocak</groupId>
        <artifactId>Spring-Kotlin-Captcha</artifactId>
        <version>0.0.1</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>spring-kotlin-captcha</id>
        <url>https://github.com/senocak/Spring-Kotlin-Captcha/raw/maven-repo</url>
    </repository>
</repositories>
```

### Local Development

If you want to build and use the library locally:

1. Clone the repository:
   ```bash
   git clone https://github.com/senocak/Spring-Kotlin-Captcha.git
   cd Spring-Kotlin-Captcha
   ```

2. Build and publish to local Maven repository:
   ```bash
   ./gradlew publishToMavenLocal
   ```

3. Add `mavenLocal()` to your repositories in your project.

## Usage

### Auto-Configuration

The library uses Spring Boot's auto-configuration mechanism, so it will be automatically configured when added to your Spring Boot application. No additional setup is required.

### API Endpoints

The following endpoints are automatically available in your application:

- **GET /captcha/config**: Returns the current captcha configuration
  - Returns: JSON with available difficulty levels

- **GET /captcha/text**: Generates a new text CAPTCHA image
  - Parameters:
    - `difficultyLevel` (optional): EASY, MEDIUM, or HARD
    - `useBackgroundImage` (optional): true or false
  - Returns: PNG image with CAPTCHA
  - Header: `X-Captcha-Token` containing the encrypted token

- **GET /captcha/math**: Generates a new math CAPTCHA image
  - Parameters:
    - `difficultyLevel` (optional): EASY, MEDIUM, or HARD
    - `useBackgroundImage` (optional): true or false
  - Returns: PNG image with CAPTCHA
  - Header: `X-Captcha-Token` containing the encrypted token

- **GET /captcha/pattern**: Generates a new pattern CAPTCHA image
  - Parameters:
    - `difficultyLevel` (optional): EASY, MEDIUM, or HARD
    - `useBackgroundImage` (optional): true or false
  - Returns: PNG image with CAPTCHA
  - Header: `X-Captcha-Token` containing the encrypted token

- **GET /captcha/audio**: Generates a new audio CAPTCHA
  - Parameters:
    - `difficultyLevel` (optional): EASY, MEDIUM, or HARD
  - Returns: WAV audio file with spoken digits
  - Header: `X-Captcha-Token` containing the encrypted token

- **POST /captcha/validate**: Validates a CAPTCHA input
  - Parameters:
    - `captchaInput`: User's input
    - `token`: The token received from any of the captcha generation endpoints
  - Returns: JSON with `valid` boolean field

### Integration Example

```html
<!-- HTML -->
<form id="myForm">
  <div>
    <img id="captchaImage" src="/captcha/text" alt="CAPTCHA">
    <button type="button" onclick="refreshCaptcha()">Refresh</button>
  </div>
  <input type="text" id="captchaInput" name="captchaInput" required>
  <input type="hidden" id="captchaToken" name="token">
  <button type="submit">Submit</button>
</form>

<!-- JavaScript -->
<script>
  // Load CAPTCHA image and store token
  function loadCaptcha() {
    // You can use any of the captcha endpoints: /captcha/text, /captcha/math, /captcha/pattern
    fetch('/captcha/text?difficultyLevel=MEDIUM&useBackgroundImage=true')
      .then(response => {
        const token = response.headers.get('X-Captcha-Token');
        document.getElementById('captchaToken').value = token;
        return response.blob();
      })
      .then(blob => {
        document.getElementById('captchaImage').src = URL.createObjectURL(blob);
      });
  }

  // Refresh CAPTCHA
  function refreshCaptcha() {
    loadCaptcha();
    document.getElementById('captchaInput').value = '';
  }

  // Initialize
  document.addEventListener('DOMContentLoaded', loadCaptcha);

  // Form submission
  document.getElementById('myForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const input = document.getElementById('captchaInput').value;
    const token = document.getElementById('captchaToken').value;

    fetch('/captcha/validate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: `captchaInput=${encodeURIComponent(input)}&token=${encodeURIComponent(token)}`
    })
    .then(response => response.json())
    .then(data => {
      if (data.valid) {
        // CAPTCHA validation successful - proceed with form submission
        console.log('CAPTCHA validated successfully');
        // Submit the form or perform other actions
        // document.getElementById('myForm').submit();
      } else {
        // CAPTCHA validation failed
        console.log('CAPTCHA validation failed');
        refreshCaptcha();
      }
    });
  });
</script>
```

### Audio CAPTCHA Example

```html
<div>
  <audio id="captchaAudio" controls></audio>
  <button type="button" onclick="loadAudioCaptcha()">Load Audio CAPTCHA</button>
  <input type="text" id="audioCaptchaInput" placeholder="Enter the digits you hear">
  <input type="hidden" id="audioCaptchaToken">
  <button type="button" onclick="validateAudioCaptcha()">Validate</button>
</div>

<script>
  function loadAudioCaptcha() {
    fetch('/captcha/audio?difficultyLevel=EASY')
      .then(response => {
        const token = response.headers.get('X-Captcha-Token');
        document.getElementById('audioCaptchaToken').value = token;
        return response.blob();
      })
      .then(blob => {
        const audioUrl = URL.createObjectURL(blob);
        const audioElement = document.getElementById('captchaAudio');
        audioElement.src = audioUrl;
        audioElement.play();
      });
  }

  function validateAudioCaptcha() {
    const input = document.getElementById('audioCaptchaInput').value;
    const token = document.getElementById('audioCaptchaToken').value;

    fetch('/captcha/validate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: `captchaInput=${encodeURIComponent(input)}&token=${encodeURIComponent(token)}`
    })
    .then(response => response.json())
    .then(data => {
      if (data.valid) {
        alert('Audio CAPTCHA validated successfully');
      } else {
        alert('Audio CAPTCHA validation failed');
        loadAudioCaptcha();
      }
    });
  }
</script>
```

## Configuration

You can configure the CAPTCHA behavior in your `application.yml` or `application.properties` file:

```yaml
captcha:
  secret-key: YourSecretKey1234567890123456789012 # 32 bytes for AES-256
  expiration-minutes: 10
  default-type: TEXT # Options: TEXT, MATH, PATTERN, AUDIO, BACKGROUND_IMAGE
  default-difficulty: MEDIUM # Options: EASY, MEDIUM, HARD
  use-background-image: false # Enable background images by default
```

Or in properties format:

```properties
captcha.secret-key=YourSecretKey1234567890123456789012
captcha.expiration-minutes=10
captcha.default-type=TEXT
captcha.default-difficulty=MEDIUM
captcha.use-background-image=false
```

### Configuration Options

| Property | Description | Default | Options |
|----------|-------------|---------|---------|
| secret-key | Secret key for AES encryption | ThisIsASecretKey1234567890123456 | 32-byte string |
| expiration-minutes | CAPTCHA validity period | 10 | Any positive number |
| default-type | Default type of CAPTCHA challenge | TEXT | TEXT, MATH, PATTERN, AUDIO, BACKGROUND_IMAGE |
| default-difficulty | Default difficulty level | MEDIUM | EASY, MEDIUM, HARD |
| use-background-image | Enable background images by default | false | true, false |

### Customizing the Library

If you need to customize the behavior beyond what's available through configuration properties, you can provide your own beans to override the default ones:

```kotlin
@Configuration
class CustomCaptchaConfig {

    @Bean
    fun captchaService(captchaProperties: CaptchaProperties): CaptchaService {
        // Create a custom implementation or extend the default one
        return CustomCaptchaService(captchaProperties)
    }

    @Bean
    fun captchaStorageService(captchaProperties: CaptchaProperties): CaptchaStorageService {
        // Create a custom implementation or extend the default one
        return CustomCaptchaStorageService(captchaProperties)
    }
}
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Inspired by various CAPTCHA implementations
- Built with Spring Boot and Kotlin
