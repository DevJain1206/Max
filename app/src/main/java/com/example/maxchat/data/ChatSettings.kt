package com.example.maxchat.data

import android.content.Context
import android.content.SharedPreferences

class ChatSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("maxchat_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "selected_model"
        private const val DEFAULT_MODEL = "google/gemini-2.5-flash"
    }

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var selectedModel: String
        get() = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()
}
