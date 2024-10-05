package com.example.echofind.data.model.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StoreBoarding(private val context: Context){
    companion object{
        private val Context.dataStore : DataStore<Preferences> by preferencesDataStore("storeBoarding")
        val STORE_BOARD = booleanPreferencesKey("store_board")
    }

    val getBoarding: Flow<Boolean> = context.dataStore.data.map{
            preference -> preference[STORE_BOARD] ?: false
    }

    suspend fun saveBoarding(value: Boolean){
        context.dataStore.edit{
                preferences -> preferences[STORE_BOARD] = value
        }
    }
}
    