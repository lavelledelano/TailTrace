package com.example.tailtrace

data class User(val id: String, val fullName: String, val email: String, val phone: String?)
data class AuthResponse(val token: String, val user: User)
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val fullName: String, val email: String, val password: String, val phone: String)

data class Pet(
    val id: String,
    val petName: String,
    val species: String,
    val breed: String,
    val color: String,
    val description: String?,
    val status: String,
    val lat: Double,
    val lng: Double,
    val lastSeenAt: String,
    val photoUrl: String,
    val ownerId: String?,
    val ownerName: String?,
    val ownerPhone: String?,
)

data class NewPet(
    val petName: String, val species: String, val breed: String, val color: String,
    val description: String, val photoBase64: String,
    val lat: Double, val lng: Double, val lastSeenAt: String,
)

data class StatusRequest(val status: String)

data class Sighting(
    val id: String, val direction: String?, val notes: String?,
    val lat: Double, val lng: Double, val seenAt: String, val photoUrl: String?,
)

data class NewSighting(
    val petId: String, val lat: Double, val lng: Double,
    val direction: String, val notes: String, val photoBase64: String?,
)
data class ProfileUpdate(val fullName: String, val phone: String)