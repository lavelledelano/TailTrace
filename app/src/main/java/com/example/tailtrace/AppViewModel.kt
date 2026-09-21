package com.example.tailtrace

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.Preferences

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val api = ApiClient.api

    /** null means still loading from the phone's storage */
    val settings: StateFlow<Settings?> = prefs.settings
        .onEach { ApiClient.token = it.token }
        .map<Settings, Settings?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private fun launchApi(block: suspend () -> Unit) = viewModelScope.launch {
        loading = true
        error = null
        try {
            block()
        } catch (e: Exception) {
            error = e.friendly()
        } finally {
            loading = false
        }
    }

    fun clearError() { error = null }

    fun login(email: String, password: String) = launchApi {
        prefs.saveSession(api.login(LoginRequest(email, password)))
    }

    fun register(name: String, email: String, password: String, phone: String) = launchApi {
        prefs.saveSession(api.register(RegisterRequest(name, email, password, phone)))
    }

    fun logout() {
        viewModelScope.launch { prefs.clearSession() }
    }
    // ---- location and pets ----
    var lat by mutableStateOf(DEFAULT_LAT)
        private set
    var lng by mutableStateOf(DEFAULT_LNG)
        private set
    var pets by mutableStateOf<List<Pet>>(emptyList())
        private set
    var selected by mutableStateOf<Pet?>(null)
        private set
    var sightings by mutableStateOf<List<Sighting>>(emptyList())
        private set

    fun setLocation(la: Double, ln: Double) {
        lat = la
        lng = ln
    }

    fun loadNearby() = launchApi {
        pets = api.nearby(lat, lng, settings.value?.radius ?: 10)
    }

    fun loadPet(id: String) = launchApi {
        selected = null
        sightings = emptyList()
        selected = api.pet(id)
        sightings = api.sightings(id)
    }

    fun createPet(body: NewPet, done: () -> Unit) = launchApi {
        api.createPet(body)
        done()
    }

    fun addSighting(body: NewSighting, done: () -> Unit) = launchApi {
        api.addSighting(body)
        done()
    }

    fun setStatus(id: String, status: String) = launchApi {
        selected = api.setStatus(id, StatusRequest(status))
        pets = api.nearby(lat, lng, settings.value?.radius ?: 10)
    }
    // ---- settings ----
    fun updateProfile(name: String, phone: String, done: () -> Unit) = launchApi {
        val u = api.updateProfile(ProfileUpdate(name, phone))
        prefs.saveProfile(u)
        done()
    }

    fun <T> setPref(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch { prefs.set(key, value) }
    }
}