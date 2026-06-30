package com.example.maxchat.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.maxchat.data.ChatMessage
import com.example.maxchat.data.ChatSettings
import com.example.maxchat.data.OpenRouterClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = ChatSettings(application)
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        _uiState.update { 
            it.copy(
                apiKey = settings.apiKey,
                selectedModel = settings.selectedModel
            )
        }
    }

    fun saveSettings(apiKey: String, model: String) {
        settings.apiKey = apiKey
        settings.selectedModel = model
        _uiState.update {
            it.copy(
                apiKey = apiKey,
                selectedModel = model,
                error = null
            )
        }
    }

    fun connectApiKey(apiKey: String, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Validate key
                OpenRouterClient.validateApiKey(apiKey)
                // 2. Fetch best available model
                val bestModel = OpenRouterClient.fetchBestModel(apiKey)
                // 3. Save to SharedPreferences
                saveSettings(apiKey, bestModel)
                
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to connect API Key"
                    )
                }
            }
        }
    }

    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return

        val userMessage = ChatMessage(role = "user", content = trimmed)
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(userMessage)

        _uiState.update {
            it.copy(
                messages = currentMessages,
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val apiKey = _uiState.value.apiKey
                val model = _uiState.value.selectedModel
                
                val reply = OpenRouterClient.chatCompletion(
                    apiKey = apiKey,
                    model = model,
                    messages = currentMessages
                )

                val assistantMessage = ChatMessage(role = "assistant", content = reply)
                _uiState.update {
                    it.copy(
                        messages = it.messages + assistantMessage,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Unknown network error"
                    )
                }
            }
        }
    }

    fun clearHistory() {
        _uiState.update {
            it.copy(
                messages = emptyList(),
                error = null
            )
        }
    }
}

data class ChatUiState(
    val apiKey: String = "",
    val selectedModel: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
