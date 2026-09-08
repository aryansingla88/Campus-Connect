# Campus Connect — Password Reset Feature Documentation
## 1. Overview

The Password Reset feature provides a secure mechanism for users who have forgotten their password or need to reset it.

The implementation follows a three-stage authentication flow:

Request OTP → Verify OTP → Reset Password

The system uses two different temporary credentials:

A 6-digit OTP for proving ownership of the registered email.
A cryptographically secure reset token for authorizing the actual password change.

The feature is designed with the following security controls:

OTP expiry
Maximum verification attempts
OTP resend cooldown
Old OTP invalidation
Hashed OTP storage
Opaque reset tokens
Hashed reset-token storage
Reset-token expiry
Single-use reset tokens
SMTP-based email delivery
No hardcoded email credentials

## 2. Complete Flow

User forgets password
        │
        ▼
POST /auth/password-reset/request
        │
        ▼
Backend generates 6-digit OTP
        │
        ├── Hashes OTP using BCrypt
        │
        ├── Stores hash in database
        │
        └── Sends raw OTP to user's email
                │
                ▼
User enters OTP
                │
                ▼
POST /auth/password-reset/verify
                │
                ├── Validate expiry
                ├── Validate attempt count
                └── BCrypt.matches()
                        │
                        ▼
                OTP verified
                        │
                        ▼
          Generate secure reset token
                        │
                        ├── Return raw token to frontend
                        │
                        └── Store SHA-256 hash in DB
                                │
                                ▼
User enters new password
                                │
                                ▼
POST /auth/password-reset/reset
                                │
                                ├── Hash submitted token
                                ├── Find DB record
                                ├── Check expiry
                                ├── Check if already used
                                └── Update password
                                        │
                                        ▼
                               Mark token as used
                                        │
                                        ▼
                                Password reset complete
## 3. Database Design

The feature uses a dedicated table:

CREATE TABLE password_reset_tokens (

    id SERIAL PRIMARY KEY,

    user_id INTEGER NOT NULL,

    otp_hash VARCHAR(255) NOT NULL,

    expires_at TIMESTAMP NOT NULL,

    attempt_count INTEGER NOT NULL DEFAULT 0,

    otp_verified_at TIMESTAMP,

    reset_token_hash VARCHAR(255),

    reset_token_expires_at TIMESTAMP,

    used_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

Indexes:

CREATE INDEX idx_password_reset_tokens_user_id
ON password_reset_tokens(user_id);

CREATE INDEX idx_password_reset_tokens_expires_at
ON password_reset_tokens(expires_at);

CREATE INDEX idx_password_reset_tokens_reset_token_hash
ON password_reset_tokens(reset_token_hash);
3.1 Purpose of Each Column
Column	Purpose
id	Unique identifier for the reset request
user_id	User requesting password reset
otp_hash	BCrypt hash of the OTP
expires_at	OTP expiry time
attempt_count	Number of OTP verification attempts
otp_verified_at	Timestamp when OTP was successfully verified
reset_token_hash	SHA-256 hash of generated reset token
reset_token_expires_at	Expiry time of reset token
used_at	Timestamp when reset token was consumed
created_at	Timestamp when reset request was created
## 4. Why a Separate Password Reset Table?

Password reset is a temporary authentication workflow and should not be mixed directly into the users table.

A dedicated table allows the system to manage:

OTP lifecycle
Expiry
Attempt limits
Verification state
Reset authorization
Single-use token tracking

This keeps the User entity focused on permanent account data while temporary authentication state remains isolated.

## 5. PasswordResetToken Entity

The database table is mapped using:

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "otp_verified_at")
    private LocalDateTime otpVerifiedAt;

    @Column(name = "reset_token_hash")
    private String resetTokenHash;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

The entity acts as the complete server-side representation of a password-reset session.

## 6. Stage 1 — Request Password Reset
Endpoint
POST /auth/password-reset/request
Request
{
    "email": "student@example.com"
}
6.1 User Identification

The backend identifies the user using:

userRepository.findByUsernameOrEmail(
    request.getEmail(),
    request.getEmail()
)

