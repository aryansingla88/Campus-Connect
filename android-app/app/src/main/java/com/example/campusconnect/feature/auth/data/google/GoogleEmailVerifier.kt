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

            val result = try {

                credentialManager.getCredential(
                    context = context,
                    request = authorizedRequest
                )

            } catch (e: NoCredentialException) {

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

                credentialManager.getCredential(
                    context = context,
                    request = allAccountsRequest
                )
            }

            val credential = result.credential

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

            GoogleEmailVerificationResult.Failure(
                e.message
                    ?: "Google verification failed."
            )

        } catch (e: Exception) {

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