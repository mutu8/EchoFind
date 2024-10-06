package com.example.echofind.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echofind.data.model.firebase.AuthState
import com.example.echofind.data.model.player.Song
import com.example.echofind.data.model.player.TrackItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance() // Inicializa Firestore

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username // LiveData para observar el nombre de usuario

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

    // Nueva función para obtener el userId del usuario autenticado
    fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    // Nueva función para obtener el nombre de usuario desde Firestore
    fun fetchUsername() {
        val userId = getUserId() ?: return
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "Sin Nombre"
                    _username.value = username
                } else {
                    Log.w("Firestore", "Documento del usuario no existe")
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al obtener el nombre de usuario: ", e)
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

    fun guardarCancionSeleccionada(trackItem: TrackItem) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val cancionMap = hashMapOf(
                "trackId" to trackItem.id,
                "nombre" to trackItem.name,
                "preview_url" to trackItem.preview_url,
                "album" to trackItem.album.images.firstOrNull()?.url, // Guarda la imagen del álbum
                "artistas" to trackItem.artists.map { it.name }, // Guarda los nombres de los artistas
                "popularity" to trackItem.popularity, // Popularidad de la canción

                // Si las características de audio existen, se guardan en Firestore
                "tempo" to (trackItem.audioFeatures?.tempo ?: 0.0),
                "energy" to (trackItem.audioFeatures?.energy ?: 0.0),
                "valence" to (trackItem.audioFeatures?.valence ?: 0.0),
                "danceability" to (trackItem.audioFeatures?.danceability ?: 0.0)
            )

            db.collection("users")
                .document(userId)
                .collection("canciones_seleccionadas")
                .add(cancionMap)
                .addOnSuccessListener {
                    Log.d("Firestore", "Canción guardada exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al guardar canción: ", e)
                }
        } else {
            Log.w("Firestore", "Usuario no autenticado")
        }
    }


    fun guardarCancionDislikeada(trackItem: TrackItem) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val cancionMap = hashMapOf(
                "trackId" to trackItem.id,
                "nombre" to trackItem.name,
                "preview_url" to trackItem.preview_url,
                "album" to trackItem.album.images.firstOrNull()?.url, // Guarda la imagen del álbum
                "artistas" to trackItem.artists.map { it.name }, // Guarda los nombres de los artistas
                "popularity" to trackItem.popularity, // Popularidad de la canción
                // Si las características de audio existen, se guardan en Firestore
                "tempo" to (trackItem.audioFeatures?.tempo ?: 0.0),
                "energy" to (trackItem.audioFeatures?.energy ?: 0.0),
                "valence" to (trackItem.audioFeatures?.valence ?: 0.0),
                "danceability" to (trackItem.audioFeatures?.danceability ?: 0.0)
            )

            db.collection("users")
                .document(userId)
                .collection("canciones_dislikeadas")
                .add(cancionMap)
                .addOnSuccessListener {
                    Log.d("Firestore", "Canción dislikeada guardada exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al guardar canción dislikeada: ", e)
                }
        } else {
            Log.w("Firestore", "Usuario no autenticado")
        }
    }


    fun obtenerCancionesGuardadas(callback: (List<Song>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("users")
                .document(userId)
                .collection("canciones_seleccionadas")
                .get()
                .addOnSuccessListener { documents ->
                    val canciones = documents.map { document ->
                        Song(
                            title = document.getString("nombre") ?: "",
                            artist = (document.get("artistas") as? List<String>)?.joinToString(", ") ?: "",
                            imageUrl = document.getString("album") ?: "",
                            previewUrl = document.getString("preview_url") ?: "" // Obtener el preview_url
                        )
                    }
                    callback(canciones) // Retorna las canciones guardadas
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al obtener canciones: ", e)
                }
        }
    }

    // Nueva función para eliminar una canción guardada
    fun eliminarCancionGuardada(song: Song) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("users")
                .document(userId)
                .collection("canciones_seleccionadas")
                .whereEqualTo("nombre", song.title)
                .get()
                .addOnSuccessListener { documents ->
                    documents.forEach { document ->
                        db.collection("users")
                            .document(userId)
                            .collection("canciones_seleccionadas")
                            .document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("Firestore", "Canción eliminada exitosamente")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error al eliminar canción: ", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al obtener canciones: ", e)
                }
        }
    }

}