This retrieves the user associated with the provided identifier.

If the user does not exist:

User not found.
## 7. OTP Generation

A cryptographically secure random number generator is used:

private String generateOtp() {

    SecureRandom secureRandom = new SecureRandom();

    int otp = 100000 + secureRandom.nextInt(900000);

    return String.valueOf(otp);
}

This produces a random 6-digit OTP:

482913

The OTP is never stored directly.

## 8. Why SecureRandom?

Normal random generators are not designed for security-sensitive operations.

SecureRandom is specifically intended for generating unpredictable values.

Therefore:

Password Reset
       ↓
Security-sensitive operation
       ↓
SecureRandom

This makes OTP generation significantly harder to predict.

## 9. OTP Hashing

Before storing the OTP:

String otpHash = passwordEncoder.encode(otp);

The resulting BCrypt hash is stored instead of the original OTP.

Conceptually:

Raw OTP
482913
   │
   ▼
 BCrypt
   │
   ▼
$2a$10$.........
   │
   ▼
Database

The database never contains:

otp = 482913

Instead:

otp_hash = $2a$10$...
## 10. OTP Expiry

The OTP is valid for:

LocalDateTime.now().plusMinutes(10)

Therefore:

OTP generated
     │
     ▼
Valid for 10 minutes
     │
     ▼
Expires

Once expired, OTP verification must fail.

This limits the time window in which a stolen OTP could be abused.

## 11. OTP Request Cooldown

The implementation prevents users from repeatedly requesting OTPs.

Before generating a new OTP, the backend checks the latest request:

Optional<PasswordResetToken> latestToken =
        passwordResetTokenRepository
                .findTopByUserOrderByCreatedAtDesc(user);

Then:

lastRequestTime
    .plusSeconds(60)
    .isAfter(LocalDateTime.now())

If less than 60 seconds have passed:

Please wait before requesting another OTP.

Therefore:

OTP Request
    │
    ▼
Wait 60 seconds
    │
    ▼
Next OTP allowed

This prevents excessive OTP generation and email spam.

## 12. Old OTP Invalidation

When a new OTP is requested, the old reset record is removed:

passwordResetTokenRepository.deleteByUser(user);

Then a new record is created.

Therefore:

OTP A generated
      │
      ▼
User requests new OTP
      │
      ▼
OTP A invalidated
      │
      ▼
OTP B generated

Only the latest OTP remains valid.

This prevents multiple simultaneously active OTPs.

## 13. Sending OTP Through Email

Email delivery is handled by:

EmailService

The service uses:

JavaMailSender

and SMTP configuration.

Example:

mailSender.send(message);

The email contains the raw OTP because the user needs to enter it manually.

The database only stores the hashed version.

## 14. SMTP Configuration

The application uses environment variables:

spring.mail.host=${MAIL_HOST}
spring.mail.port=${MAIL_PORT}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

Additional SMTP security:

spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

This ensures credentials are not hardcoded inside source code.

Conceptually:

Source Code
    │
    └── No email credentials

Environment Variables
    │
    ├── MAIL_HOST
    ├── MAIL_PORT
    ├── MAIL_USERNAME
    └── MAIL_PASSWORD

This makes deployment safer and allows different credentials across development and production environments.

## 15. Stage 2 — Verify OTP
Endpoint
POST /auth/password-reset/verify
Request
{
    "email": "student@example.com",
    "otp": "482913"
}
15.1 Verification Process

The backend performs the following steps:

Receive Email + OTP
        │
        ▼
Find User
        │
        ▼
Find PasswordResetToken
        │
        ▼
Check expiry
        │
        ▼
Check attempt count
        │
        ▼
BCrypt.matches()
        │
        ├── Incorrect → Increase attempts
        │
        └── Correct → Generate reset token
## 16. Maximum OTP Attempts

The maximum allowed attempts are:

5 attempts

Every incorrect OTP increments:

attempt_count

Example:

Attempt 1 → Wrong
Attempt 2 → Wrong
Attempt 3 → Wrong
Attempt 4 → Wrong
Attempt 5 → Wrong

After the limit is reached, further verification is rejected.

