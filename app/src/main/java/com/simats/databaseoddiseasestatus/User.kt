package com.simats.databaseoddiseasestatus

import androidx.compose.runtime.mutableStateListOf

data class User(val email: String, val password: String)

// In a real app, this would be handled by a proper database and authentication service.
val userDatabase = mutableStateListOf<User>(
    User("geethamadhurioddi451@gmail.com", "password")
)

// Tracks users who have already been prompted to save their password.
val promptedUsers = mutableStateListOf<String>()
