package com.lehau007.todolist.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lehau007.todolist.domain.model.Category
import com.lehau007.todolist.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for category management screen.
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CategoryUiState>(CategoryUiState.Idle)
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()
    
    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()
    
    private val _editingCategory = MutableStateFlow<Category?>(null)
    val editingCategory: StateFlow<Category?> = _editingCategory.asStateFlow()
    
    /**
     * Show the add/edit category dialog.
     */
    fun showAddCategoryDialog() {
        _editingCategory.value = null
        _showDialog.value = true
    }
    
    /**
     * Show the edit dialog for a specific category.
     */
    fun showEditCategoryDialog(category: Category) {
        _editingCategory.value = category
        _showDialog.value = true
    }
    
    /**
     * Hide the add/edit category dialog.
     */
    fun hideDialog() {
        _showDialog.value = false
        _editingCategory.value = null
    }
    
    /**
     * Create a new category.
     */
    fun createCategory(name: String, color: Long, iconName: String) {
        if (name.isBlank()) {
            _uiState.value = CategoryUiState.Error("Category name cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = CategoryUiState.Loading
            try {
                categoryRepository.createCategory(name, color, iconName)
                _uiState.value = CategoryUiState.Success("Category created")
                hideDialog()
            } catch (e: Exception) {
                _uiState.value = CategoryUiState.Error(e.message ?: "Failed to create category")
            }
        }
    }
    
    /**
     * Update an existing category.
     */
    fun updateCategory(id: String, name: String, color: Long, iconName: String, isDefault: Boolean, createdAt: Long, taskCount: Int) {
        if (name.isBlank()) {
            _uiState.value = CategoryUiState.Error("Category name cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = CategoryUiState.Loading
            try {
                val category = Category(
                    id = id,
                    name = name,
                    color = color,
                    iconName = iconName,
                    isDefault = isDefault,
                    createdAt = createdAt,
                    taskCount = taskCount
                )
                categoryRepository.upsertCategory(category)
                _uiState.value = CategoryUiState.Success("Category updated")
                hideDialog()
            } catch (e: Exception) {
                _uiState.value = CategoryUiState.Error(e.message ?: "Failed to update category")
            }
        }
    }
    
    /**
     * Delete a category.
     */
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _uiState.value = CategoryUiState.Loading
            try {
                val deleted = categoryRepository.deleteCategory(categoryId)
                if (deleted) {
                    _uiState.value = CategoryUiState.Success("Category deleted")
                } else {
                    _uiState.value = CategoryUiState.Error("Cannot delete default category")
                }
            } catch (e: Exception) {
                _uiState.value = CategoryUiState.Error(e.message ?: "Failed to delete category")
            }
        }
    }
    
    /**
     * Clear the UI state.
     */
    fun clearUiState() {
        _uiState.value = CategoryUiState.Idle
    }
}

/**
 * UI state for category operations.
 */
sealed class CategoryUiState {
    data object Idle : CategoryUiState()
    data object Loading : CategoryUiState()
    data class Success(val message: String) : CategoryUiState()
    data class Error(val message: String) : CategoryUiState()
}
