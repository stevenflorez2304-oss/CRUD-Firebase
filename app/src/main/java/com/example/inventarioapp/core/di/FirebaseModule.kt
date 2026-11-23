package com.example.inventarioapp.core.di

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun auth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun firestore(@ApplicationContext context: Context): FirebaseFirestore =
        FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
}
