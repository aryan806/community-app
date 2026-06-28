package com.communityheroai.ui.screens // FIXED: 19

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.communityheroai.DependencyProvider
import com.communityheroai.data.Issue
import com.communityheroai.data.IssueRepository
import com.communityheroai.data.IssueDraft
import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class AiSuggestion(val category: String, val severity: String, val summary: String) // FIXED: 6

class ReportViewModel(private val issueRepository: IssueRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null) // FIXED: 21
    val error = _error.asStateFlow()

    private val _aiSuggestion = MutableStateFlow<AiSuggestion?>(null) // FIXED: 6
    val aiSuggestion = _aiSuggestion.asStateFlow()

    fun submitIssue(issue: Issue, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            issueRepository.reportIssue(issue, imageUri)
                .onSuccess { _isSuccess.value = true }
                .onFailure { _error.value = it.message } // FIXED: 21
            _isLoading.value = false
        }
    }

    fun saveDraft(title: String, description: String, category: String, severity: String, locationName: String, lat: Double, lng: Double) { // FIXED: 7
        viewModelScope.launch {
            DependencyProvider.draftRepository.saveDraft(
                IssueDraft(title = title, description = description, category = category, severity = severity, locationName = locationName, locationLat = lat, locationLng = lng)
            )
        }
    }

    fun suggestCategoryAndSeverity(description: String) { // FIXED: 6
        if (description.isBlank()) return
        viewModelScope.launch {
            try {
                val generativeModel = Firebase.vertexAI.generativeModel("gemini-2.0-flash")
                val prompt = "Analyze this issue description: '$description'. Classify it into one of these categories: [Potholes, Garbage, Streetlights, Water Leakage, Sewage, Traffic Signals, Public Safety, Other]. Determine its severity (Low, Medium, High, Critical). Provide a short summary. Respond ONLY in valid JSON format: {\"category\":\"...\",\"severity\":\"...\",\"summary\":\"...\"}"
                val response = generativeModel.generateContent(prompt)
                val text = response.text?.removePrefix("```json")?.removeSuffix("```")?.trim() ?: return@launch
                val json = JSONObject(text)
                _aiSuggestion.value = AiSuggestion(
                    category = json.getString("category"),
                    severity = json.getString("severity"),
                    summary = json.getString("summary")
                )
            } catch (e: Exception) {
                // Ignore AI failure
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ReportViewModel(DependencyProvider.issueRepository) as T
        }
    }
}
