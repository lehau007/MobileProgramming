package com.lehau007.todolist.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lehau007.todolist.domain.model.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for app settings using DataStore.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
    
    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            language = AppSettings.Language.entries.find { 
                it.code == preferences[PreferencesKeys.LANGUAGE] 
            } ?: AppSettings.Language.ENGLISH,
            themeMode = AppSettings.ThemeMode.entries.find { 
                it.name == preferences[PreferencesKeys.THEME_MODE] 
            } ?: AppSettings.ThemeMode.SYSTEM
        )
    }
    
    val language: Flow<AppSettings.Language> = context.dataStore.data.map { preferences ->
        AppSettings.Language.entries.find { 
            it.code == preferences[PreferencesKeys.LANGUAGE] 
        } ?: AppSettings.Language.ENGLISH
    }
    
    suspend fun setLanguage(language: AppSettings.Language) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language.code
        }
    }
    
    suspend fun setThemeMode(themeMode: AppSettings.ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }
}
