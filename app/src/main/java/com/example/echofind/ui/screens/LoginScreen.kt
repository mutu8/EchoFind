package com.example.echofind.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.echofind.R

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.page1))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    navController.navigate("twyperPreview")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login", color = Color.White, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = AnnotatedString("Don't have an account? Register here"),
                color = Color.Blue,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }
    }
}