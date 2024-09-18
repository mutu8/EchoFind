package com.example.echofind.repository

class UserRepository {
    suspend fun login(username: String, password: String): Boolean {
        // Lógica de autenticación
        return true
    }
}