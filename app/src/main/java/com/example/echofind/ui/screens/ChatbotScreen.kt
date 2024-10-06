package com.example.echofind.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.echofind.data.model.chatbot.PreguntaRespuesta
import kotlinx.coroutines.launch

@Composable
fun ChatbotScreen(
    preguntasRespuestas: List<PreguntaRespuesta>,
    navController: NavController // Recibe NavController como parámetro
) {
    val botColor = Color(0xFFE3F2FD)
    val userMessageColor = Color(0xFFE3F2FD)
    val botMessageColor = Color(0xFFDCF8C6)
    val buttonColor = Color(0xFFE3F2FD)
    val textColor = Color(0xFF424242)

    val conversation = remember { mutableStateListOf<Pair<String, String>>() }
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Crear una lista mutable de preguntas
    var remainingQuestions by remember { mutableStateOf(preguntasRespuestas.toMutableList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top, // Cambiar el arreglo vertical
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Box para alinear el botón de retroceso y el emoji de robot
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // Espacio para separar del contenido de abajo
        ) {
            // Botón de retroceso alineado a la izquierda
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart) // Alineado al inicio (izquierda)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retroceder",
                    tint = Color.Black // Color del ícono
                )
            }

            // Emoji de robot alineado en el centro
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center) // Alineado en el centro
                    .background(botColor, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\uD83E\uDD16",
                    fontSize = 36.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Lista de conversación
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            items(conversation.size) { index ->
                val (speaker, message) = conversation[index]
                if (speaker == "Tú") {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        ChatBubble(
                            message = message,
                            isUser = true,
                            backgroundColor = userMessageColor
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        ChatBubble(
                            message = message,
                            isUser = false,
                            backgroundColor = botMessageColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Menú desplegable
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = buttonColor),
                enabled = remainingQuestions.isNotEmpty() // Deshabilitar si no hay preguntas
            ) {
                Text(
                    text = "Selecciona una pregunta",
                    textAlign = TextAlign.Center,
                    color = if (remainingQuestions.isNotEmpty()) textColor else Color.Gray, // Cambiar color si está deshabilitado
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Mostrar solo preguntas restantes
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                remainingQuestions.forEach { preguntaRespuesta ->
                    DropdownMenuItem(
                        text = { Text(preguntaRespuesta.pregunta, color = textColor) },
                        onClick = {
                            // Agregar la pregunta y la respuesta a la conversación
                            conversation.add("Tú" to preguntaRespuesta.pregunta)
                            conversation.add("Bot" to preguntaRespuesta.respuesta)

                            // Eliminar la pregunta seleccionada de las preguntas restantes
                            remainingQuestions = remainingQuestions.filterNot { it.pregunta == preguntaRespuesta.pregunta }.toMutableList()

                            expanded = false
                            coroutineScope.launch {
                                listState.animateScrollToItem(conversation.size - 1)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: String, isUser: Boolean, backgroundColor: Color) {
    val textColor = if (isUser) Color.Black else Color.Black
    val bubbleShape = if (isUser) {
        androidx.compose.foundation.shape.RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        androidx.compose.foundation.shape.RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    androidx.compose.material3.Card(
        shape = bubbleShape,
        modifier = Modifier
            .padding(8.dp)
            .wrapContentWidth(Alignment.Start)
            .widthIn(max = 250.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = message,
                color = textColor,
                fontSize = 16.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}
