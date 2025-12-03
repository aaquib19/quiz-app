package com.example.quizapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.data.QuizRepository
import com.example.quizapp.ui.theme.ModuleListUiState
import com.example.quizapp.ui.theme.ModuleWithProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ModuleListViewModel(private val repository: QuizRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ModuleListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadModules()
    }

    private fun loadModules() {
        viewModelScope.launch {
            try {
                val modules = repository.getModules()

                // Collect progress for all modules
                repository.getAllProgress().collect { progressList ->
                    val modulesWithProgress = modules.map { module ->
                        val progress = progressList.find { it.moduleId == module.id }
                        ModuleWithProgress(module, progress)
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            modules = modulesWithProgress
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load modules"
                    )
                }
            }
        }
    }

    fun refreshModules() {
        _uiState.update { it.copy(isLoading = true) }
        loadModules()
    }
}