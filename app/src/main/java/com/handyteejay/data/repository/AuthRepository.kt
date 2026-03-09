package com.handyteejay.data.repository

import com.handyteejay.data.model.User
import com.handyteejay.data.model.UserType
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signUp(email: String, password: String, name: String, phone: String, userType: UserType): Result<User>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signOut()
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun updateProfile(user: User): Result<Unit>

}
