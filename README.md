# Spring Kotlin CAPTCHA

A flexible and customizable CAPTCHA implementation for Spring Boot applications written in Kotlin. This library provides multiple CAPTCHA types with varying difficulty levels to protect your web applications from bots.

## Features

- **Multiple CAPTCHA Types**:
  - **Text**: Random alphanumeric characters
  - **Math**: Mathematical expressions to solve
  - **Pattern**: Sequence patterns where users identify the next number

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

1. Clone the repository:
   ```bash
   git clone https://github.com/senocak/Spring-Kotlin-Captcha.git
   cd Spring-Kotlin-Captcha
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

The application will start on port 8081 by default. You can access the demo page at `http://localhost:8081/`.

## Usage

### API Endpoints

- **GET /captcha/image**: Generates a new CAPTCHA image
  - Returns: PNG image with CAPTCHA
  - Header: `X-Captcha-Token` containing the encrypted token

- **POST /captcha/validate**: Validates a CAPTCHA input
  - Parameters:
    - `captchaInput`: User's input
    - `token`: The token received from `/captcha/image`
  - Returns: JSON with `valid` boolean field

- **GET /captcha/config**: Returns the current CAPTCHA configuration
  - Returns: JSON with `type` and `difficulty` fields

### Integration Example

```html
<!-- HTML -->
<form id="myForm">
  <div>
    <img id="captchaImage" src="/captcha/image" alt="CAPTCHA">
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
    fetch('/captcha/image')
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
      } else {
        // CAPTCHA validation failed
        console.log('CAPTCHA validation failed');
        refreshCaptcha();
      }
    });
  });
</script>
```

## Configuration

You can configure the CAPTCHA behavior in your `application.yml` file:

```yaml
captcha:
  secretKey: YourSecretKey1234567890123456789012 # 32 bytes for AES-256
  expirationMinutes: 10
  captchaType: TEXT # Options: TEXT, MATH, PATTERN
  difficultyLevel: MEDIUM # Options: EASY, MEDIUM, HARD
```

### Configuration Options

| Property | Description | Default | Options |
|----------|-------------|---------|---------|
| secretKey | Secret key for AES encryption | ThisIsASecretKey1234567890123456 | 32-byte string |
| expirationMinutes | CAPTCHA validity period | 10 | Any positive number |
| captchaType | Type of CAPTCHA challenge | TEXT | TEXT, MATH, PATTERN |
| difficultyLevel | Difficulty level | MEDIUM | EASY, MEDIUM, HARD |

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Inspired by various CAPTCHA implementations
- Built with Spring Boot and Kotlin