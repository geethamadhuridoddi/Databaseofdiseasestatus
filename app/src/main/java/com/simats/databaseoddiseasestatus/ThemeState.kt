package com.simats.databaseoddiseasestatus

import androidx.compose.runtime.mutableStateOf

enum class Theme {
    Light, Dark, System
}

val themeState = mutableStateOf(Theme.System)
