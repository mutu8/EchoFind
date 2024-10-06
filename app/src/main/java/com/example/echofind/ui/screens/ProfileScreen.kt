@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.echofind.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.echofind.R
import com.example.echofind.data.model.dataStore.UserDataStore
import com.example.echofind.data.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(navController: NavHostController, authViewModel: AuthViewModel = viewModel()) {
    // Obtener el userId del usuario autenticado
    val userId = authViewModel.getUserId()

    if (userId == null) {
        navController.navigate("Login") {
            popUpTo(0) { inclusive = true } // Esto limpia todas las pantallas previas
        }
        return
    }
    // Obtener una única instancia de UserDataStore para el userId
    val userDataStore = UserDataStore.getInstance(LocalContext.current, userId)
    val userInteractions = userDataStore.userInteractions.collectAsState(initial = emptyMap())
    val imageUri = rememberSaveable { mutableStateOf("") }
    val username by authViewModel.username.observeAsState(initial = "")

    // Solo llama a fetchUsername si el username aún no está definido.
    LaunchedEffect(userId) {
        if (username.isEmpty()) {
            authViewModel.fetchUsername()
        }
    }

    val painter = rememberAsyncImagePainter(
        imageUri.value.ifEmpty { R.drawable.profile_icon }
    )
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri.value = it.toString() }
    }

    val showDialog = rememberSaveable { mutableStateOf(false) }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(it)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(255.dp)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1DB954),
                                Color.Black.copy(0.75f)
                            )
                        )
                    )
            ) {
                Card(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(130.dp),
                    content = {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                                .clickable { launcher.launch("image/*") },
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                    }
                )
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "$username", // Cambia según el nombre del usuario real
                    style = TextStyle(fontSize = 28.sp),
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileTextField(
                    headerText = userInteractions.value["swipes"]?.toString() ?: "0",
                    footerText = "Swipes"
                )
                ProfileTextField(
                    headerText = userInteractions.value["likes"]?.toString() ?: "0",
                    footerText = "Liked"
                )
                ProfileTextField(
                    headerText = userInteractions.value["dislikes"]?.toString() ?: "0",
                    footerText = "Disliked"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(40.dp))
            OptionCard(
                leadingIcon = R.drawable.download,
                option = "Download"
            )
            OptionCard(
                leadingIcon = R.drawable.ic_bot,
                option = "Chatbot",
                onClick = { navController.navigate("chatbot") } // Navegar al chatbot
            )
            OptionCard(
                leadingIcon = R.drawable.log_out,
                option = "Log Out",
                onClick = { showDialog.value = true }
            )

            // Diálogo de confirmación
            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text(text = "Cerrar sesión") },
                    text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
                    confirmButton = {
                        Button(onClick = {
                            authViewModel.signout()
                            navController.navigate("Login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }) {
                            Text("Sí")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog.value = false }) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileTextField(headerText: String = "", footerText: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = headerText, fontSize = 20.sp, color = Color.White)
        Text(text = footerText, fontSize = 14.sp, color = Color.White.copy(0.7f))
    }
}

@Composable
fun OptionCard(
    leadingIcon: Int,
    option: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = leadingIcon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = option, fontSize = 18.sp, color = Color.White)
    }
}
