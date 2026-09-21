package com.example.tailtrace

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore(name = "tailtrace_prefs")

data class Settings(
    val token: String? = null,
    val userId: String? = null,
    val name: String = "",
    val phone: String = "",
    val dark: Boolean = false,
    val notify: Boolean = true,
    val radius: Int = 10,
)

class Prefs(private val ctx: Context) {
    companion object {
        val TOKEN = stringPreferencesKey("token")
        val USER_ID = stringPreferencesKey("user_id")
        val NAME = stringPreferencesKey("name")
        val PHONE = stringPreferencesKey("phone")
        val DARK = booleanPreferencesKey("dark")
        val NOTIFY = booleanPreferencesKey("notify")
        val RADIUS = intPreferencesKey("radius")
    }

    val settings: Flow<Settings> = ctx.store.data.map { p ->
        Settings(p[TOKEN], p[USER_ID], p[NAME] ?: "", p[PHONE] ?: "", p[DARK] ?: false, p[NOTIFY] ?: true, p[RADIUS] ?: 10)
    }

    suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        ctx.store.edit { it[key] = value }
    }

    suspend fun saveSession(r: AuthResponse) {
        ctx.store.edit {
            it[TOKEN] = r.token
            it[USER_ID] = r.user.id
            it[NAME] = r.user.fullName
            it[PHONE] = r.user.phone ?: ""
        }
    }

    suspend fun clearSession() {
        ctx.store.edit {
            it.remove(TOKEN)
            it.remove(USER_ID)
            it.remove(NAME)
            it.remove(PHONE)
        }
    }
}