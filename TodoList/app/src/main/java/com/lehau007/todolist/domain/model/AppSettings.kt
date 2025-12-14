package com.lehau007.todolist.domain.model

/**
 * App settings model.
 */
data class AppSettings(
    val language: Language = Language.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
) {
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        VIETNAMESE("vi", "Tiếng Việt")
    }
    
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
}
