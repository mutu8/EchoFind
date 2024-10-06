package com.example.echofind.data.model.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserDataStore private constructor(private val context: Context, private val userId: String) {

    // Definir DataStore basado en el userId
    private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_$userId")

    companion object {
        private val instances = mutableMapOf<String, UserDataStore>()

        fun getInstance(context: Context, userId: String): UserDataStore {
            return instances.getOrPut(userId) { UserDataStore(context, userId) }
        }

        val SWIPES_KEY = intPreferencesKey("swipes")
        val LIKES_KEY = intPreferencesKey("likes")
        val DISLIKES_KEY = intPreferencesKey("dislikes")
    }

    // Función para obtener los datos almacenados
    val userInteractions: Flow<Map<String, Int>> = context.userDataStore.data.map { preferences ->
        mapOf(
            "swipes" to (preferences[SWIPES_KEY] ?: 0),
            "likes" to (preferences[LIKES_KEY] ?: 0),
            "dislikes" to (preferences[DISLIKES_KEY] ?: 0),
        )
    }

    // Función para guardar las interacciones
    suspend fun saveUserInteraction(swipes: Int, likes: Int, dislikes: Int) {
        context.userDataStore.edit { preferences ->
            preferences[SWIPES_KEY] = swipes
            preferences[LIKES_KEY] = likes
            preferences[DISLIKES_KEY] = dislikes
        }
    }
}
