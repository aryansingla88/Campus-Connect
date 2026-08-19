package com.example.campusconnect.feature.auth.data.google

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.campusconnect.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.SecureRandom
import android.util.Base64
import android.util.Log

sealed class GoogleEmailVerificationResult {

    data class Success(
        val email: String,
        val idToken: String
    ) : GoogleEmailVerificationResult()

    data class Failure(
        val message: String
    ) : GoogleEmailVerificationResult()

    data object Cancelled : GoogleEmailVerificationResult()
}

object GoogleEmailVerifier {

    suspend fun verify(
        context: Context
    ): GoogleEmailVerificationResult {

        Log.d("GOOGLE_FLOW", "5. GoogleEmailVerifier.verify() started")

        val credentialManager =
            CredentialManager.create(context)

        return try {

            val authorizedOption =
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(
                        context.getString(
                            R.string.google_web_client_id
                        )
                    )
                    .setAutoSelectEnabled(false)
                    .setNonce(generateNonce())
                    .build()

            val authorizedRequest =
                GetCredentialRequest.Builder()
                    .addCredentialOption(authorizedOption)
                    .build()

            Log.d(
                "GOOGLE_FLOW",
                "6. Calling getCredential() with authorized accounts"
            )

            val result = try {

                credentialManager.getCredential(
                    context = context,
                    request = authorizedRequest
                )

            } catch (e: NoCredentialException) {

                Log.d(
                    "GOOGLE_FLOW",
                    "7. No authorized credential, trying ALL accounts"
                )



                val allAccountsOption =
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(
                            context.getString(
                                R.string.google_web_client_id
                            )
                        )
                        .setAutoSelectEnabled(false)
                        .setNonce(generateNonce())
                        .build()

                val allAccountsRequest =
                    GetCredentialRequest.Builder()
                        .addCredentialOption(allAccountsOption)
                        .build()

                Log.d(
                    "GOOGLE_FLOW",
                    "8. Calling getCredential() with all accounts"
                )

                credentialManager.getCredential(
                    context = context,
                    request = allAccountsRequest
                )
            }

            Log.d(
                "GOOGLE_FLOW",
                "9. Credential received successfully"
            )

            val credential = result.credential

            Log.d(
                "GOOGLE_FLOW",
                "10. Credential type = ${credential.type}"
            )

            if (
                credential is CustomCredential &&
                credential.type ==
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {

                val googleCredential =
                    try {

                        GoogleIdTokenCredential
                            .createFrom(
                                credential.data
                            )

                    } catch (e: GoogleIdTokenParsingException) {

                        return GoogleEmailVerificationResult.Failure(
                            "Unable to read Google account information."
                        )
                    }

                val email =
                    googleCredential.id

                if (email.isNullOrBlank()) {

                    return GoogleEmailVerificationResult.Failure(
                        "Google did not provide an email address."
                    )
                }

                return GoogleEmailVerificationResult.Success(
                    email = email,
                    idToken = googleCredential.idToken
                )
            }

            GoogleEmailVerificationResult.Failure(
                "Unexpected Google credential received."
            )

        } catch (e: GetCredentialException) {

            Log.e(
                "GOOGLE_FLOW",
                "ERROR: GetCredentialException",
                e
            )

            GoogleEmailVerificationResult.Failure(
                e.message
                    ?: "Google verification failed."
            )

        } catch (e: Exception) {
            Log.e(
                "GOOGLE_FLOW",
                "ERROR: Unexpected exception",
                e
            )

            GoogleEmailVerificationResult.Failure(
                e.message
                    ?: "Google verification failed."
            )
        }
    }

    private fun generateNonce(): String {

        val bytes = ByteArray(32)

        SecureRandom().nextBytes(bytes)

        return Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or
                    Base64.NO_WRAP or
                    Base64.NO_PADDING
        )
    }
}