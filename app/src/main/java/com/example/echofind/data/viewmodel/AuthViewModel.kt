package com.example.echofind.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echofind.data.model.player.Auth.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                if (result.user != null) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Incorrect credentials")
                }
            } catch (e: Exception) {
                if (e.message?.contains("There is no user record corresponding to this identifier") == true) {
                    _authState.value = AuthState.Error("User does not exist")
                } else {
                    _authState.value = AuthState.Error("Incorrect credentials")
                }
            }
        }
    }

    fun signup(email: String, password: String, user: String) {
        if (email.isEmpty() || password.isEmpty() || user.isEmpty()) {
            _authState.value = AuthState.Error("Any value is empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid
                    if (userId != null) {
                        val userMap = hashMapOf(
                            "userId" to userId,
                            "email" to email,
                            "username" to user
                        )
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Authenticated
                            }
                            .addOnFailureListener { e: Exception ->
                                _authState.value =
                                    AuthState.Error(e.message ?: "Something went wrong")
                            }
                    } else {
                        _authState.value = AuthState.Error("User ID is null")
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

