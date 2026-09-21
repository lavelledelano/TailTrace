package com.example.tailtrace

data class User(val id: String, val fullName: String, val email: String, val phone: String?)
data class AuthResponse(val token: String, val user: User)
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val fullName: String, val email: String, val password: String, val phone: String)