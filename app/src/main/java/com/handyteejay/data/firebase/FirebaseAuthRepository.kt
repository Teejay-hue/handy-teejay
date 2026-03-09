package com.handyteejay.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.handyteejay.data.model.User
import com.handyteejay.data.model.UserType
import com.handyteejay.data.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                firestore.collection("users").document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toObject(User::class.java)
                        trySend(user)
                    }
                    .addOnFailureListener {
                        trySend(null)
                    }
            } else {
                trySend(null)
            }
        }
        
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        name: String,
        phone: String,
        userType: UserType
    ): Result<User> = try {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user!!
        
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        firebaseUser.updateProfile(profileUpdates).await()
        
        val user = User(
            id = firebaseUser.uid,
            email = email,
            name = name,
            phone = phone,
            userType = userType
        )
        
        firestore.collection("users").document(firebaseUser.uid)
            .set(user)
            .await()
        
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signIn(email: String, password: String): Result<User> = try {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user!!
        
        val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
        val user = doc.toObject(User::class.java)
            ?: throw Exception("User data not found")
        
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun resetPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProfile(user: User): Result<Unit> = try {
        firestore.collection("users").document(user.id)
            .set(user)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
