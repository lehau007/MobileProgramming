package com.lehau007.todolist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lehau007.todolist.data.repository.SettingsRepository
import com.lehau007.todolist.domain.model.AppSettings
import com.lehau007.todolist.domain.model.Category
import com.lehau007.todolist.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val currentLanguage: StateFlow<AppSettings.Language> = settingsRepository.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings.Language.ENGLISH)
    
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()
    
    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
    }
    
    fun setLanguage(language: AppSettings.Language) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
            com.lehau007.todolist.util.LocaleHelper.setAppLanguage(language)
        }
    }
    
    fun createCategory(name: String, color: Long, iconName: String) {
        viewModelScope.launch {
            categoryRepository.createCategory(name, color, iconName)
        }
    }
    
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(categoryId)
        }
    }
}
