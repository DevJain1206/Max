package com.example.maxchat.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object OpenRouterClient {
    private const val API_URL = "https://openrouter.ai/api/v1/chat/completions"

    suspend fun chatCompletion(
        apiKey: String,
        model: String,
        messages: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val url = URL(API_URL)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.doOutput = true
            
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("HTTP-Referer", "https://github.com/example/maxchat")
            connection.setRequestProperty("X-Title", "MaxChat")

            // Construct JSON request body
            val requestJson = JSONObject()
            requestJson.put("model", model)
            requestJson.put("max_tokens", 2048)
            
            val messagesArray = JSONArray()
            for (msg in messages) {
                val msgJson = JSONObject()
                msgJson.put("role", msg.role)
                msgJson.put("content", msg.content)
                messagesArray.put(msgJson)
            }
            requestJson.put("messages", messagesArray)

            // Write output stream
            val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
            writer.use {
                it.write(requestJson.toString())
                it.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val response = StringBuilder()
                reader.use {
                    var line: String?
                    while (it.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                }
                
                val responseJson = JSONObject(response.toString())
                val choices = responseJson.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val messageObj = firstChoice.optJSONObject("message")
                    if (messageObj != null) {
                        return@withContext messageObj.optString("content", "")
                    }
                }
                throw Exception("Unexpected empty response from OpenRouter API")
            } else {
                // Read error stream
                val errorStream = connection.errorStream
                val response = StringBuilder()
                if (errorStream != null) {
                    val reader = BufferedReader(InputStreamReader(errorStream, "UTF-8"))
                    reader.use {
                        var line: String?
                        while (it.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                    }
                }
                
                var errorMessage = "HTTP $responseCode"
                if (response.isNotEmpty()) {
                    try {
                        val errorJson = JSONObject(response.toString())
                        val errorObj = errorJson.optJSONObject("error")
                        if (errorObj != null) {
                            errorMessage = errorObj.optString("message", errorMessage)
                        } else {
                            val msg = errorJson.optString("message")
                            if (msg.isNotEmpty()) {
                                errorMessage = msg
                            }
                        }
                    } catch (e: Exception) {
                        if (response.length < 200) {
                            errorMessage = "$errorMessage: ${response.toString().trim()}"
                        }
                    }
                }
                throw Exception(errorMessage)
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun validateApiKey(apiKey: String): Boolean = withContext(Dispatchers.IO) {
        val url = URL("https://openrouter.ai/api/v1/auth/key")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return@withContext true
            } else {
                throw Exception("Invalid API Key (HTTP $responseCode)")
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun fetchBestModel(apiKey: String): String = withContext(Dispatchers.IO) {
        val url = URL("https://openrouter.ai/api/v1/models")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Authorization", "Bearer $apiKey")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val response = StringBuilder()
                reader.use {
                    var line: String?
                    while (it.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                }

                val json = JSONObject(response.toString())
                val dataArray = json.optJSONArray("data")
                val availableIds = mutableListOf<String>()
                if (dataArray != null) {
                    for (i in 0 until dataArray.length()) {
                        val modelObj = dataArray.getJSONObject(i)
                        val id = modelObj.optString("id", "")
                        if (id.isNotEmpty()) {
                            availableIds.add(id)
                        }
                    }
                }

                val preferences = listOf(
                    "google/gemini-2.5-flash",
                    "meta-llama/llama-3-8b-instruct:free",
                    "deepseek/deepseek-chat",
                    "google/gemini-2.5-pro"
                )

                for (pref in preferences) {
                    if (availableIds.contains(pref)) {
                        return@withContext pref
                    }
                }

                val freeModel = availableIds.firstOrNull { it.endsWith(":free") }
                if (freeModel != null) {
                    return@withContext freeModel
                }

                if (availableIds.isNotEmpty()) {
                    return@withContext availableIds.first()
                }
            }
            return@withContext "google/gemini-2.5-flash"
        } catch (e: Exception) {
            return@withContext "google/gemini-2.5-flash"
        } finally {
            connection.disconnect()
        }
    }
}
