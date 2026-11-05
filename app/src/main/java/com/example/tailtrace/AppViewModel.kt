package com.example.tailtrace

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val api = ApiClient.api
    private val db = OfflineDb(app)
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var syncRunning = false

    /** null means still loading from the phone's storage */
    val settings: StateFlow<Settings?> = prefs.settings
        .onEach { ApiClient.token = it.token }
        .map<Settings, Settings?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    // ---- offline state ----
    var online by mutableStateOf(true)
        private set
    var pending by mutableStateOf(0)
        private set
    var syncing by mutableStateOf(false)
        private set

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

    init {
        refreshPending()
        runCatching {
            val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val cb = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    viewModelScope.launch { syncNow() }
                }

                override fun onLost(network: Network) {
                    viewModelScope.launch { online = false }
                }
            }
            cm.registerDefaultNetworkCallback(cb)
            networkCallback = cb
        }
    }

    override fun onCleared() {
        runCatching {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let { cm.unregisterNetworkCallback(it) }
        }
        super.onCleared()
    }

    private fun refreshPending() {
        pending = db.pendingCount()
    }

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
    fun setLocation(la: Double, ln: Double) { lat = la; lng = ln }

    // ---- auth ----
    fun login(email: String, password: String) = launchApi {
        prefs.saveSession(api.login(LoginRequest(email, password)))
    }

    fun register(name: String, email: String, password: String, phone: String) = launchApi {
        prefs.saveSession(api.register(RegisterRequest(name, email, password, phone)))
    }

    fun logout() {
        viewModelScope.launch { prefs.clearSession() }
    }

    // ---- pets (with offline fallback) ----
    fun loadNearby() = viewModelScope.launch {
        loading = true
        error = null
        try {
            val list = api.nearby(lat, lng, settings.value?.radius ?: 10)
            pets = list
            online = true
            withContext(Dispatchers.IO) { db.savePets(list) }
        } catch (e: IOException) {
            online = false
            pets = withContext(Dispatchers.IO) { db.loadPets() }
        } catch (e: Exception) {
            error = e.friendly()
        } finally {
            loading = false
        }
    }

    fun loadPet(id: String) = viewModelScope.launch {
        loading = true
        error = null
        selected = null
        sightings = emptyList()
        try {
            selected = api.pet(id)
            sightings = api.sightings(id)
        } catch (e: IOException) {
            online = false
            selected = pets.firstOrNull { it.id == id }
            if (selected == null) error = e.friendly()
        } catch (e: Exception) {
            error = e.friendly()
        } finally {
            loading = false
        }
    }

    fun createPet(body: NewPet, done: () -> Unit) = viewModelScope.launch {
        loading = true
        error = null
        try {
            api.createPet(body)
            done()
        } catch (e: IOException) {
            withContext(Dispatchers.IO) { db.queuePet(body) }
            refreshPending()
            online = false
            done()
        } catch (e: Exception) {
            error = e.friendly()
        } finally {
            loading = false
        }
    }

    fun addSighting(body: NewSighting, done: () -> Unit) = viewModelScope.launch {
        loading = true
        error = null
        try {
            api.addSighting(body)
            done()
        } catch (e: IOException) {
            withContext(Dispatchers.IO) { db.queueSighting(body) }
            refreshPending()
            online = false
            done()
        } catch (e: Exception) {
            error = e.friendly()
        } finally {
            loading = false
        }
    }

    fun setStatus(id: String, status: String) = launchApi {
        selected = api.setStatus(id, StatusRequest(status))
        pets = api.nearby(lat, lng, settings.value?.radius ?: 10)
    }

    // ---- sync: upload everything that was created offline ----
    fun syncNow() {
        viewModelScope.launch {
            syncPending()
            loadNearby()
        }
    }

    private suspend fun syncPending() {
        if (syncRunning || ApiClient.token == null) return
        syncRunning = true
        syncing = true
        try {
            val queuedPets = withContext(Dispatchers.IO) { db.pendingPets() }
            for ((id, body) in queuedPets) {
                try {
                    api.createPet(body)
                    withContext(Dispatchers.IO) { db.removePet(id) }
                } catch (e: HttpException) {
                    if (e.code() == 400) withContext(Dispatchers.IO) { db.removePet(id) } else return
                }
            }
            val queuedSightings = withContext(Dispatchers.IO) { db.pendingSightings() }
            for ((id, body) in queuedSightings) {
                try {
                    api.addSighting(body)
                    withContext(Dispatchers.IO) { db.removeSighting(id) }
                } catch (e: HttpException) {
                    if (e.code() == 400 || e.code() == 404) withContext(Dispatchers.IO) { db.removeSighting(id) } else return
                }
            }
        } catch (e: IOException) {
            // still offline: keep the queue and try again later
        } catch (e: Exception) {
            // keep the queue
        } finally {
            syncRunning = false
            syncing = false
            refreshPending()
        }
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