@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.echofind.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.echofind.R

@Composable
fun ProfileScreen(navController: NavHostController) {

    val imageUri = rememberSaveable { mutableStateOf("") }
    val painter = rememberAsyncImagePainter(
        imageUri.value.ifEmpty { R.drawable.profile_icon }
    )
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri.value = it.toString() }
    }

    StatusBarColor()

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
                        .size(130.dp)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .wrapContentSize()
                            .background(Color.Black)
                            .clickable { launcher.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "Steven",
                    style = TextStyle(
                        fontSize = 28.sp,
                        //fontFamily = FontFamily(Font(R.font.outfit_medium))
                    ),
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
                ProfileTextField(headerText = "38", footerText = "Swipes")
                ProfileTextField(headerText = "22", footerText = "Liked")
                ProfileTextField(headerText = "16", footerText = "Disliked")
                ProfileTextField(headerText = "0", footerText = "Skips")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(40.dp))
            OptionCard(
                leadingIcon = R.drawable.download,
                option = "Download"
            )
            OptionCard(
                leadingIcon = R.drawable.edit_icon,
                option = "Edit Profile"
            )
            OptionCard(
                leadingIcon = R.drawable.log_out,
                option = "Log Out"
            )
        }
    }
}

@Composable
fun StatusBarColor() {
    // Configuración del color de la barra de estado
}

@Composable
fun ProfileTextField(headerText: String = "", footerText: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = headerText, fontSize = 20.sp, color = Color.White)
        Text(text = footerText, fontSize = 14.sp, color = Color.White.copy(0.7f))
    }
}

@Composable
fun OptionCard(leadingIcon: Int, option: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { },
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
