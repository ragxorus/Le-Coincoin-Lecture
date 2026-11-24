package com.example.coincoinlecture.data

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.coincoinlecture.R

class AuthRepository(
    private val auth: FirebaseAuth = Firebase.auth
) {
    val userId: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    //renvoie l'id de l'utilisateur connecte
    val currentUserId: String?
        get() = auth.currentUser?.uid

    //renvoie le mail de l'utilisateur connecte
    val currentUserEmail: String?
        get() = auth.currentUser?.email

    //gere la connection avec google
    fun signInWithGoogle(activityContext: Context, onFailure: (String) -> Unit) {
        val option = GetSignInWithGoogleOption.Builder(
            activityContext.getString(R.string.default_web_client_id)
        ).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        CredentialManager.create(activityContext).getCredentialAsync(
            activityContext,
            request,
            null,
            ContextCompat.getMainExecutor(activityContext),
            object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                override fun onResult(result: GetCredentialResponse) {
                    try {
                        val idToken = GoogleIdTokenCredential.createFrom(result.credential.data).idToken
                        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                            .addOnFailureListener { onFailure(it.message ?: "Connexion impossible") }
                    } catch (e: Exception) {
                        onFailure(e.message ?: "Connexion impossible")
                    }
                }

                override fun onError(e: GetCredentialException) {
                    onFailure(e.message ?: "Connexion impossible")
                }
            }
        )
    }

    //permet de se deconnecter
    fun signOut() = auth.signOut()
}
