package com.example.quizapp.ui.theme

import com.example.quizapp.data.Module
import com.example.quizapp.data.local.ModuleProgressEntity

data class ModuleWithProgress(
    val module: Module,
    val progress: ModuleProgressEntity?
)

data class ModuleListUiState(
    val isLoading: Boolean = true,
    val modules: List<ModuleWithProgress> = emptyList(),
    val error: String? = null
)