package com.example.maxchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.maxchat.theme.MaxChatTheme
import com.example.maxchat.ui.chat.ApiKeySetupScreen
import com.example.maxchat.ui.chat.ChatScreen
import com.example.maxchat.ui.chat.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaxChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent(viewModel: ChatViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var forceShowSettings by remember { mutableStateOf(false) }

    LaunchedEffect(state.apiKey) {
        if (state.apiKey.isNotEmpty()) {
            viewModel.loadSettings()
        }
    }

    if (state.apiKey.isEmpty() || forceShowSettings) {
        ApiKeySetupScreen(
            currentApiKey = state.apiKey,
            isLoading = state.isLoading,
            error = state.error,
            onConnect = { key ->
                viewModel.connectApiKey(key, onSuccess = {
                    forceShowSettings = false
                })
            }
        )
    } else {
        ChatScreen(
            state = state,
            onSendMessage = { text -> viewModel.sendMessage(text) },
            onClearHistory = { viewModel.clearHistory() },
            onNavigateToSettings = { forceShowSettings = true }
        )
    }
}
