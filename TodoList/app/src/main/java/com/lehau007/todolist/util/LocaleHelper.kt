package com.lehau007.todolist.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.lehau007.todolist.domain.model.AppSettings

/**
 * Helper class for managing app locale/language.
 */
object LocaleHelper {
    
    /**
     * Apply the selected language using AppCompatDelegate.
     */
    fun setAppLanguage(language: AppSettings.Language) {
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
