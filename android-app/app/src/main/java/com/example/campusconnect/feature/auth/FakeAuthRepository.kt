package com.example.campusconnect.feature.auth

import android.util.Log
import kotlin.random.Random

object FakeAuthRepository {
/*In Kotlin, the 'object' keyword is a versatile tool used to define a class
 and create an instance of it at the same time. It is primarily used for creating singletons,
  static-like members, and anonymous classes. */
    private var storedOtp = ""

    private var storedEmail = ""
    private var attemptsLeft = 5

    fun sendOtp(email: String): Boolean {

        storedEmail = email

        storedOtp =
            Random.nextInt(
                100000,
                999999
            ).toString()

        Log.d(
            "OTP_DEBUG",
            "OTP for $email : $storedOtp"
        )
        attemptsLeft = 5
        return true
    }

    fun verifyOtp(

        email: String,

        enteredOtp: String

    ): VerifyOtpResult {

        if (attemptsLeft <= 0) {

            return VerifyOtpResult.Expired
        }

        val isCorrect = (

                email == storedEmail &&

                        enteredOtp == storedOtp
                )

        if (isCorrect) {

            return VerifyOtpResult.Success
        }

        attemptsLeft--

        return if (attemptsLeft <= 0) {

            VerifyOtpResult.Expired

        } else {

            VerifyOtpResult.Failure(
                attemptsLeft
            )
        }
    }
}