This protects against brute-force attempts.

## 17. OTP Verification Using BCrypt

The OTP is verified using:

passwordEncoder.matches(
    submittedOtp,
    storedOtpHash
);

Important: BCrypt does not generate the same hash every time.

For example:

BCrypt("123456")
→ Hash A

BCrypt("123456")
→ Hash B

Therefore direct database searching by BCrypt hash is impractical.

Instead:

Email
  ↓
Find User
  ↓
Find Reset Record
  ↓
Get Stored OTP Hash
  ↓
BCrypt.matches()

The record is already known before verification.

## 18. Successful OTP Verification

Once the OTP is verified:

otpVerifiedAt = LocalDateTime.now();

The system then generates a reset token.

The OTP's role is complete.

The reset token becomes the temporary credential required to authorize the password change.

## 19. Reset Token Generation

The reset token is generated using:

SecureRandom secureRandom = new SecureRandom();

byte[] tokenBytes = new byte[32];

secureRandom.nextBytes(tokenBytes);

The random bytes are encoded:

Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(tokenBytes);

Example:

wz-lDE4wqK8ttbIKuIJUwbOwqm0dY3BHDjT6LnN2Ewc

The token contains:

32 random bytes
=
256 bits of randomness

This provides extremely high entropy.

## 20. Why Use an Opaque Reset Token?

The reset token contains no meaningful user information.

It does not expose:

User ID
Email
Password
Role
JWT claims

It is simply a random authorization credential.

Therefore it is called an opaque token.

Conceptually:

Random Secret
     │
     ▼
Authorization Credential
     │
     ▼
Server understands its meaning

The frontend treats it as an opaque string.

## 21. Why SHA-256 for the Reset Token?

The reset token is handled differently from passwords and OTPs.

The backend needs to find the database record corresponding to the incoming token.

The reset request contains:

{
    "resetToken": "...",
    "newPassword": "..."
}

The backend must answer:

Which password reset record belongs to this token?

For this purpose, deterministic hashing is useful.

## 22. SHA-256 is Deterministic

For the same input:

SHA256(token)

always produces:

Same hash

Conceptually:

Original Token
      │
      ▼
   SHA-256
      │
      ▼
Hash A


Same Token
      │
      ▼
   SHA-256
      │
      ▼
Hash A

Therefore the backend can perform deterministic lookup.

## 23. Reset Token Storage

After generating the raw token:

String rawToken = generateResetToken();

The backend hashes it:

String tokenHash = hashResetToken(rawToken);

Only the hash is stored:

Database:

reset_token_hash = SHA256(rawToken)

The raw token is returned to the frontend.

{
    "resetToken": "wz-lDE4wqK8ttbIKuIJUwbOwqm0dY3BHDjT6LnN2Ewc"
}

The raw token is never stored permanently in the database.

## 24. Why Not Store the Raw Reset Token?

Suppose the database stored:

reset_token = wz-lDE4...

If the database leaked, an attacker could potentially use active reset tokens.

Instead, the database contains:

reset_token_hash = a9f8c21d...

The attacker cannot directly use the hash as a reset token.

Therefore the system follows the principle:

Authentication secrets should not be stored in plaintext when hashing is practical.

## 25. Why Not Use BCrypt for the Reset Token?

BCrypt produces a different hash every time because of its random salt.

Example:

Token
  │
  ▼
BCrypt
  │
  ▼
Hash A


Same Token
  │
  ▼
BCrypt
  │
  ▼
Hash B

Therefore:

Hash A ≠ Hash B

The backend cannot reliably do:

findByResetTokenHash(
    passwordEncoder.encode(submittedToken)
);

because the newly generated BCrypt hash will differ from the stored hash.

SHA-256 solves this because it is deterministic.

## 26. Reset Token Database Lookup

When the frontend sends the token back:

Raw Reset Token
        │
        ▼
     SHA-256
        │
        ▼
Find matching reset_token_hash
        │
        ▼
Retrieve PasswordResetToken record

Conceptually:

SELECT *
FROM password_reset_tokens
WHERE reset_token_hash = ?;

The repository supports this:

