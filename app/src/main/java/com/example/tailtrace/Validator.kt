package com.example.tailtrace

object Validator {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val phoneRegex = Regex("^\\+?[0-9]{9,15}$")

    fun isValidEmail(e: String) = emailRegex.matches(e.trim())
    fun isStrongPassword(p: String) = p.length >= 8 && p.any { it.isDigit() } && p.any { it.isLetter() }
    fun isValidPhone(p: String) = p.isBlank() || phoneRegex.matches(p.trim())
}