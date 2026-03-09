package com.handyteejay.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.handyteejay.data.firebase.FirebaseAuthRepository
import com.handyteejay.data.firebase.FirebaseRideRepository
import com.handyteejay.data.firebase.FirebaseUserRepository
import com.handyteejay.data.repository.AuthRepository
import com.handyteejay.data.repository.RideRepository
import com.handyteejay.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = FirebaseAuthRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): UserRepository = FirebaseUserRepository(firestore, auth)

    @Provides
    @Singleton
    fun provideRideRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        messaging: FirebaseMessaging
    ): RideRepository = FirebaseRideRepository(firestore, auth, mess
aging)
}