Optional<PasswordResetToken>
findByResetTokenHash(String resetTokenHash);
## 27. Why the Reset Token Hash is Indexed

The database includes:

CREATE INDEX idx_password_reset_tokens_reset_token_hash
ON password_reset_tokens(reset_token_hash);

This allows efficient lookup:

Incoming Token
      │
      ▼
SHA-256
      │
      ▼
Indexed Database Lookup
      │
      ▼
Correct Reset Record

This is particularly useful because the reset endpoint identifies the reset session through the token itself.

## 28. BCrypt vs SHA-256 — Core Difference
Credential	Hashing Algorithm	Reason
Password	BCrypt	Human-chosen and vulnerable to brute force
OTP	BCrypt	Low entropy, but record is already identified
Reset Token	SHA-256	High entropy and requires deterministic lookup

The simplest way to remember the design is:

BCrypt

"I already know the record. Does this submitted secret match?"

Used for:

Passwords
OTPs
SHA-256

"I have this token. Which database record belongs to it?"

Used for:

Reset tokens
## 29. Stage 3 — Reset Password
Endpoint
POST /auth/password-reset/reset
Request
{
    "resetToken": "wz-lDE4wqK8ttbIKuIJUwbOwqm0dY3BHDjT6LnN2Ewc",
    "newPassword": "NewPassword123"
}
## 30. Reset Password Execution

The backend performs:

Receive reset token
        │
        ▼
SHA-256(token)
        │
        ▼
Find PasswordResetToken
        │
        ▼
Does token exist?
        │
        ├── No → Invalid token
        │
        ▼
Has token expired?
        │
        ├── Yes → Reject
        │
        ▼
Has token already been used?
        │
        ├── Yes → Reject
        │
        ▼
Hash new password with BCrypt
        │
        ▼
Update user's password_hash
        │
        ▼
Set used_at timestamp
        │
        ▼
Success
## 31. Password Storage

The new password is never stored directly.

The backend performs:

String hashedPassword =
        passwordEncoder.encode(
                request.getNewPassword()
        );

Then updates:

users.password_hash

Conceptually:

New Password
      │
      ▼
   BCrypt
      │
      ▼
Password Hash
      │
      ▼
Database
## 32. Single-Use Reset Tokens

After a successful password reset:

usedAt = LocalDateTime.now();

This permanently marks the reset token as consumed.

Therefore:

Valid Token
    │
    ▼
Reset Password
    │
    ▼
used_at = current time
    │
    ▼
Token cannot be reused

Even if someone retains the token, it no longer authorizes another password change.

## 33. Security Controls Summary
Security Measure	Implementation
Secure OTP generation	SecureRandom
OTP length	6 digits
OTP storage	BCrypt hash
OTP expiry	10 minutes
OTP attempt limit	5 attempts
OTP resend cooldown	60 seconds
Old OTP invalidation	Previous record deleted
Email delivery	SMTP
Email credentials	Environment variables
Reset token randomness	32 bytes / 256 bits
Reset token storage	SHA-256 hash
Reset token lookup	Indexed hash lookup
Reset token expiry	Server-side timestamp
Token reuse prevention	used_at timestamp
Password storage	BCrypt
## 34. API Summary
1. Request OTP
Endpoint
POST /auth/password-reset/request
Request
{
    "email": "student@example.com"
}
Response
{
    "success": true,
    "data": null,
    "message": "Password reset OTP generated successfully."
}
2. Verify OTP
Endpoint
POST /auth/password-reset/verify
Request
{
    "email": "student@example.com",
    "otp": "482913"
}
Response
{
    "success": true,
    "data": {
        "resetToken": "wz-lDE4wqK8ttbIKuIJUwbOwqm0dY3BHDjT6LnN2Ewc"
    },
    "message": "OTP verified successfully."
}
3. Reset Password
Endpoint
POST /auth/password-reset/reset
Request
{
    "resetToken": "wz-lDE4wqK8ttbIKuIJUwbOwqm0dY3BHDjT6LnN2Ewc",
    "newPassword": "NewPassword123"
}
Response
{
    "success": true,
    "data": null,
    "message": "Password reset successfully."
}